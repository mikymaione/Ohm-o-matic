/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Implementazione in Java di Chord:
-In computing, Chord is a protocol and algorithm for a peer-to-peer distributed hash table. A distributed hash table stores key-value pairs by assigning keys to different computers (known as "nodes"); a node will store the values for all the keys for which it is responsible. Chord specifies how keys are assigned to nodes, and how a node can discover the value for a given key by first locating the node responsible for that key.
-Chord is one of the four original distributed hash table protocols, along with CAN, Tapestry, and Pastry. It was introduced in 2001 by Ion Stoica, Robert Morris, David Karger, Frans Kaashoek, and Hari Balakrishnan, and was developed at MIT.
-Fonte: https://en.wikipedia.org/wiki/Chord_(peer-to-peer)
*/
package OhmOMatic.Chord;

//region Imports

import OhmOMatic.Chord.FN.*;
import OhmOMatic.Global.GB;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
//endregion

public class Chord implements AutoCloseable
{

	//region Fields
	//====================================== DHT ======================================
	private final DHT dht = new DHT();
	//====================================== DHT ======================================


	//===================================== Chord =====================================
	//private static final Integer mBit = 160; //SHA1 versione normale
	private static final Integer mBit = 16; //versione semplificata
	private static final Integer _successorNumber = 1;
	private Integer next = 1;

	private final BigInteger keyListaPeers;
	private final NodeLink n;
	private NodeLink _predecessor;

	private final HashMap<Integer, NodeLink> _fingerTable;

	//timer per le funzioni di stabilizzazione
	private final Set<SleepingThread> stabilizingRoutines;
	//===================================== Chord =====================================


	//============================= Comunicazione di rete =============================
	//listner gRPC
	private Server gRPC_listner;
	//============================= Comunicazione di rete =============================
	//endregion

	//region Constructor & Destructors
	public Chord(final String _ip, final int _port) throws IOException
	{
		this(new NodeLink(_ip, _port));
	}

	public Chord(final NodeLink address) throws IOException
	{
		keyListaPeers = new BigInteger(GB.SHA1("ListaPeer"));

		n = address;

		_fingerTable = new HashMap<>(mBit);

		stabilizingRoutines = Set.of(
				new SleepingThread("stabilize", this::stabilize, 60),
				new SleepingThread("fix_fingers", this::fix_fingers, 500),
				new SleepingThread("check_predecessor", this::check_predecessor, 500),
				new SleepingThread("handoff", this::handoff, 1000)
		);

		setPredecessor(null);
		setSuccessor(n);

		start_gRPC_listner();
	}

	@Override
	public void close()
	{
		removeFromPeerList(keyListaPeers, n.ID);
		remove(n.ID);

		for (var t : stabilizingRoutines)
			t.stopMeGently();

		leave();

		gRPC_listner.shutdown();
	}
	//endregion

	//region Proprietà
	public Serializable removeFromPeerList(final BigInteger key, final Serializable object)
	{
		return _functionDHT(RichiestaDHT.removeFromPeerList, key, object);
	}

	public Serializable addToPeerList(final BigInteger key, final Serializable object)
	{
		return _functionDHT(RichiestaDHT.addToPeerList, key, object);
	}

	public HashSet<BigInteger> getPeerList()
	{
		var s = _functionDHT(RichiestaDHT.getPeerList, keyListaPeers, null);

		return (s instanceof HashSet ? (HashSet<BigInteger>) s : new HashSet<>());
	}

	public BigInteger getID()
	{
		return n.ID;
	}

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
	}
	//endregion

	//region Funzioni Chord
	// ask node n to find the successor of id
	public NodeLink find_successor(final BigInteger id)
	{
		final var s = getSuccessor();

		if (s != null)
			if (GB.incluso(id, n, s))
				return s;

		// forward the query around the circle
		final var n0 = closest_preceding_node(id);

		if (n.equals(n0))
			return n;

		//return n0.find_successor(id);
		return gRPC_Client.gRPC(n0, RichiestaChord.findSuccessor, id);
	}

	// search the local table for the highest predecessor of id
	private NodeLink closest_preceding_node(final BigInteger id)
	{
		for (Integer i = mBit; i > 0; i--)
		{
			final var iThFinger = getFinger(i);

			if (iThFinger != null)
				if (GB.incluso(iThFinger, n, id))
					return iThFinger;
		}

		return n;
	}

	// join a Chord ring containing node n_
	public void join(final String _ip, final int _port) throws Exception
	{
		join(new NodeLink(_ip, _port));
	}

	private void join(final NodeLink n_) throws Exception
	{
		if (!n.equals(n_))
		{
			setPredecessor(null);

			//successor = n_.find_successor(n);
			final var successor = gRPC_Client.gRPC(n_, RichiestaChord.findSuccessor, n.ID);

			if (successor == null)
				throw new Exception("Join fallito, " + n_ + " è irraggiungibile!");

			setSuccessor(successor);
		}

		addToPeerList(keyListaPeers, n.ID);

		startStabilizingRoutines();
	}
	//endregion

	//region DHT
	public Serializable remove(final BigInteger key)
	{
		return _functionDHT(RichiestaDHT.remove, key, null);
	}

	public Serializable get(final BigInteger key)
	{
		return _functionDHT(RichiestaDHT.get, key, null);
	}

	public Serializable put(final BigInteger key, final Serializable object)
	{
		return _functionDHT(RichiestaDHT.put, key, object);
	}

	public Serializable transfer(final BigInteger key, final Serializable object)
	{
		return dht.put(key, object);
	}

	private void handoff()
	{
		var daRimuovere = new ArrayList<BigInteger>();

		dht.forEachAndRemoveAll(e ->
		{
			final var n_ = find_successor(e.getKey());

			if (n_ != null && !n.equals(n_))
			{
				final var dL = gRPC_Client.gRPC(n_, RichiestaDHT.put, e.getKey(), e.getValue());

				if (!linkMorto(dL))
					daRimuovere.add(e.getKey());
			}
		}, daRimuovere);
	}

	private boolean linkMorto(Serializable dL)
	{
		return (dL instanceof DeadLink && ((DeadLink) dL).isDead);
	}

	private void leave()
	{
		var cercandoDestinatario = true;

		while (cercandoDestinatario)
		{
			final var s = getSuccessor();
			final var p = getPredecessor();

			final var ping = gRPC_Client.gRPC(s, RichiestaChord.ping);

			if (ping == null)
			{
				stabilize();
			}
			else
			{
				cercandoDestinatario = false;

				if (s != null && !n.equals(s) && p != null)
					dht.forEachAndClearAll(e ->
							gRPC_Client.gRPC(s, RichiestaDHT.transfer, e.getKey(), e.getValue()));
			}
		}
	}

	private Serializable _functionDHT(RichiestaDHT req, final BigInteger key, final Serializable object)
	{
		final var n_ = find_successor(key);

		if (n_ != null && !n.equals(n_))
			return gRPC_Client.gRPC(n_, req, key, object);
		else
			switch (req)
			{
				case getPeerList:
					return dht.getPeerList(key);
				case addToPeerList:
					return dht.addToPeerList(key, object);
				case removeFromPeerList:
					return dht.removeFromPeerList(key, object);

				case get:
					return dht.get(key);
				case put:
					return dht.put(key, object);
				case remove:
					return dht.remove(key);

				default:
					return null;
			}
	}
	//endregion


	//region Stabilizzazione
	//stabilize the chord ring/circle after getNode joins and departures
	private void startStabilizingRoutines()
	{
		for (var t : stabilizingRoutines)
			t.start();
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize()
	{
		//var x = successor.predecessor;
		var successor = getSuccessor();
		final var x = gRPC_Client.gRPC(successor, RichiestaChord.predecessor);

		if (linkMorto(x))
		{
			removeFinger(successor);

			successor = find_successor(n.ID);
			setSuccessor(successor);
		}

		if (x != null)
			if (GB.incluso(x, n, successor))
			{
				successor = x;
				setSuccessor(successor);
			}

		if (successor != null && !n.equals(successor))
			//successor.notify(n);
			gRPC_Client.gRPC(successor, RichiestaChord.notify, n);
	}

	private synchronized void removeFinger(NodeLink finger)
	{
		for (var e : _fingerTable.entrySet())
			if (finger.equals(e.getValue()))
				e.setValue(null);
	}

	// n_ thinks it might be our predecessor.
	public NodeLink notify(final NodeLink n_)
	{
		final var predecessor = getPredecessor();

		if (predecessor == null || (GB.incluso(n_, predecessor, n)))
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
		i = i.mod(GB.getPowerOfTwo(mBit, mBit));
		// n + 2^(next - 1)

		setFinger(next, find_successor(i));
	}

	// called periodically. checks whether predecessor has failed.
	private void check_predecessor()
	{
		final var predecessor = getPredecessor();

		if (predecessor != null)
		{
			final var vivo = gRPC_Client.gRPC(predecessor, RichiestaChord.ping);

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
	private void start_gRPC_listner() throws IOException
	{
		gRPC_listner = ServerBuilder
				.forPort(n.port)
				.addService(gRPC_Server.getListnerServer(this))
				.build()
				.start();
	}
	//endregion

	//region Print
	public void stampaTutto()
	{
		stampaStato();
		stampaFingerTable();
	}

	public void stampaFingerTable()
	{
		System.out.println("Finger Table:");

		for (Integer i = 1; i <= mBit; i++)
			System.out.println(i + ": " + getFinger(i));
	}

	public void stampaStato()
	{
		System.out.println("Nodo: " + n);
		System.out.println("Predecessor: " + getPredecessor());
		System.out.println("Successor: " + getSuccessor());
	}
	//endregion

}