package OhmOMatic.Chord;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * Fixfingers thread that periodically access a random entry in finger table
 * and fix it.
 *
 * @author Chuan Xia
 */

public class FixFingers extends Thread
{

	private Node local;
	private final Random random;
	boolean alive;

	public FixFingers(Node node)
	{
		local = node;
		alive = true;
		random = new Random();
	}

	@Override
	public void run()
	{
		while (alive)
		{
			int i = random.nextInt(31) + 2;
			InetSocketAddress ithfinger = null;

			try
			{
				ithfinger = local.find_successor(Helper.ithStart(local.getId(), i));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				local.updateFingers(i, ithfinger);
			}
			catch (Exception e)
			{
				e.printStackTrace();
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

	public void toDie()
	{
		alive = false;
	}

}
