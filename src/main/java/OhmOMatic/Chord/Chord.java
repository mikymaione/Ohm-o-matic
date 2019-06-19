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

	private final Timer timersChord;

	private final NodeLink localNode;
	private NodeLink predecessor;
	private final Finger[] finger = new Finger[mBit];


	public Chord(NodeLink address)
	{
		localNode = address;
		predecessor = null;

		timersChord = new Timer();
	}

	private NodeLink getSuccessor()
	{
		return finger[0];
	}

	// ask node to find	id's getSuccessor
	public NodeLink find_successor(long id)
	{
		var n_ = find_predecessor(id);

		return n_.successor;
	}

	// ask node to find	id's predecessor
	private NodeLink find_predecessor(long id)
	{
		var n_ = localNode;

		while (!(id > n_.key && id < n_.successor.key))
			n_ = n_.closest_preceding_finger(id);

		return n_;
	}

	// return closest finger preceding id
	private NodeLink closest_preceding_finger(long id)
	{
		for (var i = mBit; i > 0; i--)
			if (finger[i].node.key > localNode.key && finger[i].node.key < id)
				return finger[i].node;

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

	// node localNode joins the network;
	// n_ is an arbitrary node in the network
	public void join(NodeLink n_) throws Exception
	{
		predecessor = null;
		setSuccessor(n_.find_successor(localNode));
	}

	private void setSuccessor(NodeLink n)
	{
		finger[0] = new Finger(n);
	}

	// initialize finger table of local node;
	// n_ is an arbitrary node already in the network
	private void init_finger_table(NodeLink n_)
	{
		finger[0].node = n_.find_successor(finger[0].start);
		predecessor = getSuccessor().predecessor;
		getSuccessor().predecessor = localNode;

		for (var i = 0; i < mBit - 1; i++)
			if (finger[i + 1].start >= localNode.key && finger[i + 1].start < finger[i].node.key)
				finger[i + 1].node = finger[i].node;
			else
				finger[i + 1].node = n_.find_successor(finger[i + 1].start);
	}

	// update all nodes whose finger
	// tables should refer to localNode
	private void update_others()
	{
		for (int i = 0; i < mBit; i++)
		{
			//find last node p whose iTh finger might be localNode
			var p = find_predecessor(localNode.key - GB.getPowerOfTwo(i - 1, mBit));
			p.update_finger_table(localNode, i);
		}
	}

	// if s is iTh finger of localNode, update localNode's finger table with s
	private void update_finger_table(NodeLink s, int i)
	{
		if (s.key >= localNode.key && s < finger[i].node.key)
		{
			finger[i].node = s;

			var p = predecessor; //get first node preceding localNode
			p.update_finger_table(s, i);
		}
	}

	// called periodically. localNode asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize() throws Exception
	{
		var successor = getSuccessor();

		var x = gRPCCommander.requestAddress(successor, Richiesta.Predecessor);

		if (x.key > localNode.key && x.key < successor.key)
			successor = x;

		successor.notify(localNode);
	}

	// n_ thinks it might be our predecessor.
	private void notify(NodeLink n_)
	{
		if (predecessor == null || (n_.key > predecessor.key && n_.key < localNode.key))
			predecessor = n_;
	}

	// called periodically refreshes finger table entries.
	private void fix_fingers()
	{
		var i = GB.randomInt(0, mBit - 1);
		
		finger[i].node = find_successor(Finger.start(localNode, i, mBit));
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