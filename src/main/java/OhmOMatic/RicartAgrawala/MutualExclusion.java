/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Implementazione in Java di An Optimal Algorithm for Mutual Exclusion in Computer Networks:
-The Ricart-Agrawala Algorithm is an algorithm for mutual exclusion on a distributed system.
-This algorithm is an extension and optimization of Lamport's Distributed Mutual Exclusion Algorithm, by removing the need for ACK messages.
-It was developed by Glenn Ricart and Ashok Agrawala.
-Fonte: http://en.wikipedia.org/wiki/Ricart%E2%80%93Agrawala_algorithm
*/
package OhmOMatic.RicartAgrawala;

import OhmOMatic.Chord.Chord;
import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Global.GB;
import OhmOMatic.RicartAgrawala.Enums.RichiestaRicartAgrawala;
import OhmOMatic.RicartAgrawala.gRPC.gRPC_Client;

import java.util.HashMap;
import java.util.HashSet;

public class MutualExclusion implements AutoCloseable
{

	private final int timeout_gRPC = 250;

	private final NodeLink me;

	private final Object shared_vars = new Object();
	private final int numberOfMutex;

	private int highest_sequence_number = 0;
	private int our_sequence_number = 0;

	private boolean i_am_requesting_critical_section = false;

	private final HashMap<NodeLink, Boolean> reply_deferred = new HashMap<>();
	private final HashMap<NodeLink, Boolean> outstanding_reply_count = new HashMap<>();
	private final HashMap<NodeLink, Boolean> requesting_critical_section = new HashMap<>();
	private final HashMap<NodeLink, Integer> caller_sequence_numbers = new HashMap<>();

	private final Chord chord;


	public MutualExclusion(final int numberOfMutex, final NodeLink me, Chord chord)
	{
		this.me = me;
		this.chord = chord;
		this.numberOfMutex = numberOfMutex;
	}

	@Override
	public void close()
	{
		//
	}

	private NodeLink[] getNodi()
	{
		return chord.getPeerList();
	}

	//region Client

	// 2(N – 1)
	// Request entry to our critical section
	public void invokeMutualExclusion(Runnable critical_region_callback)
	{
		final var Nodi = getNodi();

		synchronized (shared_vars)
		{
			// Choose a sequence number
			i_am_requesting_critical_section = true;
			requesting_critical_section.put(me, true);
			our_sequence_number = highest_sequence_number + 1;

			for (final var nodo : Nodi)
				if (!nodo.equals(me))
					outstanding_reply_count.put(nodo, true);
		}

		GB.Sleep(5000);

		// Send a request message containing our sequence number and our node number to all other nodes
		for (final var nodo : Nodi)
			if (!nodo.equals(me))
				GB.waitfor(() ->
						gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.request, our_sequence_number, me, true), timeout_gRPC);

		// Now wait for a reply from each of the other nodes
		GB.waitfor(() ->
		{
			synchronized (shared_vars)
			{
				return GB.countThisValue(outstanding_reply_count, true) == 0;
			}
		}, 100);

		// Critical section processing can be performed at this point
		critical_region_callback.run();

		synchronized (shared_vars)
		{
			i_am_requesting_critical_section = false;
			requesting_critical_section.put(me, false);
		}

		for (final var nodo : Nodi)
		{
			final boolean ok;

			synchronized (shared_vars)
			{
				ok = reply_deferred.getOrDefault(nodo, false);

				if (ok)
					reply_deferred.put(nodo, false);
			}

			if (ok)
				GB.waitfor(() ->
						gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.reply, me, false), timeout_gRPC);

			GB.waitfor(() ->
					gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.free, me, false), timeout_gRPC);
		}
	}
	//endregion

	//region Server
	public void free(final NodeLink caller)
	{
		synchronized (shared_vars)
		{
			requesting_critical_section.put(caller, false);
		}
	}

	public void reply(final NodeLink caller, final boolean caller_requesting_critical_section)
	{
		final var toReply = new HashSet<NodeLink>();

		synchronized (shared_vars)
		{
			requesting_critical_section.put(caller, caller_requesting_critical_section);
			outstanding_reply_count.put(caller, false);

			if (GB.countThisValue(outstanding_reply_count, true) == 0) // ci sono solo io nella critical section
			{
				var pNellaCritical = 1;

				for (var e : requesting_critical_section.entrySet())
					if (pNellaCritical <= numberOfMutex)
						if (e.getValue() && !e.getKey().equals(me)) // lui vuole entrare
						{
							pNellaCritical++;
							toReply.add(e.getKey());
							reply_deferred.put(e.getKey(), false);
						}
			}
		}

		for (var nodo : toReply)
			GB.waitfor(() ->
					gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.reply, me, false), timeout_gRPC);
	}

	// caller_sequence_number is the sequence number begin requested,
	// caller is the node making the request;
	public void request(final Integer caller_sequence_number, final NodeLink caller)
	{
		final boolean defer_it;

		synchronized (shared_vars)
		{
			requesting_critical_section.put(caller, true);
			caller_sequence_numbers.put(caller, caller_sequence_number);

			highest_sequence_number = Math.max(highest_sequence_number, caller_sequence_number);

			// defer_it will be true if we have priority over node caller's request
			defer_it = i_am_requesting_critical_section && (
					(caller_sequence_number > our_sequence_number) ||
							(caller_sequence_number == our_sequence_number && caller.ID.compareTo(me.ID) > 0)
			);
		}

		if (defer_it)
			synchronized (shared_vars)
			{
				reply_deferred.put(caller, true);
			}
		else
			GB.waitfor(() ->
					gRPC_Client.gRPC(caller, RichiestaRicartAgrawala.reply, me, i_am_requesting_critical_section), timeout_gRPC);
	}
	//endregion

}