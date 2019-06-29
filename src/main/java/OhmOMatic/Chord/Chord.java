/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Chord.FN.Richiesta;
import OhmOMatic.Chord.FN.gRPCCommander;
import OhmOMatic.Global.GB;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Chord implements AutoCloseable
{
	private final char mBit = 160; //sha1

	private final Timer timersChord;

	private final NodeLink n;
	private final Thread threadListener;
	public NodeLink predecessor, successor;
	private final FingerTable fingerTable;
	private Server gRPC_listner;
	private int next = 1;


	public Chord(NodeLink address)
	{
		n = address;

		fingerTable = new FingerTable(mBit);

		predecessor = null;
		successor = n;

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

	private void listener()
	{
		var chord = this;

		try
		{
			gRPC_listner = ServerBuilder
					.forPort(n.port)
					.addService(gRPCCommander.getListnerServer(chord))
					.build()
					.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// ask getNode n to find id's getSuccessor
	public NodeLink find_successor(String id)
	{
		//if ((id > n.key && id <= successor.key))
		if (id.compareTo(n.key) > 0 && id.compareTo(successor.key) <= 0)
		{
			return successor;
		}
		else
		{
			var n_ = closest_preceding_finger(id);

//			if (n.equals(n_))
//				s = n;
//			else
			return gRPCCommander.gRPC_A(n_, Richiesta.FindSuccessor, id);
		}
	}

	// return closest fingerTable preceding id
	public NodeLink closest_preceding_finger(String id)
	{
		for (var i = mBit; i > 0; i--)
		{
			var ith_finger = fingerTable.getNode(i);

			if (ith_finger == null)
				continue;

			//if (ith_finger.key > n.key && ith_finger.key < id)
			if (ith_finger.key.compareTo(n.key) > 0 && ith_finger.key.compareTo(id) < 0)
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
			var s = gRPCCommander.gRPC_A(n_, Richiesta.FindSuccessor, n.key);
			successor = s;
			//successor = n_.find_successor(n);
		}

		startStabilizingRoutines();
	}

	// if s is iTh fingerTable of n, update n's fingerTable table with s
	public void update_finger_table(NodeLink s, int i) throws Exception
	{
		var ith_finger = fingerTable.getNode(i);

		if (ith_finger != null)
			//if (s.key >= n.key && s.key < ith_finger.key)
			if (s.key.compareTo(n.key) >= 0 && s.key.compareTo(ith_finger.key) < 0)
			{
				fingerTable.setNode(i, s);

				//get first getNode preceding n
				gRPCCommander.gRPC_E(predecessor, Richiesta.UpdateFingerTable, "", i, s);
				//p.update_finger_table(s, i);
			}
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize() throws Exception
	{
		var x = gRPCCommander.gRPC_A(successor, Richiesta.Predecessor);
		//var x = successor.predecessor;

		if (x != null)
		{
			//if (x.key > n.key && x.key < successor.key)
			if (x.key.compareTo(n.key) > 0 && x.key.compareTo(successor.key) < 0)
				successor = x;

			gRPCCommander.gRPC_E(successor, Richiesta.Notify, n);
			//successor.notify(n);
		}
	}

	// n_ thinks it might be our predecessor.
	public void notify(NodeLink n_)
	{
		//if (predecessor == null || (n_.key > predecessor.key && n_.key < n.key))
		if (predecessor == null || (n_.key.compareTo(predecessor.key) > 0 && n_.key.compareTo(n.key) < 0))
			predecessor = n_;
	}

	// called periodically refreshes fingerTable table entries.
	private void fix_fingers()
	{
		next++;

		if (next > mBit)
			next = 1;

		var d = GB.getPowerOfTwo(next - 1, mBit);
		//d += n.key;

		var iThFinger = find_successor("aaaaaaaaaaaaaaaaaa");

		fingerTable.setNode(next, iThFinger);

		//forse
		notify(iThFinger);
	}

	public void printDataStructure() throws Exception
	{
		System.out.println("\n==============================================================");
		System.out.println("\nLOCAL:\t\t\t\t" + n.toString() + "\t" + n);

		if (predecessor != null)
			System.out.println("\nPREDECESSOR:\t\t\t" + predecessor.toString() + "\t" + predecessor);
		else
			System.out.println("\nPREDECESSOR:\t\t\tNULL");

		System.out.println("\nFINGER TABLE:\n");

		for (var i = 1; i <= mBit; i++)
		{
			long ithstart = fingerTable.start(i);

			var f = fingerTable.getNode(i);

			var sb = new StringBuilder();

			sb.append(i + "\t" + ithstart + "\t\t");

			if (f != null)
				sb.append(f.toString() + "\t" + f);
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

		if ((predecessor == null || predecessor.equals(n)) && (successor == null || successor.equals(n)))
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

			if (successor != null)
				System.out.println("Your successor is getNode " + successor.IP + ", " + "port " + successor.port + ", position " + successor + ".");
			else
				System.out.println("Your successor is updating.");
		}
	}


}