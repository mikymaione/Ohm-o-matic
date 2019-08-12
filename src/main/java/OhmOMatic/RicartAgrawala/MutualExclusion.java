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
import OhmOMatic.Chord.Enums.RichiestaRicartAgrawala;
import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Global.GB;
import OhmOMatic.RicartAgrawala.gRPC.gRPC_Client;

import java.util.HashMap;

public class MutualExclusion implements AutoCloseable
{

	private final NodeLink me;

	private int outstanding_reply_count = 0;

	private int highest_sequence_number = 0;
	private int our_sequence_number = 0;

	private boolean requesting_critical_section = false;
	private final HashMap<NodeLink, Boolean> reply_deferred = new HashMap<>();

	private final Chord chord;


	public MutualExclusion(final NodeLink me, Chord chord)
	{
		this.me = me;
		this.chord = chord;
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

	public void invokeMutualExclusion(Runnable critical_region_callback)
	{
		// Request entry to our critical section

		// Choose a sequence number
		requesting_critical_section = true;
		our_sequence_number = highest_sequence_number + 1;

		final var Nodi = getNodi();
		outstanding_reply_count = Nodi.length - 1;

		// Send a request message containing our sequnce number and our node number to all other nodes
		for (final var nodo : Nodi)
			if (!nodo.equals(me))
			{
				final var r = gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.request, our_sequence_number, me);
				reply_deferred.put(nodo, r);
			}

		// Now wait for a reply from each of the other nodes
		try
		{
			GB.waitfor(() -> outstanding_reply_count == 0, 250);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Critical section processing can be performed at this point
		critical_region_callback.run();
		requesting_critical_section = false;

		for (final var nodo : Nodi)
			if (reply_deferred.getOrDefault(nodo, false))
			{
				reply_deferred.put(nodo, false);

				// Send a reply to node j
				gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.reply);
			}
	}

	public void reply()
	{
		outstanding_reply_count--;
	}

	// k is the sequence number begin requested,
	// j is the node number making the request;
	public void request(Integer k, NodeLink j)
	{
		highest_sequence_number = Math.max(highest_sequence_number, k);

		// defer_it will be true if we have priority over node j's request
		final var defer_it =
				requesting_critical_section &&
						((k > our_sequence_number) ||
								(k == our_sequence_number && j.ID.compareTo(me.ID) > 0));

		if (defer_it)
			reply_deferred.put(j, true);
		else
			gRPC_Client.gRPC(j, RichiestaRicartAgrawala.reply);
	}


}