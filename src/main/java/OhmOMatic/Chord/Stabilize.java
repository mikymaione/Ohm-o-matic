/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

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
		while (alive)
		{
			NodeLink successor = local.getSuccessor();

			if (successor == null || successor.equals(local.getAddress()))
				try
				{
					local.updateFingers(-3, null); //fill
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			successor = local.getSuccessor();

			if (successor != null && !successor.equals(local.getAddress()))
			{
				NodeLink x = null;

				try
				{
					x = Helper.requestAddress(successor, Richiesta.YOURPRE);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (x == null)
				{
					try
					{
						local.updateFingers(-1, null);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (!x.equals(successor))
				{
					long local_id = Helper.hashSocketAddress(local.getAddress());
					long successor_relative_id = Helper.computeRelativeId(Helper.hashSocketAddress(successor), local_id);
					long x_relative_id = Helper.computeRelativeId(Helper.hashSocketAddress(x), local_id);

					if (x_relative_id > 0 && x_relative_id < successor_relative_id)
						try
						{
							local.updateFingers(1, x);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
				}
				else
				{
					try
					{
						local.notify(successor);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			try
			{
				Thread.sleep(60);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}


}