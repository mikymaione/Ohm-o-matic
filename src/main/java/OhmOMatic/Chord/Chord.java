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
import OhmOMatic.Global.GB;
import OhmOMatic.ProtoBuffer.Common;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Chord
{
	private final char mBit = 32;
	private char next;

	private final Timer timersChord;

	private final NodeLink localNode;
	private NodeLink successor, predecessor;
	private final NodeLink[] finger = new NodeLink[mBit];


	public Chord(NodeLink address)
	{
		localNode = address;
		successor = address;
		predecessor = null;

		timersChord = new Timer();
	}

	// ask node n to find the successor of id
	public NodeLink find_successor(long id)
	{
		//Yes, that should be a closing square bracket to match the opening parenthesis.
		//It is a half closed interval.
		if (id > localNode.key && id <= successor.key)
		{
			return successor;
		}
		else
		{
			// forward the query around the circle
			var n0 = closest_preceding_node(id);

			return n0.find_successor(id);
		}
	}

	// search the local table for the highest predecessor of id
	private NodeLink closest_preceding_node(long id)
	{
		for (var i = mBit; i > 0; i--)
			if (finger[i].key > localNode.key && finger[i].key < id)
				return finger[i];

		return localNode;
	}

	//stabilize the chord ring/circle after node joins and departures
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

		timersChord.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					check_predecessor();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, new Date(), 500);
	}

	// join a Chord ring containing node n'.
	public void join(NodeLink n) throws Exception
	{
		predecessor = null;
		successor = gRPCCommander.requestAddress(n, Richiesta.FindSuccessor, localNode.key);

		startStabilizingRoutines();
	}

	// called periodically. n asks the successor
	// about its predecessor, verifies if n's immediate
	// successor is consistent, and tells the successor about n
	private void stabilize() throws Exception
	{
		var x = gRPCCommander.requestAddress(successor, Richiesta.Predecessor);

		if (x.key > localNode.key && x.key < successor.key)
			successor = x;

		successor.notify(localNode);
	}

	// n thinks it might be our predecessor.
	private void notify(NodeLink n)
	{
		if (predecessor == null || (n.key > predecessor.key && n.key < localNode.key))
			predecessor = n;
	}

	// called periodically. refreshes finger table entries.
	// next stores the index of the finger to fix
	private void fix_fingers()
	{
		next++;

		if (next > mBit)
			next = 1;

		var e = GB.getPowerOfTwo(next - 1, mBit);

		finger[next] = find_successor(e);
	}

	// called periodically. checks whether predecessor has failed.
	private void check_predecessor() throws Exception
	{
		if (predecessor != null)
		{
			var r = gRPCCommander.<Common.standardRes>sendRequest(predecessor, Richiesta.Ping);

			if (!r.getOk())
				setPredecessor(null);
		}
	}

	private synchronized void setPredecessor(NodeLink pre)
	{
		predecessor = pre;
	}


}