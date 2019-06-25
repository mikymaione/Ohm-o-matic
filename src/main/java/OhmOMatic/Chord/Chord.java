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

public class Chord
{
	private final char mBit = 32;

	private final Timer timersChord;

	private final NodeLink n;
	private final Thread threadListener;
	private NodeLink predecessor;
	private final FingerTable fingerTable;
	private Server gRPC_listner;


	public Chord(NodeLink address)
	{
		n = address;
		fingerTable = new FingerTable(mBit, address);
		
		timersChord = new Timer();

		threadListener = new Thread(() -> listener());
		threadListener.start();
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

	// ask node toE find	id's getSuccessor
	public NodeLink find_successor(long id)
	{
		var n_ = find_predecessor(id);

		return gRPCCommander.gRPC_A(n_, Richiesta.Successor);
		//return n_.successor;
	}

	// ask node n_ to find id's predecessor
	private NodeLink find_predecessor(long id)
	{
		var n_ = n;

		while (!(id > n_.key && id < gRPCCommander.gRPC_A(n_, Richiesta.Successor).key))
			n_ = gRPCCommander.gRPC_A(n_, Richiesta.ClosestPrecedingFinger, id);

		//while (!(id > n_.key && id < n_.successor.key))
		//	n_ = n_.closest_preceding_finger(id);

		return n_;
	}

	// return closest fingerTable preceding id
	public NodeLink closest_preceding_finger(long id)
	{
		for (var i = mBit - 1; i > 0; i--)
			if (fingerTable.node(i).key > n.key && fingerTable.node(i).key < id)
				return fingerTable.node(i);

		return n;
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
	}

	// node n joins the network;
	// n_ is an arbitrary node in the network
	public void join(NodeLink n_) throws Exception
	{
		predecessor = null;

		if (n_ == n)
			setSuccessor(n);
		else
			setSuccessor(gRPCCommander.gRPC_A(n_, Richiesta.FindSuccessor, n.key));
		//successor = n_.find_successor(n);

		startStabilizingRoutines();
	}

	// node n joins the network;
	// n_ is an arbitrary node in the network
	public void join2(NodeLink n_) throws Exception
	{
		if (n_ == null)
		{
			for (var i = 0; i < mBit; i++)
				fingerTable.setNode(i, n);

			predecessor = n;
		}
		else
		{
			init_finger_table(n_);
			update_others();
		}
	}

	public NodeLink getSuccessor()
	{
		return fingerTable.node(0);
	}

	private void setSuccessor(NodeLink s)
	{
		fingerTable.setNode(0, s);
	}

	public void setPredecessor(NodeLink p)
	{
		predecessor = p;
	}

	public NodeLink getPredecessor()
	{
		return predecessor;
	}

	// initialize fingerTable table of local node;
	// n_ is an arbitrary node already in the network
	private void init_finger_table(NodeLink n_) throws Exception
	{
		fingerTable.setNode(0, gRPCCommander.gRPC_A(n_, Richiesta.FindSuccessor, fingerTable.start(0)));
		//fingerTable.node[0] = n_.find_successor(fingerTable.start[0]);

		predecessor = gRPCCommander.gRPC_A(getSuccessor(), Richiesta.Predecessor);
		//predecessor = getSuccessor().predecessor;


		gRPCCommander.gRPC_E(getSuccessor(), Richiesta.SetPredecessor, n);
		//getSuccessor().predecessor = n;

		for (var i = 0; i < mBit - 1; i++)
			if (fingerTable.start(i + 1) >= n.key && fingerTable.start(i + 1) < fingerTable.node(i).key)
				fingerTable.setNode(i + 1, fingerTable.node(i));
			else
				//fingerTable.node[i + 1] = n_.find_successor(fingerTable.start[i + 1]);
				fingerTable.setNode(i + 1, gRPCCommander.gRPC_A(n_, Richiesta.FindSuccessor, fingerTable.start(i + 1)));
	}

	// update all nodes whose fingerTable
	// tables should refer toE n
	private void update_others() throws Exception
	{
		for (var i = 0; i < mBit; i++)
		{
			//find last node p whose iTh fingerTable might be n
			var p = find_predecessor(n.key - GB.getPowerOfTwo(i - 1, mBit));
			gRPCCommander.gRPC_E(p, Richiesta.UpdateFingerTable, i, n);
			//p.update_finger_table(n, i);
		}
	}

	// if s is iTh fingerTable of n, update n's fingerTable table with s
	public void update_finger_table(NodeLink s, int i) throws Exception
	{
		if (s.key >= n.key && s.key < fingerTable.node(i).key)
		{
			fingerTable.setNode(i, s);

			var p = predecessor; //get first node preceding n
			gRPCCommander.gRPC_E(p, Richiesta.UpdateFingerTable, i, s);
			//p.update_finger_table(s, i);
		}
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize() throws Exception
	{
		var x = gRPCCommander.gRPC_A(getSuccessor(), Richiesta.Predecessor);
		//var x = successor.predecessor;

		if (x.key > n.key && x.key < getSuccessor().key)
			setSuccessor(x);

		gRPCCommander.gRPC_E(getSuccessor(), Richiesta.Notify, n);
		//getSuccessor().notify(n);
	}

	// n_ thinks it might be our predecessor.
	public void notify(NodeLink n_)
	{
		if (predecessor == null || (n_.key > predecessor.key && n_.key < n.key))
			predecessor = n_;
	}

	// called periodically refreshes fingerTable table entries.
	private void fix_fingers()
	{
		var i = GB.randomInt(0, mBit - 1);
		fingerTable.setNode(i, find_successor(fingerTable.start(i)));
	}


}