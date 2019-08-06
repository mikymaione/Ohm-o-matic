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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

class DHT
{

	private final BigInteger keyListaPeers;
	private final HashMap<BigInteger, Serializable> _data = new HashMap<>();


	DHT(BigInteger keyListaPeers)
	{
		this.keyListaPeers = keyListaPeers;
	}

	Boolean createPeerList()
	{
		synchronized (_data)
		{
			_data.put(keyListaPeers, new HashSet<BigInteger>());

			return true;
		}
	}

	Boolean addToPeerList(final Serializable ID)
	{
		if (ID instanceof BigInteger)
		{
			final var n_ = (BigInteger) ID;

			synchronized (_data)
			{
				if (_data.containsKey(keyListaPeers))
				{
					var _peerList = (HashSet<BigInteger>) _data.get(keyListaPeers);
					_peerList.add(n_);

					return true;
				}
			}
		}

		return false;
	}

	Boolean removeFromPeerList(final Serializable ID)
	{
		if (ID instanceof BigInteger)
		{
			final var n_ = (BigInteger) ID;

			synchronized (_data)
			{
				if (_data.containsKey(keyListaPeers))
				{
					var _peerList = (HashSet<BigInteger>) _data.get(keyListaPeers);
					_peerList.remove(n_);

					return true;
				}
			}
		}

		return false;
	}


	Serializable get(final BigInteger key)
	{
		synchronized (_data)
		{
			return _data.get(key);
		}
	}

	Boolean put(final BigInteger key, final Serializable value)
	{
		synchronized (_data)
		{
			_data.put(key, value);

			return true;
		}
	}

	Boolean remove(BigInteger key)
	{
		synchronized (_data)
		{
			_data.remove(key);

			return true;
		}
	}

	void forEach(Consumer<Map.Entry<BigInteger, Serializable>> callback)
	{
		synchronized (_data)
		{
			for (var e : _data.entrySet())
				callback.accept(e);
		}
	}

	void getData(Consumer<Map.Entry<BigInteger, Serializable>> callback)
	{
		synchronized (_data)
		{
			for (var e : _data.entrySet())
				callback.accept(e);
		}
	}

	BigInteger[] getPeerList()
	{
		synchronized (_data)
		{
			if (_data.containsKey(keyListaPeers))
			{
				var _peerList = (HashSet<BigInteger>) _data.get(keyListaPeers);
				var a = _peerList.toArray();
				var b = new BigInteger[a.length];

				for (var i = 0; i < a.length; i++)
					b[i] = (BigInteger) a[i];

				return b;
			}

			return null;
		}
	}


}