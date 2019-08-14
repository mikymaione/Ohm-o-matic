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

import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Global.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

class DHT
{

	// S(N): O(㏒ N)
	private final HashMap<String, Serializable> _data = new HashMap<>();
	private final String keyListaPeers;


	DHT(String keyListaPeers)
	{
		this.keyListaPeers = keyListaPeers;
	}

	void createPeerList()
	{
		synchronized (_data)
		{
			_data.put(keyListaPeers, new HashSet<NodeLink>());
		}
	}

	private HashSet<NodeLink> _getPeerList_NS()
	{
		if (_data.containsKey(keyListaPeers))
		{
			var klp = _data.get(keyListaPeers);

			if (klp instanceof HashSet)
				return (HashSet<NodeLink>) klp;
		}

		return null;
	}

	Boolean addToPeerList(final Serializable ID)
	{
		if (ID instanceof NodeLink)
		{
			final var n_ = (NodeLink) ID;

			synchronized (_data)
			{
				var _peerList = _getPeerList_NS();

				if (_peerList != null)
				{
					_peerList.add(n_);
					return true;
				}
			}
		}

		return false;
	}

	Boolean removeFromPeerList(final Serializable ID)
	{
		if (ID instanceof NodeLink)
		{
			final var n_ = (NodeLink) ID;

			synchronized (_data)
			{
				var _peerList = _getPeerList_NS();

				if (_peerList != null)
				{
					_peerList.remove(n_);
					return true;
				}
			}
		}

		return false;
	}

	String[] getKeys()
	{
		synchronized (_data)
		{
			final var keySet = _data.keySet();
			final var R = new String[keySet.size()];

			var x = -1;
			for (final var k : keySet)
				R[x += 1] = k;

			return R;
		}
	}

	Pair<String, Serializable>[] getData()
	{
		synchronized (_data)
		{
			final var entryset = _data.entrySet();
			final var dati = new Pair[entryset.size()];

			var x = -1;
			for (final var d : entryset)
				dati[x += 1] = new Pair<>(d.getKey(), d.getValue());

			return dati;
		}
	}

	Serializable get(final String key)
	{
		synchronized (_data)
		{
			return _data.getOrDefault(key, false);
		}
	}

	Boolean put(final String key, final Serializable value)
	{
		synchronized (_data)
		{
			final var prev = _data.put(key, value);

			if (prev != null)
				System.out.println("Put overwrite " + key + " from " + prev + " to " + value);

			return true;
		}
	}

	Serializable inc(String key)
	{
		synchronized (_data)
		{
			var val = (Integer) _data.getOrDefault(key, 0);
			val += 1;

			_data.put(key, val);

			return val;
		}
	}

	Boolean remove(String key)
	{
		synchronized (_data)
		{
			_data.remove(key);

			return true;
		}
	}

	void removeAll(HashSet<String> daRimuovere)
	{
		synchronized (_data)
		{
			for (var d : daRimuovere)
				_data.remove(d);
		}
	}

	void forEachAndRemoveAll(Consumer<Map.Entry<String, Serializable>> callback, HashSet<String> daRimuovere)
	{
		synchronized (_data)
		{
			for (var e : _data.entrySet())
				callback.accept(e);

			removeAll(daRimuovere);
		}
	}

	NodeLink[] getPeerList()
	{
		synchronized (_data)
		{
			final var _peerList = _getPeerList_NS();

			if (_peerList != null)
			{
				var b = new NodeLink[_peerList.size()];

				var x = -1;
				for (var p : _peerList)
					b[x += 1] = p;

				return b;
			}

			return null;
		}
	}


}