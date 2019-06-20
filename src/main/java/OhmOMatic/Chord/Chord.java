/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.Chord.FN.HomeFastStub;
import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Chord.FN.Richiesta;
import OhmOMatic.Global.GB;
import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Chord
{
	private final char mBit = 32;

	private final Timer timersChord;

	private final NodeLink n;
	private NodeLink predecessor;
	private final FingerTable fingerTable = new FingerTable(mBit);


	private void gestioneErroreRequest(Common.standardRes R) throws Exception
	{
		if (R == null)
			throw new Exception("Errore richiesta!");

		if (!R.getOk())
			throw new Exception(R.getErrore());
	}

	private NodeLink gRPC_A(NodeLink server, Richiesta req) throws Exception
	{
		Home.casaRes response = gRPC_E(server, req);

		if (response == null)
		{
			return null;
		}
		else if (!response.getStandardRes().getOk())
		{
			return server;
		}
		else
		{
			var c = response.getCasa();

			return new NodeLink(c.getIP(), c.getPort());
		}
	}

	private <A> A gRPC_E(NodeLink server, Richiesta req) throws Exception
	{
		try (var hfs = new HomeFastStub())
		{
			var stub = hfs.getStub(server);

			var c = Home.casa.newBuilder()
					.setIP(server.IP)
					.setPort(server.port)
					.build();

			Home.casaRes CR;
			Common.standardRes R;

			switch (req)
			{
				case join:
					throw new UnsupportedOperationException();

				case esciDalCondominio:
					throw new UnsupportedOperationException();

				case Ping:
					R = stub.kEEP(c);
					gestioneErroreRequest(R);
					return (A) R;

				case ClosestPrecedingFinger:
					CR = stub.cLOSEST(c);
					break;

				case FindSuccessor:
					CR = stub.fINDSUCC(c);
					break;

				case ImPredecessor:
					CR = stub.iAMPRE(c);
					break;

				case Predecessor:
					CR = stub.yOURPRE(c);
					break;

				case Successor:
					CR = stub.yOURSUCC(c);
					break;

				default:
					throw new Exception("Switch " + req + " non implementato");
			}

			R = CR.getStandardRes();
			gestioneErroreRequest(R);

			return (A) CR;
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public Chord(NodeLink address)
	{
		n = address;
		predecessor = null;

		timersChord = new Timer();
	}

	// ask node toE find	id's getSuccessor
	public NodeLink find_successor(long id)
	{
		var n_ = find_predecessor(id);

		return n_.successor;
	}

	// ask node toE find	id's predecessor
	private NodeLink find_predecessor(long id)
	{
		var n_ = n;

		while (!(id > n_.key && id < n_.successor.key))
			n_ = n_.closest_preceding_finger(id);

		return n_;
	}

	// return closest fingerTable preceding id
	private NodeLink closest_preceding_finger(long id)
	{
		for (var i = mBit; i > 0; i--)
			if (fingerTable.node[i].key > n.key && fingerTable.node[i].key < id)
				return fingerTable.node[i];

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
		successor = n_.find_successor(n);

		startStabilizingRoutines();
	}

	// initialize fingerTable table of local node;
	// n_ is an arbitrary node already in the network
	private void init_finger_table(NodeLink n_)
	{
		fingerTable.node[0] = n_.find_successor(fingerTable.start[0]);
		predecessor = getSuccessor().predecessor;
		getSuccessor().predecessor = n;

		for (var i = 0; i < mBit - 1; i++)
			if (fingerTable.start[i + 1] >= n.key && fingerTable.start[i + 1] < fingerTable.node[i].key)
				fingerTable.node[i + 1] = fingerTable.node[i];
			else
				fingerTable.node[i + 1] = n_.find_successor(fingerTable.start[i + 1]);
	}

	// update all nodes whose fingerTable
	// tables should refer toE n
	private void update_others()
	{
		for (int i = 0; i < mBit; i++)
		{
			//find last node p whose iTh fingerTable might be n
			var p = find_predecessor(n.key - GB.getPowerOfTwo(i - 1, mBit));
			p.update_finger_table(n, i);
		}
	}

	// if s is iTh fingerTable of n, update n's fingerTable table with s
	private void update_finger_table(NodeLink s, int i)
	{
		if (s.key >= n.key && s.key < fingerTable.node[i].key)
		{
			fingerTable.node[i] = s;

			var p = predecessor; //get first node preceding n
			p.update_finger_table(s, i);
		}
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize() throws Exception
	{
		var x = successor.predecessor;

		if (x.key > n.key && x.key < successor.key)
			successor = x;

		successor.notify(n);
	}

	// n_ thinks it might be our predecessor.
	private void notify(NodeLink n_)
	{
		if (predecessor == null || (n_.key > predecessor.key && n_.key < n.key))
			predecessor = n_;
	}

	// called periodically refreshes fingerTable table entries.
	private void fix_fingers()
	{
		var i = GB.randomInt(0, mBit - 1);
		fingerTable.node[i] = find_successor(fingerTable.start[i]);
	}


}