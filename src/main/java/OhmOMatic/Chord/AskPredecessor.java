/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.Common;

import java.net.InetSocketAddress;

public class AskPredecessor extends Thread
{

	private Node local;
	private boolean alive = true;

	public AskPredecessor(Node _local)
	{
		local = _local;
	}

	public void die()
	{
		alive = false;
	}

	@Override
	public void run()
	{
		while (alive)
		{
			InetSocketAddress predecessor = local.getPredecessor();

			if (predecessor != null)
			{
				String response = null;

				try
				{
					var r = Helper.<Common.standardRes>sendRequest(predecessor, Richiesta.KEEP);

					response = r.getMsg();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (response == null || !response.equals("ALIVE"))
					local.clearPredecessor();
			}

			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}


}