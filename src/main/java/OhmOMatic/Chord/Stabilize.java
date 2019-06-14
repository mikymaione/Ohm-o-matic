package OhmOMatic.Chord;

import java.net.InetSocketAddress;

/**
 * Stabilize thread that periodically asks successor for its predecessor
 * and determine if current node should update or delete its successor.
 *
 * @author Chuan Xia
 */

public class Stabilize extends Thread
{

	private Node local;
	private boolean alive;

	public Stabilize(Node _local)
	{
		local = _local;
		alive = true;
	}

	@Override
	public void run()
	{
		while (alive)
		{
			InetSocketAddress successor = local.getSuccessor();

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

				// try to get my successor's predecessor
				InetSocketAddress x = null;

				try
				{
					x = Helper.requestAddress(successor, Richiesta.YOURPRE);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// if bad connection with successor! delete successor
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
				// else if successor's predecessor is not itself
				else if (!x.equals(successor))
				{
					long local_id = Helper.hashSocketAddress(local.getAddress());
					long successor_relative_id = Helper.computeRelativeId(Helper.hashSocketAddress(successor), local_id);
					long x_relative_id = Helper.computeRelativeId(Helper.hashSocketAddress(x), local_id);

					if (x_relative_id > 0 && x_relative_id < successor_relative_id)
					{
						try
						{
							local.updateFingers(1, x);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				// successor's predecessor is successor itself, then notify successor
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

	public void toDie()
	{
		alive = false;
	}


}