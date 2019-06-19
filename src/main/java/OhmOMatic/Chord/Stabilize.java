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

public class Stabilize extends Thread
{

	private Node local;
	private boolean alive = true;

	public Stabilize(Node _local)
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
			stabilize();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	//periodically verify this's immediatre succesor;
	//and tell the successor about this
	private void stabilize() throws Exception
	{
		while (alive)
		{
			NodeLink successor = local.getSuccessor();

			if (successor == null || successor.equals(local.getAddress()))
				local.update_finger_table(null, -3);

			successor = local.getSuccessor();

			if (successor != null && !successor.equals(local.getAddress()))
			{
				NodeLink x = gRPCCommander.requestAddress(successor, Richiesta.Predecessor);

				if (x == null)
				{
					local.update_finger_table(null, -1);
				}
				else if (!x.equals(successor))
				{
					long local_id = gRPCCommander.hashNodeLink(local.getAddress());
					long successor_relative_id = gRPCCommander.computeRelativeId(gRPCCommander.hashNodeLink(successor), local_id);
					long x_relative_id = gRPCCommander.computeRelativeId(gRPCCommander.hashNodeLink(x), local_id);

					if (x_relative_id > 0 && x_relative_id < successor_relative_id)
						local.update_finger_table(x, 1);
				}
				else
				{
					local.notify(successor);
				}
			}

			Thread.sleep(60);
		}
	}


}