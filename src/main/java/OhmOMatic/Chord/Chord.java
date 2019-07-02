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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Timer;

public class Chord implements AutoCloseable
{
	private static final Integer mBit = 160; //SHA1
	private static final Integer _successorNumber = 1;
	private Integer next = 1;

	private final NodeLink n;
	private NodeLink _predecessor;

	private final HashMap<Integer, NodeLink> _fingerTable;

	private Server gRPC_listner;
	private final Thread threadListener;
	private final Timer timersChord;


	public Chord(final NodeLink address)
	{
		n = address;

		_fingerTable = new HashMap<>(mBit);

		setPredecessor(null);
		setSuccessor(n);

		timersChord = new Timer();

		threadListener = new Thread(this::listener);
		threadListener.start();
	}

	@Override
	public void close()
	{
		timersChord.cancel();
		threadListener.stop();
		gRPC_listner.shutdown();
	}


	//region Proprietà
	private NodeLink getSuccessor()
	{
		return getFinger(_successorNumber);
	}

	private void setSuccessor(final NodeLink n_)
	{
		setFinger(_successorNumber, n_);
	}

	public synchronized NodeLink getPredecessor()
	{
		return _predecessor;
	}

	private synchronized void setPredecessor(final NodeLink n_)
	{
		_predecessor = n_;
	}

	private synchronized NodeLink getFinger(final Integer i)
	{
		return _fingerTable.get(i);
	}

	private synchronized void setFinger(final Integer i, final NodeLink n_)
	{
		_fingerTable.put(i, n_);

		// if the updated one is successor, notify the new successor
		if (i == 1 && n_ != null && !n_.equals(n))
			notify(n_);
	}
	//endregion

	//region Funzioni Chord
	// ask node n to find the successor of id
	public NodeLink find_successor(final BigInteger id)
	{
		var s = getSuccessor();

		//Yes, that should be a closing square bracket to match the opening parenthesis.
		//It is a half closed interval.

		//id ∈ (n, successor)
		if (GB.incluso(id, n, s))
		{
			return s;
		}
		else
		{
			// forward the query around the circle
			var n0 = closest_preceding_node(id);

			if (n0.equals(n))
				return n;

			return gRPC_Client.gRPC(n0, Richiesta.findSuccessor, id);
		}
	}

	// search the local table for the highest predecessor of id
	private NodeLink closest_preceding_node(final BigInteger id)
	{
		for (Integer i = mBit; i > 0; i--)
		{
			var ith_finger = getFinger(i);

			if (ith_finger != null)
			{
				//finger[i] ∈ (n, id)
				if (GB.incluso(ith_finger, n, id))
					return ith_finger;
			}
		}

		return n;
	}

	// join a Chord ring containing node n_
	public void join(final NodeLink n_)
	{
		if (!n.equals(n_))
		{
			setPredecessor(null);

			var s = gRPC_Client.gRPC(n_, Richiesta.findSuccessor, n.ID);
			setSuccessor(s);
			//successor = n_.find_successor(n);
		}

		startStabilizingRoutines();
	}
	//endregion

	//region Stabilizzazione

	//stabilize the chord ring/circle after getNode joins and departures
	private void startStabilizingRoutines()
	{
		GB.executeTimerTask(timersChord, 60, this::stabilize);
		GB.executeTimerTask(timersChord, 500, this::fix_fingers);
		GB.executeTimerTask(timersChord, 500, this::check_predecessor);
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize()
	{
		var x = gRPC_Client.gRPC(getSuccessor(), Richiesta.predecessor);
		//var x = successor.predecessor;

		if (x != null)
			//x ∈ (n, successor)
			if (GB.incluso(x, n, getSuccessor()))
				setSuccessor(x);

		if (!n.equals(getSuccessor()))
			gRPC_Client.gRPC(getSuccessor(), Richiesta.notify, n);
		//successor.notify(n);
	}

	// n_ thinks it might be our predecessor.
	public NodeLink notify(final NodeLink n_)
	{
		//predecessor is nil or n_ ∈ (predecessor, n)
		if (getPredecessor() == null || (GB.incluso(n_, getPredecessor(), n)))
		{
			setPredecessor(n_);

			return n_;
		}
		else
		{
			return null;
		}
	}

	// called periodically. refreshes finger table entries.
	// next stores the index of the finger to fix
	private void fix_fingers()
	{
		next++;

		if (next > mBit)
			next = 2;

		// n + 2^(next - 1)
		var i = GB.getPowerOfTwo(next - 1, mBit);
		i = n.ID.add(i);
		i = i.mod(GB.getPowerOfTwo(mBit, mBit)); // da fare?

		var iThFinger = find_successor(i);

		setFinger(next, iThFinger);
	}

	// called periodically. checks whether predecessor has failed.
	private void check_predecessor()
	{
		var predecessor = getPredecessor();

		if (predecessor != null)
		{
			var vivo = gRPC_Client.gRPC(predecessor, Richiesta.ping);

			if (vivo == null)
				setPredecessor(null);
		}
	}

	public NodeLink ping()
	{
		return n;
	}
	//endregion

	//region Server
	private void listener()
	{
		try
		{
			gRPC_listner = ServerBuilder
					.forPort(n.port)
					.addService(gRPC_Server.getListnerServer(this))
					.build()
					.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	//endregion

	//region Print
	public void printDataStructure()
	{
		GB.clearScreen();
		System.out.println("\n==============================================================");
		System.out.println("\nLOCAL:\t\t\t\t" + n.toString() + "\t");

		if (getPredecessor() != null)
			System.out.println("\nPREDECESSOR:\t\t\t" + getPredecessor().toString() + "\t");
		else
			System.out.println("\nPREDECESSOR:\t\t\tNULL");

		System.out.println("\nFINGER TABLE:\n");

		for (Integer i = 1; i <= mBit; i++)
		{
			var f = getFinger(i);

			var sb = new StringBuilder();

			sb.append(i + "\t" + "\t\t");

			if (f != null)
				sb.append(f + "\t");

			else
				sb.append("NULL");

			System.out.println(sb.toString());
		}

		System.out.println("\n==============================================================\n");
	}

	public void printNeighbors()
	{
		GB.clearScreen();
		System.out.println("You are listening on port " + n.port + ".");
		System.out.println("Your position is " + n + ".");

		if ((getPredecessor() == null || getPredecessor().equals(n)) && (getSuccessor() == null || getSuccessor().equals(n)))
		{
			System.out.println("Your predecessor is yourself.");
			System.out.println("Your successor is yourself.");
		}
		else
		{
			if (getPredecessor() != null)
				System.out.println("Your predecessor is node " + getPredecessor().IP + ", " + "port " + getPredecessor().port + ", position " + getPredecessor() + ".");
			else
				System.out.println("Your predecessor is updating.");

			if (getSuccessor() != null)
				System.out.println("Your successor is node " + getSuccessor().IP + ", " + "port " + getSuccessor().port + ", position " + getSuccessor() + ".");
			else
				System.out.println("Your successor is updating.");
		}
	}
	//endregion

}