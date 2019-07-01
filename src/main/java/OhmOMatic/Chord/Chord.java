/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Chord.FN.Richiesta;
import OhmOMatic.Chord.FN.gRPC_Client;
import OhmOMatic.Chord.FN.gRPC_Server;
import OhmOMatic.Global.GB;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Chord implements AutoCloseable
{
	private final char mBit = 32;

	private final Timer timersChord;

	private final NodeLink n;
	private final Thread threadListener;
	private NodeLink predecessor;
	private final HashMap<Integer, NodeLink> fingerTable;
	private Server gRPC_listner;
	private int next = 1;

	private final long ID;


	public Chord(NodeLink address)
	{
		n = address;
		ID = GB.hashSocketAddress(address);

		fingerTable = new HashMap<>(mBit);

		setSuccessor(address);
		predecessor = null;

		timersChord = new Timer();

		threadListener = new Thread(() -> listener());
		threadListener.start();
	}

	@Override
	public void close()
	{
		timersChord.cancel();
		threadListener.stop();
	}

	public NodeLink getPredecessor()
	{
		return predecessor;
	}

	public synchronized void setPredecessor(NodeLink n_)
	{
		predecessor = n_;
	}

	public NodeLink getSuccessor()
	{
		return fingerTable.get(1);
	}

	public void setSuccessor(NodeLink n_)
	{
		fingerTable.put(1, n_);
	}

	private void listener()
	{
		var chord = this;

		try
		{
			gRPC_listner = ServerBuilder
					.forPort(n.port)
					.addService(gRPC_Server.getListnerServer(chord))
					.build()
					.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// ask getNode n to find id's getSuccessor
	public NodeLink find_successor(long _id)
	{
		var _successor = getSuccessor();
		var successor_ID = GB.computeRelativeId(_successor, ID, mBit);
		var id = GB.computeRelativeId(_id, ID, mBit);

		if ((id > ID && id <= successor_ID))
		{
			return _successor;
		}
		else
		{
			var n_ = closest_preceding_finger(id);

			if (n.equals(n_))
				_successor = n;
			else
				_successor = gRPC_Client.gRPC(n_, Richiesta.findSuccessor, id);

			return _successor;
		}
	}

	// return closest fingerTable preceding id
	public NodeLink closest_preceding_finger(long _id)
	{
		for (var i = mBit; i > 0; i--)
		{
			var ith_finger = fingerTable.get(i);

			if (ith_finger == null)
				continue;

			var ith_finger_ID = GB.computeRelativeId(ith_finger, ID, mBit);
			var id = GB.computeRelativeId(_id, ID, mBit);

			if (ith_finger_ID > ID && ith_finger_ID < id)
				return ith_finger;
		}

		return n;
	}

	//stabilize the chord ring/circle after getNode joins and departures
	private void startStabilizingRoutines()
	{
		timersChord.scheduleAtFixedRate(new TimerTask()
		{
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
		}, new Date(), 60);

		timersChord.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				fix_fingers();
			}
		}, new Date(), 500);
	}

	// getNode n joins the network;
	// n_ is an arbitrary getNode in the network
	public void join(NodeLink n_) throws Exception
	{
		if (!n.equals(n_))
		{
			var s = gRPC_Client.gRPC(n_, Richiesta.findSuccessor, ID);
			setSuccessor(s);
			//successor = n_.find_successor(n);
		}

		startStabilizingRoutines();
	}

	// if s is iTh fingerTable of n, update n's fingerTable table with s
	public void update_finger_table(NodeLink s, int i)
	{
		var ith_finger = fingerTable.get(i);

		if (ith_finger != null)
		{
			var s_ID = GB.computeRelativeId(s, ID, mBit);
			var ith_finger_ID = GB.computeRelativeId(ith_finger, ID, mBit);

			if (s_ID >= ID && s_ID < ith_finger_ID)
			{
				fingerTable.put(i, s);

				//get first getNode preceding n
				gRPC_Client.gRPC(predecessor, Richiesta.updateFingerTable, null, i, s);
				//p.update_finger_table(s, i);
			}
		}
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize() throws Exception
	{
		var _successor = getSuccessor();
		var x = gRPC_Client.gRPC(_successor, Richiesta.predecessor);
		//var x = successor.predecessor;

		if (x != null)
		{
			var x_ID = GB.computeRelativeId(x, ID, mBit);
			var successor_ID = GB.computeRelativeId(_successor, ID, mBit);

			if (x_ID > ID && x_ID < successor_ID)
				setSuccessor(x);

			gRPC_Client.gRPC(_successor, Richiesta.notify, n);
			//successor.notify(n);
		}
	}

	// n_ thinks it might be our predecessor.
	public void notify(NodeLink n_)
	{
		var n__ID = GB.computeRelativeId(n_, ID, mBit);
		var predecessor_ID = GB.computeRelativeId(predecessor, ID, mBit);

		if (predecessor == null || (n__ID > predecessor_ID && n__ID < ID))
			predecessor = n_;
	}

	// called periodically refreshes fingerTable table entries.
	private void fix_fingers()
	{
		next++;

		if (next > mBit)
			next = 1;

		var d = GB.getPowerOfTwo(next - 1, mBit);
		d += ID;

		var iThFinger = find_successor(d.longValue());

		fingerTable.put(next, iThFinger);

		//forse
		//notify(iThFinger);
	}

	public void printDataStructure()
	{
		System.out.println("\n==============================================================");
		System.out.println("\nLOCAL:\t\t\t\t" + n.toString() + "\t" + GB.hexIdAndPosition(n, mBit));

		if (predecessor != null)
			System.out.println("\nPREDECESSOR:\t\t\t" + predecessor.toString() + "\t" + GB.hexIdAndPosition(predecessor, mBit));
		else
			System.out.println("\nPREDECESSOR:\t\t\tNULL");

		System.out.println("\nFINGER TABLE:\n");

		for (int i = 1; i <= 32; i++)
		{
			long ithstart = GB.ithStart(GB.hashSocketAddress(n), i, mBit);

			var f = fingerTable.get(i);

			var sb = new StringBuilder();

			sb.append(i + "\t" + GB.longTo8DigitHex(ithstart) + "\t\t");

			if (f != null)
				sb.append(f + "\t" + GB.hexIdAndPosition(f, mBit));

			else
				sb.append("NULL");

			System.out.println(sb.toString());
		}

		System.out.println("\n==============================================================\n");
	}

	public void printNeighbors()
	{
		System.out.println("You are listening on port " + n.port + ".");
		System.out.println("Your position is " + n + ".");

		var _successor = getSuccessor();
		if ((predecessor == null || predecessor.equals(n)) && (_successor == null || _successor.equals(n)))
		{
			System.out.println("Your predecessor is yourself.");
			System.out.println("Your successor is yourself.");
		}
		else
		{
			if (predecessor != null)
				System.out.println("Your predecessor is getNode " + predecessor.IP + ", " + "port " + predecessor.port + ", position " + predecessor + ".");
			else
				System.out.println("Your predecessor is updating.");

			if (_successor != null)
				System.out.println("Your successor is getNode " + _successor.IP + ", " + "port " + _successor.port + ", position " + _successor + ".");
			else
				System.out.println("Your successor is updating.");
		}
	}


}