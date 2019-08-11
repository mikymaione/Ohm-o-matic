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

import OhmOMatic.Chord.Enums.RichiestaRicartAgrawala;
import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Global.GB;
import OhmOMatic.Global.LockableGeneric;
import OhmOMatic.RicartAgrawala.gRPC.gRPC_Client;

import java.math.BigInteger;

public class MutualExclusion
{

	private final NodeLink me;
	private NodeLink[] Nodi;

	private LockableGeneric<Integer> outstanding_reply_count;

	private int highest_sequence_number = 0;
	private int our_sequence_number = 0;

	private boolean requesting_critical_section = false;
	private boolean[] reply_deferred;

	private final Object lock_shared_vars = new Object();


	public MutualExclusion(final NodeLink me, final NodeLink[] Nodi)
	{
		this.me = me;
		this.Nodi = Nodi;
		this.reply_deferred = new boolean[Nodi.length];

		this.outstanding_reply_count.set(0);
	}

	public void setNodi(NodeLink[] Nodi_)
	{
		this.Nodi = Nodi_;
	}

	public void invokeMutualExclusion()
	{
		// Request entry to our critical section
		synchronized (lock_shared_vars)
		{
			// Choose a sequence number
			requesting_critical_section = true;
			our_sequence_number = highest_sequence_number + 1;
		}

		outstanding_reply_count.set(Nodi.length - 1);

		// Send a request message containing our sequnce number and our node number to all other nodes
		for (var j = 0; j < Nodi.length; j++)
			if (!BigInteger.valueOf(j).equals(me.ID))
				reply_deferred[j] = gRPC_Client.gRPC(Nodi[j], RichiestaRicartAgrawala.request, our_sequence_number, me.ID);

		// Now wait for a reply from each of the other nodes
		try
		{
			GB.waitfor(() -> outstanding_reply_count.equals(0), 250);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Critical section processing can be performed at this point
		requesting_critical_section = false;

		for (var j = 0; j < Nodi.length; j++)
			if (reply_deferred[j])
			{
				reply_deferred[j] = false;

				// Send a reply to node j
				gRPC_Client.gRPC(Nodi[j], RichiestaRicartAgrawala.reply);
			}
	}

	public void reply()
	{
		outstanding_reply_count.inc(-1);
	}

	// k is the sequence number begin requested,
	// j is the node number making the request;
	public void request(Integer k, Integer j)
	{
		final boolean defer_it;

		highest_sequence_number = Math.max(highest_sequence_number, k);

		synchronized (lock_shared_vars)
		{
			// defer_it will be true if we have priority over node j's request
			defer_it = requesting_critical_section && ((k > our_sequence_number) || (k == our_sequence_number && BigInteger.valueOf(j).compareTo(me.ID) > 0));
		}

		if (defer_it)
			reply_deferred[j] = true;
		else
			gRPC_Client.gRPC(Nodi[j], RichiestaRicartAgrawala.reply);
	}


}