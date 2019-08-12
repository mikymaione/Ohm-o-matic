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
import java.util.HashSet;
import java.util.Stack;

public class MutualExclusion implements AutoCloseable
{

	private final NodeLink me;

	private final Object shared_vars = new Object();

	private int outstanding_reply_count = 0;

	private int highest_sequence_number = 0;
	private int our_sequence_number = 0;

	private final HashSet<BigInteger> requesting_critical_section = new HashSet<>();
	private final Stack<BigInteger> reply_deferred = new Stack<>();

	private final Chord chord;
	private final int numberOFMutex;


	public MutualExclusion(final int numberOFMutex, final NodeLink me, final Chord chord)
	{
		this.me = me;
		this.chord = chord;
		this.numberOFMutex = numberOFMutex;
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
	// Request entry to our critical section
	public void invokeMutualExclusion(Runnable critical_region_callback)
	{
		final var Nodi = getNodi();

		synchronized (shared_vars)
		{
			// Choose a sequence number
			synchronized (requesting_critical_section)
			{
				requesting_critical_section.add(me.ID);
			}

			our_sequence_number = highest_sequence_number + 1;
			outstanding_reply_count = Nodi.length - 1;
		}

		// Send a request message containing our sequence number and our node number to all other nodes
		for (final var nodo : Nodi)
			if (!nodo.equals(me))
				try
				{
					GB.waitfor(() -> gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.request, our_sequence_number, me), 250);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

		// Now wait for a reply from each of the other nodes
		try
		{
			GB.waitfor(() ->
			{
				synchronized (shared_vars)
				{
					return outstanding_reply_count == 0;
				}
			}, 100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Critical section processing can be performed at this point
		critical_region_callback.run();
		synchronized (requesting_critical_section)
		{
			requesting_critical_section.remove(me);
		}

		for (final var nodo : Nodi)
			if (!nodo.ID.equals(me.ID))
				try
				{
					GB.waitfor(() -> gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.free, me), 250);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

		final int deferredCount;
		synchronized (reply_deferred)
		{
			deferredCount = reply_deferred.size();
		}

		if (deferredCount > 0)
		{
			final BigInteger deferred;

			synchronized (reply_deferred)
			{
				deferred = reply_deferred.pop();
			}

			for (final var nodo : Nodi)
				if (nodo.ID.equals(deferred))
					try
					{
						GB.waitfor(() -> gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.reply), 250);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
		}
	}
	//endregion


	//region Server
	public void free(NodeLink n)
	{
		synchronized (requesting_critical_section)
		{
			if (requesting_critical_section.contains(n.ID))
				requesting_critical_section.remove(n.ID);
		}
	}

	public void reply()
	{
		synchronized (shared_vars)
		{
			outstanding_reply_count--;

			final int deferredCount;
			synchronized (reply_deferred)
			{
				deferredCount = reply_deferred.size();
			}

			if (deferredCount > 0)
			{
				final boolean inviaReq;
				synchronized (requesting_critical_section)
				{
					inviaReq = requesting_critical_section.contains(me.ID);
				}

				if (inviaReq)
				{
					final var Nodi = getNodi();

					if (Nodi.length > 0)
					{
						final BigInteger deferred;
						synchronized (reply_deferred)
						{
							deferred = reply_deferred.pop();
						}

						for (final var nodo : Nodi)
							try
							{
								if (nodo.ID.equals(deferred))
								{
									GB.waitfor(() -> gRPC_Client.gRPC(nodo, RichiestaRicartAgrawala.reply), 250);
									break;
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
					}
				}
			}
		}
	}

	// caller_sequence_number is the sequence number begin requested,
	// caller is the node making the request;
	public void request(Integer caller_sequence_number, NodeLink caller)
	{
		final boolean defer_it;

		synchronized (shared_vars)
		{
			synchronized (requesting_critical_section)
			{
				requesting_critical_section.add(caller.ID);

				highest_sequence_number = Math.max(highest_sequence_number, caller_sequence_number);

				// defer_it will be true if we have priority over node caller's request
				defer_it =
						(requesting_critical_section.size() > numberOFMutex) &&
								((caller_sequence_number > our_sequence_number) ||
										(caller_sequence_number == our_sequence_number && caller.ID.compareTo(me.ID) > 0));
			}
		}

		if (defer_it)
			synchronized (reply_deferred)
			{
				reply_deferred.push(caller.ID);
			}
		else
			try
			{
				GB.waitfor(() -> gRPC_Client.gRPC(caller, RichiestaRicartAgrawala.reply), 250);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}
	//endregion

}