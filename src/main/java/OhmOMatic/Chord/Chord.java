/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Implementazione in Java di Chord:
-In computing, Chord is a protocol and algorithm for a peer-to-peer distributed hash table. A distributed hash table stores key-value pairs by assigning keys to different computers (known as "nodes"); a node will store the values for all the keys for which it is responsible. Chord specifies how keys are assigned to nodes, and how a node can discover the value for a given key by first locating the node responsible for that key.
-Chord is one of the four original distributed hash table protocols, along with CAN, Tapestry, and Pastry. It was introduced in 2001 by Ion Stoica, Robert Morris, David Karger, Frans Kaashoek, and Hari Balakrishnan, and was developed at MIT.
-Fonte: https://en.wikipedia.org/wiki/Chord_(peer-to-peer)

// ask node n to find the successor of id
n.find_successor(id)
	//Yes, that should be a closing square bracket to match the opening parenthesis.
	//It is a half closed interval.
	if (id ∈ (n, successor])
		return successor;
	else
		// forward the query around the circle
		n0 = closest_preceding_node(id);
	return n0.find_successor(id);

// search the local table for the highest predecessor of id
n.closest_preceding_node(id)
	for i = m downto 1
		if (finger[i] ∈ (n,id))
			return finger[i];
	return n;

// create a new Chord ring.
n.create()
	predecessor = nil;
	successor = n;

// join a Chord ring containing node n'.
n.join(n')
	predecessor = nil;
	successor = n'.find_successor(n);

// called periodically. n asks the successor
// about its predecessor, verifies if n's immediate
// successor is consistent, and tells the successor about n
n.stabilize()
	x = successor.predecessor;
	if (x ∈ (n, successor))
		successor = x;
	successor.notify(n);

// n' thinks it might be our predecessor.
n.notify(n')
	if (predecessor is nil or n' ∈ (predecessor, n))
		predecessor = n';

// called periodically. refreshes finger table entries.
// next stores the index of the finger to fix
n.fix_fingers()
	next = next + 1;
	if (next > m)
		next = 1;
	finger[next] = find_successor(n + 2^(next - 1));

// called periodically. checks whether predecessor has failed.
n.check_predecessor()
	if (predecessor has failed)
		predecessor = nil;
*/
package OhmOMatic.Chord;

//region Imports

import OhmOMatic.Chord.Enums.RichiestaChord;
import OhmOMatic.Chord.Enums.RichiestaDHT;
import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Chord.Threads.SleepingThread;
import OhmOMatic.Chord.gRPC.gRPC_Client;
import OhmOMatic.Chord.gRPC.gRPC_Server;
import OhmOMatic.Global.GB;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
//endregion

public class Chord implements AutoCloseable
{

	//region Fields
	//====================================== DHT ======================================
	private final DHT dht;
	//====================================== DHT ======================================


	//===================================== Chord =====================================
	//private static final Integer mBit = 160; //SHA1 versione normale
	private static final Integer mBit = 8; //versione semplificata
	private Integer next = 1;

	private final BigInteger keyListaPeers;
	private final NodeLink n;

	private final Object _predecessorLock = new Object();
	private NodeLink _predecessor;

	private final Integer _successorFingerNumber = 1;
	private final HashMap<Integer, NodeLink> _fingerTable;

	//timer per le funzioni di stabilizzazione
	private final Set<SleepingThread> stabilizingRoutines;
	//===================================== Chord =====================================


	//============================= Comunicazione di rete =============================
	//listner gRPC
	private final Server gRPC_listner;
	//============================= Comunicazione di rete =============================
	//endregion

	//region Constructor & Destructors
	public Chord(final String _ip, final int _port) throws IOException
	{
		this(new NodeLink(_ip, _port));
	}

	public Chord(final NodeLink address) throws IOException
	{
		n = address;

		_fingerTable = new HashMap<>(mBit);

		keyListaPeers = new BigInteger(GB.SHA1("Chiave speciale per lista dei peer"));
		dht = new DHT(keyListaPeers);

		stabilizingRoutines = Set.of(
				new SleepingThread("stabilize", this::stabilize, 60),
				new SleepingThread("fix_fingers", this::fix_fingers, 500),
				new SleepingThread("check_predecessor", this::check_predecessor, 500),
				new SleepingThread("handoff", this::handoff, 1000)
		);

		setPredecessor(null);
		setSuccessor(n);

		gRPC_listner = ServerBuilder
				.forPort(n.port)
				.addService(gRPC_Server.getListnerServer(this))
				.build()
				.start();
	}

	@Override
	public void close()
	{
		removeFromPeerList(keyListaPeers, n.ID);
		remove(n.ID);

		for (var routine : stabilizingRoutines)
			routine.stopMeGently();

		gRPC_listner.shutdown();

		leave();
	}
	//endregion

	//region Proprietà
	public BigInteger getID()
	{
		return n.ID;
	}

	private NodeLink getSuccessor()
	{
		return getFinger(_successorFingerNumber);
	}

	private void setSuccessor(final NodeLink n_)
	{
		setFinger(_successorFingerNumber, n_);
	}

	public NodeLink getPredecessor()
	{
		synchronized (_predecessorLock)
		{
			return _predecessor;
		}
	}

	private void setPredecessor(final NodeLink n_)
	{
		synchronized (_predecessorLock)
		{
			_predecessor = n_;
		}
	}

	private NodeLink getFinger(final Integer i)
	{
		synchronized (_fingerTable)
		{
			return _fingerTable.get(i);
		}
	}

	private void setFinger(final Integer i, final NodeLink n_)
	{
		synchronized (_fingerTable)
		{
			_fingerTable.put(i, n_);
		}
	}
	//endregion

	//region Funzioni Chord
	// ask node n to find the successor of id
	public NodeLink find_successor(final BigInteger id)
	{
		var successor = getSuccessor();

		//if (id ∈ (n, successor])
		if (GB.inclusoR(id, n, successor))
			return successor;

		// forward the query around the circle
		var n0 = closest_preceding_node(id);

		//return n0.find_successor(id);
		return gRPC_Client.gRPC(n0, RichiestaChord.findSuccessor, id);
	}

	// search the local table for the highest predecessor of id
	private NodeLink closest_preceding_node(final BigInteger id)
	{
		synchronized (_fingerTable)
		{
			for (Integer i = mBit; i > 0; i--)
			{
				var iThFinger = _fingerTable.get(i);

				if (iThFinger != null)
					if (GB.incluso(iThFinger, n, id))
						return iThFinger;
			}

			return n;
		}
	}

	// join a Chord ring containing node n_
	public void join(final String _ip, final int _port) throws Exception
	{
		join(new NodeLink(_ip, _port));
	}

	private void join(final NodeLink n_) throws Exception
	{
		if (n.equals(n_))
		{
			dht.createPeerList();
			System.out.println("Creata peer list");
		}
		else
		{
			setPredecessor(null);

			//successor = n_.find_successor(n);
			final var successor = gRPC_Client.gRPC(n_, RichiestaChord.findSuccessor, n.ID);

			if (successor == null)
				throw new Exception("Join fallito, " + n_ + " è irraggiungibile!");

			setSuccessor(successor);
		}

		var addedToPeerList = addToPeerList(keyListaPeers, n.ID);
		System.out.println("Aggiunta a lista peer: " + addedToPeerList);

		startStabilizingRoutines();
	}
	//endregion

	//region Peer List
	public Serializable removeFromPeerList(final BigInteger key, final Serializable object)
	{
		return _functionDHT(RichiestaDHT.removeFromPeerList, key, object);
	}

	public Serializable addToPeerList(final BigInteger key, final Serializable object)
	{
		return _functionDHT(RichiestaDHT.addToPeerList, key, object);
	}

	public BigInteger[] getPeerList()
	{
		return (BigInteger[]) _functionDHT(RichiestaDHT.getPeerList, keyListaPeers, null);
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

	private void leave()
	{
		var ciSonoElementiDaTrasferire = false;

		do
		{
			final var successor = getSuccessor();
			final var predecessor = getPredecessor();

			if (!n.equals(successor) && predecessor != null)
			{
				final var data = dht.getData();

				ciSonoElementiDaTrasferire = (data.length > 0);

				for (var i : data)
					try
					{
						gRPC_Client.gRPC(successor, RichiestaDHT.transfer, i.getKey(), i.getValue());
					}
					catch (StatusRuntimeException e)
					{
						System.out.println("leave: Nodo " + successor + " non raggiungibile!");
					}
					catch (IOException e)
					{
						System.out.println("Errore serializzazione oggetto!");
						e.printStackTrace();
					}
					catch (ClassNotFoundException e)
					{
						System.out.println("Classe SHA1 non trovata!");
						e.printStackTrace();
					}
			}
		}
		while (ciSonoElementiDaTrasferire);
	}

	private Serializable _functionDHT(final RichiestaDHT req, final BigInteger key, final Serializable object)
	{
		final var n_ = find_successor(key);

		System.out.println("[" + GB.DateToString() + "] " + n_ + " > DHT." + req + ": " + key + "=" + object);

		if (n_ == null || n.equals(n_))
			switch (req)
			{
				case getPeerList:
					return dht.getPeerList();
				case addToPeerList:
					return dht.addToPeerList(object);
				case removeFromPeerList:
					return dht.removeFromPeerList(object);

				case get:
					return dht.get(key);
				case put:
					return dht.put(key, object);
				case remove:
					return dht.remove(key);
			}
		else
			try
			{
				return gRPC_Client.gRPC(n_, req, key, object);
			}
			catch (StatusRuntimeException e)
			{
				System.out.println("DHT: Nodo " + n_ + " non raggiungibile!");
				return _functionDHT(req, key, object);
			}
			catch (IOException e)
			{
				System.out.println("Errore serializzazione oggetto!");
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				System.out.println("Classe SHA1 non trovata!");
				e.printStackTrace();
			}

		return null;
	}
	//endregion


	//region Stabilizzazione
	//stabilize the chord ring/circle after getNode joins and departures
	private void startStabilizingRoutines()
	{
		for (var routine : stabilizingRoutines)
			routine.start();
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize()
	{
		//var x = successor.predecessor;
		var x = gRPC_Client.gRPC(getSuccessor(), RichiestaChord.predecessor);

		if (x != null)
			//x ∈ (n, successor)
			if (GB.incluso(x, n, getSuccessor()))
				setSuccessor(x);

		//successor.notify(n);
		gRPC_Client.gRPC(getSuccessor(), RichiestaChord.notify, n);
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

		return null;
	}

	// called periodically. refreshes finger table entries.
	// next stores the index of the finger to fix
	private void fix_fingers()
	{
		next++;

		if (next > mBit)
			next = 1;

		// n + 2^(next - 1)
		var i = GB.getPowerOfTwo(next - 1, mBit);
		i = n.ID.add(i);
		i = i.mod(GB.getPowerOfTwo(mBit, mBit));

		//final var successor = gRPC_Client.gRPC(find_successor(i), RichiestaChord.ping);
		final var successor = find_successor(i);
		setFinger(next, successor);
	}

	// called periodically. checks whether predecessor has failed.
	private void check_predecessor()
	{
		final var predecessor_vivo = gRPC_Client.gRPC(getPredecessor(), RichiestaChord.ping);

		if (predecessor_vivo == null)
			setPredecessor(null);
	}

	// called periodically. handoff my data to the correct peer
	private void handoff()
	{
		final var daFare = new Stack<BigInteger>();
		final var daRimuovere = new Stack<BigInteger>();

		do
		{
			daFare.clear();
			final var data = dht.getData();

			for (var e : data)
			{
				final var n_ = find_successor(e.getKey());

				if (n_ != null && !n.equals(n_))
					try
					{
						daFare.push(e.getKey());

						final var risultatoTrasferimento = gRPC_Client.gRPC(n_, RichiestaDHT.put, e.getKey(), e.getValue());

						if (Boolean.TRUE.equals(risultatoTrasferimento))
							daRimuovere.push(e.getKey());
					}
					catch (StatusRuntimeException ex)
					{
						System.out.println("Handoff: Nodo " + n_ + " non raggiungibile!");
					}
					catch (IOException ex)
					{
						System.out.println("Errore serializzazione oggetto!");
						ex.printStackTrace();
					}
					catch (ClassNotFoundException ex)
					{
						System.out.println("Classe SHA1 non trovata!");
						ex.printStackTrace();
					}
			}
		}
		while (daFare.size() > daRimuovere.size());

		dht.removeAll(daRimuovere);
	}

	public NodeLink ping()
	{
		return n;
	}
	//endregion

	//region Print
	public void stampaTutto()
	{
		stampaStato();
		stampaFingerTable();
		stampaData();
		stampaPeerList();
	}

	private void stampaData()
	{
		System.out.println("My data:");

		for (var data : dht.getData())
			System.out.println(data.getKey() + " > " + data.getValue());
	}

	private void stampaPeerList()
	{
		final var peerList = getPeerList();

		if (peerList != null)
		{
			System.out.println("Peer list:");

			for (var peer : peerList)
				System.out.println(peer);
		}
	}

	private void stampaFingerTable()
	{
		System.out.println("Finger Table:");

		for (Integer i = 1; i <= mBit; i++)
			System.out.println(i + ": " + getFinger(i));
	}

	private void stampaStato()
	{
		System.out.println("Nodo: " + n);
		System.out.println("Predecessor: " + getPredecessor());
		System.out.println("Successor: " + getSuccessor());
	}
	//endregion

}