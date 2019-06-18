/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;

import java.util.HashMap;

public class Node implements AutoCloseable
{

	private final long localId;
	private final NodeLink localNode;

	private NodeLink predecessor;

	private HashMap<Integer, NodeLink> finger = new HashMap<>();

	private final Listener listener;
	private final Stabilize stabilize;
	private final FixFingers fix_fingers;
	private final AskPredecessor ask_predecessor;


	public Node(NodeLink address) throws Exception
	{
		localNode = address;
		localId = Helper.hashSocketAddress(localNode);

		for (var i = 1; i <= Helper.mBit; i++)
			updateIthFinger(i, null);

		predecessor = null;

		listener = new Listener(this);
		stabilize = new Stabilize(this);
		fix_fingers = new FixFingers(this);
		ask_predecessor = new AskPredecessor(this);
	}

	//node this joins the network;
	//s is an arbitrary node in the network
	public boolean join(NodeLink s) throws Exception
	{
		if (s != null && !s.equals(localNode))
		{
			var successor = Helper.requestAddress(s, Richiesta.FindSuccessor, localId, "");

			if (successor == null)
				throw new Exception("Nodo " + s + " non trovato!");

			updateIthFinger(1, successor);
		}

		listener.start();
		stabilize.start();
		fix_fingers.start();
		ask_predecessor.start();

		return true;
	}

	//s thinks it might be our predecessor
	public String notify(NodeLink s) throws Exception
	{
		if (s != null && !s.equals(localNode))
		{
			var v = Helper.<Home.casaRes>sendRequest(s, Richiesta.ImPredecessor, -1, localNode.IP + ":" + localNode.port);

			return v.getStandardRes().getMsg();
		}
		else
		{
			return null;
		}
	}

	public void notified(NodeLink newpre)
	{
		if (predecessor == null || predecessor.equals(localNode))
		{
			this.setPredecessor(newpre);
		}
		else
		{
			long oldpre_id = Helper.hashSocketAddress(predecessor);
			long local_relative_id = Helper.computeRelativeId(localId, oldpre_id);
			long newpre_relative_id = Helper.computeRelativeId(Helper.hashSocketAddress(newpre), oldpre_id);

			if (newpre_relative_id > 0 && newpre_relative_id < local_relative_id)
				this.setPredecessor(newpre);
		}
	}

	public NodeLink find_successor(long id) throws Exception
	{
		NodeLink ret = this.getSuccessor();
		NodeLink pre = find_predecessor(id);

		if (!pre.equals(localNode))
			ret = Helper.requestAddress(pre, Richiesta.Successor);

		if (ret == null)
			ret = localNode;

		return ret;
	}

	//ask node this to find id's predecessor
	private NodeLink find_predecessor(long findid) throws Exception
	{
		NodeLink n = this.localNode;
		NodeLink s = this.getSuccessor();
		NodeLink most_recently_alive = this.localNode;

		var n_successor_relative_id = 0L;

		if (s != null)
			n_successor_relative_id = Helper.computeRelativeId(Helper.hashSocketAddress(s), Helper.hashSocketAddress(n));

		long findid_relative_id = Helper.computeRelativeId(findid, Helper.hashSocketAddress(n));

		while (!(findid_relative_id > 0 && findid_relative_id <= n_successor_relative_id))
		{
			NodeLink pre_n = n;

			if (n.equals(this.localNode))
			{
				n = this.closest_preceding_finger(findid);
			}
			else
			{
				NodeLink result = Helper.requestAddress(n, Richiesta.ClosestPrecedingFinger, findid, "");

				if (result == null)
				{
					n = most_recently_alive;
					s = Helper.requestAddress(n, Richiesta.Successor);

					if (s == null)
					{
						System.out.println("It's not possible.");
						return localNode;
					}

					continue;
				}
				else if (result.equals(n))
				{
					return result;
				}
				else
				{
					most_recently_alive = n;

					s = Helper.requestAddress(result, Richiesta.Successor);

					if (s != null)
						n = result;
					else
						s = Helper.requestAddress(n, Richiesta.Successor);
				}

				n_successor_relative_id = Helper.computeRelativeId(Helper.hashSocketAddress(s), Helper.hashSocketAddress(n));
				findid_relative_id = Helper.computeRelativeId(findid, Helper.hashSocketAddress(n));
			}

			if (pre_n.equals(n))
				break;
		}
		return n;
	}

	//return closest finger preceding id
	public NodeLink closest_preceding_finger(long findid) throws Exception
	{
		long findid_relative = Helper.computeRelativeId(findid, localId);

		for (var i = Helper.mBit; i > 0; i--)
		{
			NodeLink ith_finger = finger.get(i);

			if (ith_finger == null)
				continue;

			long ith_finger_id = Helper.hashSocketAddress(ith_finger);
			long ith_finger_relative_id = Helper.computeRelativeId(ith_finger_id, localId);

			if (ith_finger_relative_id > 0 && ith_finger_relative_id < findid_relative)
			{
				var response = Helper.<Common.standardRes>sendRequest(ith_finger, Richiesta.Ping);

				if (response != null && response.getMsg().equals("ALIVE"))
					return ith_finger;
				else
					update_finger_table(ith_finger, -2);
			}
		}

		return localNode;
	}

	//if s is iTh finger of this, update n's finger table with s
	public synchronized void update_finger_table(NodeLink s, int i) throws Exception
	{
		if (i > 0 && i <= Helper.mBit)
			updateIthFinger(i, s);
		else if (i == -1)
			deleteSuccessor();
		else if (i == -2)
			deleteCertainFinger(s);
		else if (i == -3)
			fillSuccessor();
	}

	private void updateIthFinger(int i, NodeLink value) throws Exception
	{
		finger.put(i, value);

		if (i == 1 && value != null && !value.equals(localNode))
			notify(value);
	}

	private void deleteSuccessor() throws Exception
	{
		NodeLink successor = getSuccessor();

		if (successor == null)
			return;

		var i = Helper.mBit;
		for (i = Helper.mBit; i > 0; i--)
		{
			NodeLink ithfinger = finger.get(i);

			if (ithfinger != null && ithfinger.equals(successor))
				break;
		}

		for (var j = i; j >= 1; j--)
			updateIthFinger(j, null);

		if (predecessor != null && predecessor.equals(successor))
			setPredecessor(null);

		fillSuccessor();
		successor = getSuccessor();

		if ((successor == null || successor.equals(successor)) && predecessor != null && !predecessor.equals(localNode))
		{
			NodeLink p = predecessor;
			NodeLink p_pre = null;

			while (true)
			{
				p_pre = Helper.requestAddress(p, Richiesta.Predecessor);

				if (p_pre == null)
					break;

				if (p_pre.equals(p) || p_pre.equals(localNode) || p_pre.equals(successor))
					break;
				else
					p = p_pre;
			}

			updateIthFinger(1, p);
		}
	}

	private void deleteCertainFinger(NodeLink f)
	{
		for (var i = Helper.mBit; i > 0; i--)
		{
			NodeLink ithfinger = finger.get(i);
			if (ithfinger != null && ithfinger.equals(f))
				finger.put(i, null);
		}
	}

	private void fillSuccessor() throws Exception
	{
		NodeLink successor = this.getSuccessor();

		if (successor == null || successor.equals(localNode))
			for (var i = 2; i <= Helper.mBit; i++)
			{
				NodeLink ithfinger = finger.get(i);

				if (ithfinger != null && !ithfinger.equals(localNode))
				{
					for (var j = i - 1; j >= 1; j--)
						updateIthFinger(j, ithfinger);

					break;
				}
			}

		successor = getSuccessor();

		if ((successor == null || successor.equals(localNode)) && predecessor != null && !predecessor.equals(localNode))
			updateIthFinger(1, predecessor);
	}

	public void clearPredecessor()
	{
		setPredecessor(null);
	}

	private synchronized void setPredecessor(NodeLink pre)
	{
		predecessor = pre;
	}

	public long getId()
	{
		return localId;
	}

	public NodeLink getAddress()
	{
		return localNode;
	}

	public NodeLink getPredecessor()
	{
		return predecessor;
	}

	public NodeLink getSuccessor()
	{
		if (finger != null && finger.size() > 0)
			return finger.get(1);

		return null;
	}

	public void printNeighbors()
	{
		System.out.println("\nYou are listening on port " + localNode.port + "." + "\nYour position is " + Helper.hexIdAndPosition(localNode) + ".");
		NodeLink successor = finger.get(1);

		if ((predecessor == null || predecessor.equals(localNode)) && (successor == null || successor.equals(localNode)))
		{
			System.out.println("Your predecessor is yourself.");
			System.out.println("Your successor is yourself.");
		}
		else
		{
			if (predecessor != null)
				System.out.println("Your predecessor is node " + predecessor.IP + ", " + "port " + predecessor.port + ", position " + Helper.hexIdAndPosition(predecessor) + ".");
			else
				System.out.println("Your predecessor is updating.");

			if (successor != null)
				System.out.println("Your successor is node " + successor.IP + ", " + "port " + successor.port + ", position " + Helper.hexIdAndPosition(successor) + ".");
			else
				System.out.println("Your successor is updating.");
		}
	}

	public void printDataStructure()
	{
		System.out.println("\n==============================================================");
		System.out.println("\nLOCAL:\t\t\t\t" + localNode.toString() + "\t" + Helper.hexIdAndPosition(localNode));

		if (predecessor != null)
			System.out.println("\nPREDECESSOR:\t\t\t" + predecessor.toString() + "\t" + Helper.hexIdAndPosition(predecessor));
		else
			System.out.println("\nPREDECESSOR:\t\t\tNULL");

		System.out.println("\nFINGER TABLE:\n");

		for (var i = 1; i <= Helper.mBit; i++)
		{
			long ithstart = Helper.iThStart(Helper.hashSocketAddress(localNode), i);

			NodeLink f = finger.get(i);

			StringBuilder sb = new StringBuilder();

			sb.append(i + "\t" + Helper.longTo8DigitHex(ithstart) + "\t\t");

			if (f != null)
				sb.append(f.toString() + "\t" + Helper.hexIdAndPosition(f));
			else
				sb.append("NULL");

			System.out.println(sb.toString());
		}

		System.out.println("\n==============================================================\n");
	}

	@Override
	public void close()
	{
		if (listener != null)
			listener.close();

		if (fix_fingers != null)
			fix_fingers.close();

		if (stabilize != null)
			stabilize.close();

		if (ask_predecessor != null)
			ask_predecessor.close();
	}


}