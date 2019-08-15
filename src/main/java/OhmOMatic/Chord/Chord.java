/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Implementazione in Java di Chord:
-In computing, Chord is a protocol and algorithm for a peer-to-peer distributed hash table. A distributed hash table stores key-value pairs by assigning keys to different computers (known as "nodes"); a node will store the values for all the keys for which it is responsible. Chord specifies how keys are assigned to nodes, and how a node can discover the value for a given key by first locating the node responsible for that key.
-Chord is one of the four original distributed hash table protocols, along with CAN, Tapestry, and Pastry. It was introduced in 2001 by Ion Stoica, Robert Morris, David Karger, Frans Kaashoek, and Hari Balakrishnan, and was developed at MIT.
-Fonte: http://en.wikipedia.org/wiki/Chord_(peer-to-peer)
*/
package OhmOMatic.Chord;

//region Imports

import OhmOMatic.Chord.Enums.RichiestaChord;
import OhmOMatic.Chord.Enums.RichiestaDHT;
import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Chord.gRPC.gRPC_Client;
import OhmOMatic.Global.GB;
import OhmOMatic.Global.Waiter;
import OhmOMatic.RicartAgrawala.MutualExclusion;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
//endregion

public class Chord implements AutoCloseable
{

	//region Fields
	//=============================== Mutual Exclusion ================================
	private final MutualExclusion mutualExclusion;
	//=============================== Mutual Exclusion ================================

	//====================================== DHT ======================================
	// S(N): O(㏒ N)
	private final DHT dht;
	//====================================== DHT ======================================


	//===================================== Chord =====================================
	private Integer next = 1;

	private final BigInteger keyListaPeers;
	private final NodeLink n;

	private final Object _predecessorLock = new Object();
	private NodeLink _predecessor;

	private final Integer _successorFingerNumber = 1;
	private final HashMap<Integer, NodeLink> _fingerTable;

	//timer per le funzioni di stabilizzazione
	private final Set<Waiter> stabilizingRoutines;

	private static final Integer _sleepTime = 555;
	//===================================== Chord =====================================


	//============================= Comunicazione di rete =============================
	//listner gRPC
	private final Server gRPC_listner;
	//============================= Comunicazione di rete =============================
	//endregion

	//region Constructor & Destructors
	public Chord(final String identificatore, final String ip, final int port) throws IOException
	{
		this(new NodeLink(identificatore, ip, port));
	}

	public Chord(final NodeLink address) throws IOException
	{
		n = address;

		_fingerTable = new HashMap<>(GB.numberOfFingers);

		keyListaPeers = GB.SHA("Chiave speciale per lista dei peer");
		dht = new DHT(keyListaPeers);

		stabilizingRoutines = Set.of(
				new Waiter("stabilize", this::stabilize, 60),
				new Waiter("fix_fingers", this::fix_fingers, 500),
				new Waiter("check_predecessor", this::check_predecessor, 500),
				new Waiter("handoff", this::handoff, 1000)
		);

		setPredecessor(null);
		setSuccessor(n);

		mutualExclusion = new MutualExclusion(2, n, this);

		gRPC_listner = ServerBuilder
				.forPort(n.port)
				.addService(OhmOMatic.Chord.gRPC.gRPC_Server.getListnerServer(this))
				.addService(OhmOMatic.RicartAgrawala.gRPC.gRPC_Server.getListnerServer(mutualExclusion))
				.build()
				.start();
	}

	@Override
	public void close()
	{
		removeFromPeerList(keyListaPeers, n);
		remove(n.ID);

		for (var stabilizingRoutine : stabilizingRoutines)
			stabilizingRoutine.stopMeGently();

		for (var stabilizingRoutine : stabilizingRoutines)
			while (stabilizingRoutine.isRunning())
				GB.sleep(_sleepTime);

		gRPC_listner.shutdown();

		System.out.println("Trasferimento dati ad altri peers...");
		leave();
		System.out.println("Trasferimento dati ad altri peers terminato!");
	}
	//endregion

	//region Proprietà
	public NodeLink getNodeLink()
	{
		return n;
	}

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

	private NodeLink[] getFingerList()
	{
		synchronized (_fingerTable)
		{
			var lista = new NodeLink[GB.numberOfFingers];

			for (var i = 0; i < GB.numberOfFingers; i++)
				lista[i] = _fingerTable.get(i + 1);

			return lista;
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

	// T(N): O(㏒ N)
	// ask node n to find the successor of id
	public NodeLink find_successor(final BigInteger id)
	{
		final var successor = getSuccessor();

		//if (id ∈ (n, successor])
		if (successor != null)
			if (GB.incluso(id, n, successor))
				return successor;

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
		synchronized (_fingerTable)
		{
			for (Integer i = GB.numberOfFingers; i > 0; i--)
			{
				final var iThFinger = _fingerTable.get(i);

				if (iThFinger != null && !n.equals(iThFinger))
					if (GB.finger_incluso(iThFinger, n, id))
					{
						final var vivo = gRPC_Client.gRPC(iThFinger, RichiestaChord.ping);

						if (vivo == null)
						{
							_fingerTable.put(i, null);
							continue;
						}

						return iThFinger;
					}
			}

			return n;
		}
	}

	// T(N): O(㏒² N)
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

		final var addedToPeerList = addToPeerList(keyListaPeers, n);
		System.out.println("Aggiunta a lista peer: " + addedToPeerList);

		put(n.ID, BigInteger.ZERO);

		startStabilizingRoutines();
	}

	// T(N): O(㏒² N)
	private void leave()
	{
		final var daFare = new HashSet<BigInteger>();
		final var daRimuovere = new HashSet<BigInteger>();
		final var successor = getSuccessor();

		if (!n.equals(successor))
			do
			{
				daFare.clear();

				dht.forEachAndRemoveAll(e ->
				{
					var n_ = find_successor(e.getKey());

					if (n.equals(n_))
						n_ = successor;

					try
					{
						final var info = "[" + GB.DateToString() + "] leave > " + n_ + ": " + e.getKey() + "=" + e.getValue();
						System.out.println(info);

						daFare.add(e.getKey());
						final var risultatoTrasferimento = gRPC_Client.gRPC(n_, RichiestaDHT.transfer, e.getKey(), e.getValue());

						if (Boolean.TRUE.equals(risultatoTrasferimento))
						{
							daRimuovere.add(e.getKey());
							System.out.println(info + " OK!");
						}
						else
							System.out.println(info + " KO!");
					}
					catch (StatusRuntimeException ex)
					{
						System.out.println("leave: Nodo " + successor + " non raggiungibile!");
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
				}, daRimuovere);
			}
			while (daFare.size() > 0);
	}
	//endregion

	//region Peer List
	public Boolean removeFromPeerList(final BigInteger key, final Serializable object)
	{
		while (true)
		{
			final var r = _functionDHT(RichiestaDHT.removeFromPeerList, key, object);

			if (Boolean.TRUE.equals(r))
				return true;
			else
				GB.sleep(_sleepTime);
		}
	}

	public Boolean addToPeerList(final BigInteger key, final Serializable object)
	{
		while (true)
		{
			final var r = _functionDHT(RichiestaDHT.addToPeerList, key, object);

			if (Boolean.TRUE.equals(r))
				return true;
			else
				GB.sleep(_sleepTime);
		}
	}

	public NodeLink[] getPeerList()
	{
		while (true)
		{
			final var r = _functionDHT(RichiestaDHT.getPeerList, keyListaPeers, null);

			if (r == null)
				GB.sleep(_sleepTime);
			else
				return (NodeLink[]) r;
		}
	}
	//endregion

	//region DHT
	public synchronized void putIncremental(final Serializable object)
	{
		final var curNumero = incBigInteger(n.ID);
		final var chiave = n.ID.add(curNumero);

		put(chiave, object);
	}

	public synchronized Serializable[] getIncrementals(final BigInteger key, final BigInteger lastNumero, final BigInteger curNumero)
	{
		final var from_ = lastNumero.intValue();
		final var to_ = curNumero.intValue();

		if (to_ - from_ < 0)
			return null;

		final var lista = new Serializable[to_ - from_];

		var x = -1;
		for (var i = from_; i < to_; i++)
		{
			final var new_key = key.add(BigInteger.valueOf(i + 1));
			lista[x += 1] = get(new_key);
		}

		return lista;
	}

	// T(N): O(㏒ N)
	public BigInteger incBigInteger(final BigInteger key)
	{
		while (true)
		{
			final var r = _functionDHT(RichiestaDHT.incBigInteger, key, null);

			if (r instanceof BigInteger)
				return (BigInteger) r;
			else
			{
				System.out.println(key + " inc non trovata");
				GB.sleep(_sleepTime);
			}
		}
	}

	// T(N): O(㏒ N)
	public Serializable remove(final BigInteger key)
	{
		while (true)
		{
			final var r = _functionDHT(RichiestaDHT.remove, key, null);

			if (Boolean.TRUE.equals(r))
				return r;
			else
			{
				System.out.println(key + " remove non trovata");
				GB.sleep(_sleepTime);
			}
		}
	}

	// T(N): O(㏒ N)
	public <X extends Serializable> X getOrDefault(final BigInteger key, final X default_)
	{
		final var g = get(key);

		return (g == null ? default_ : (X) g);
	}

	public Serializable get(final BigInteger key)
	{
		while (true)
		{
			var r = _functionDHT(RichiestaDHT.get, key, null);

			if (Boolean.FALSE.equals(r))
			{
				System.out.println(key + " get non trovata");
				GB.sleep(_sleepTime);
			}
			else
				return r;
		}
	}

	// T(N): O(㏒ N)
	public Serializable put(final BigInteger key, final Serializable object)
	{
		while (true)
		{
			var r = _functionDHT(RichiestaDHT.put, key, object);

			if (Boolean.TRUE.equals(r))
				return r;
			else
			{
				System.out.println(key + " put non trovata");
				GB.sleep(_sleepTime);
			}
		}
	}

	// T(N): O(1)
	public Serializable transfer(final BigInteger key, final Serializable object)
	{
		System.out.println(key + " transfer!");
		return dht.put(key, object);
	}

	private Serializable _functionDHT(final RichiestaDHT req, final BigInteger key, final Serializable object)
	{
		while (true)
		{
			final var successor = find_successor(key);

			//System.out.println("[" + GB.DateToString() + "] DHT." + req + " > " + n_ + ": " + key + "=" + object);

			if (n.equals(successor))
				switch (req)
				{
					case incBigInteger:
						return dht.incBigInteger(key);

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
					return gRPC_Client.gRPC(successor, req, key, object);
				}
				catch (StatusRuntimeException e)
				{
					System.out.println("DHT: Nodo " + successor + " non raggiungibile!");
					setSuccessor(null);
					GB.sleep(_sleepTime);
				}
				catch (IOException e)
				{
					System.out.println("Errore serializzazione oggetto!");
					e.printStackTrace();
					break;
				}
				catch (ClassNotFoundException e)
				{
					System.out.println("Classe SHA1 non trovata!");
					e.printStackTrace();
					break;
				}
		}

		return null;
	}
	//endregion


	//region Stabilizzazione
	//stabilize the chord ring/circle after getNode joins and departures
	private void startStabilizingRoutines()
	{
		for (var stabilizingRoutine : stabilizingRoutines)
			stabilizingRoutine.start();
	}

	// called periodically. n asks the getSuccessor
	// about its predecessor, verifies if n's immediate
	// getSuccessor is consistent, and tells the getSuccessor about n
	private void stabilize()
	{
		//var x = successor.predecessor;
		final var successor = getSuccessor();
		final var x = gRPC_Client.gRPC(successor, RichiestaChord.getPredecessor);

		if (x != null)
			//x ∈ (n, successor)
			if (GB.incluso(x, n, successor))
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

		if (next > GB.numberOfFingers)
			next = 1;

		var i = GB.getPowerOfTwo(next - 1);
		i = n.ID.add(i);

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

	// T(N): O(1/N)
	// called periodically. handoff my data to the correct peer
	private void handoff()
	{
		final var daRimuovere = new HashSet<BigInteger>();
		final var daFare = new HashSet<BigInteger>();

		do
		{
			daFare.clear();

			final var keys = dht.getKeys();

			for (final var key : keys)
			{
				final var successor = find_successor(key);

				if (!n.equals(successor))
					try
					{
						//final var info = "[" + GB.DateToString() + "] handoff > " + successor + ": " + e.getKey() + "=" + e.getValue();
						//System.out.println(info);

						daFare.add(key);
						final var risultatoTrasferimento = gRPC_Client.gRPC(successor, RichiestaDHT.transfer, key, dht.get(key));

						if (Boolean.TRUE.equals(risultatoTrasferimento))
						{
							daRimuovere.add(key);
							//System.out.println(info + " OK!");
						}
					}
					catch (StatusRuntimeException ex)
					{
						System.out.println("Handoff: Nodo " + successor + " non raggiungibile!");
						setSuccessor(null);
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

			dht.removeAll(daRimuovere);
		}
		while (daFare.size() > 0);
	}

	public NodeLink ping()
	{
		return n;
	}
	//endregion

	//region Mutual Exlusion
	public void invokeMutualExclusion(Runnable critical_region_callback)
	{
		mutualExclusion.invokeMutualExclusion(critical_region_callback);
	}
	//endregion

	//region Print
	public void stampaStato()
	{
		System.out.println("Nodo: " + n);
		System.out.println("Predecessor: " + getPredecessor());
		System.out.println("Successor: " + getSuccessor());
		System.out.println("Finger Table:");

		final var lista = getFingerList();

		for (var i = 0; i < lista.length; i++)
			System.out.println((i + 1) + ": " + lista[i]);
	}

	public void stampaListaPeer()
	{
		System.out.println("Lista peers:");

		final var peers = getPeerList();

		var x = 0;
		for (final var p : peers)
			System.out.println("Peer #" + (x += 1) + ": " + p);
	}

	public void stampaData()
	{
		final var data = dht.getData();
		System.out.println("Dati:");

		for (final var d : data)
			System.out.println(d.getKey() + " > " + d.getValue());
	}
	//endregion

}