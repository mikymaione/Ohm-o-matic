/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Chord.FN.Richiesta;
import OhmOMatic.Chord.FN.gRPCCommander;
import OhmOMatic.ProtoBuffer.Common;

public class AskPredecessor extends Thread
{

	private Node local;
	private boolean alive = true;

	public AskPredecessor(Node _local)
	{
		local = _local;
	}

	public void close()
	{
		alive = false;
	}

	@Override
	public void run()
	{
		try
		{
			askPredecessor();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void askPredecessor() throws Exception
	{
		while (alive)
		{
			NodeLink predecessor = local.getPredecessor();

			if (predecessor != null)
			{
				var r = gRPCCommander.<Common.standardRes>sendRequest(predecessor, Richiesta.Ping);

				if (!r.getOk())
					local.clearPredecessor();
			}

			Thread.sleep(500);
		}
	}


}