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

import java.math.BigInteger;
import java.util.HashMap;

public class MutualExclusion implements AutoCloseable
{

	private final NodeLink me;
	private final int numberOfResources;

	private final Object shared_vars = new Object();

	private int outstanding_reply_count = 0;

	private int highest_sequence_number = 0;
	private int our_sequence_number = 0;

	private boolean requesting_critical_section = false;
	private final HashMap<BigInteger, Boolean> reply_deferred = new HashMap<>();

	private final Chord chord;


	public MutualExclusion(final int numberOfResources, final NodeLink me, Chord chord)
	{
		this.me = me;
		this.chord = chord;
		this.numberOfResources = numberOfResources;
	}

	@Override
	public void close()
	{
		//
	}

	//region Client
	// Request entry to our critical section
	public void invokeMutualExclusion(Runnable critical_region_callback)
	{
		final var peerList = chord.getPeerList();

		synchronized (shared_vars)
		{
			// Choose a sequence number
			requesting_critical_section = true;
			our_sequence_number = highest_sequence_number + 1;
			outstanding_reply_count = peerList.length - numberOfResources;
		}

		// Send a request message containing our sequence number and our node number to all other nodes
		for (final var nodo : peerList)
			if (!nodo.equals(me))
				GB.waitfor(() -> gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.request, our_sequence_number, me), 250);

		// Now wait for a reply from each of the other nodes
		GB.waitfor(() ->
		{
			synchronized (shared_vars)
			{
				return outstanding_reply_count < 1;
			}
		}, 100);

		// Critical section processing can be performed at this point
		critical_region_callback.run();

		synchronized (shared_vars)
		{
			requesting_critical_section = false;
		}

		for (final var nodo : peerList)
			if (!nodo.equals(me))
			{
				final boolean ok;

				synchronized (shared_vars)
				{
					ok = reply_deferred.getOrDefault(nodo.ID, false);

					if (ok)
						reply_deferred.put(nodo.ID, false);
				}

				if (ok)
					GB.waitfor(() -> gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.reply), 250);
			}
	}
	//endregion

	//region Server
	public void reply()
	{
		synchronized (shared_vars)
		{
			outstanding_reply_count--;
		}
	}

	// caller_sequence_number is the sequence number begin requested,
	// caller is the node making the request;
	public void request(Integer caller_sequence_number, NodeLink caller)
	{
		final boolean defer_it;

		synchronized (shared_vars)
		{
			highest_sequence_number = Math.max(highest_sequence_number, caller_sequence_number);

			defer_it =
					requesting_critical_section &&
							((caller_sequence_number > our_sequence_number) ||
									(caller_sequence_number == our_sequence_number && caller.ID.compareTo(me.ID) > 0));
		}

		if (defer_it)
			synchronized (shared_vars)
			{
				reply_deferred.put(caller.ID, true);
			}
		else
			GB.waitfor(() -> gRPC_Client.gRPC(caller, RichiestaRicartAgrawala.reply), 250);
	}
	//endregion

}