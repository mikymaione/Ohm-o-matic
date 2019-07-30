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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class DHT
{

	private final HashMap<BigInteger, Serializable> _data = new HashMap<>();


	synchronized Serializable getPeerList(final BigInteger keyListaPeers)
	{
		var s = _data.get(keyListaPeers);

		return (s instanceof HashSet ? (HashSet<BigInteger>) s : new HashSet<>());
	}

	synchronized Serializable addToPeerList(final BigInteger keyListaPeers, final Serializable ID)
	{
		if (ID instanceof BigInteger)
		{
			final var n_ = (BigInteger) ID;

			var s = _data.get(keyListaPeers);
			var HS = (s instanceof HashSet ? (HashSet<BigInteger>) s : new HashSet<BigInteger>());
			HS.add(n_);

			_data.put(keyListaPeers, HS);

			return true;
		}

		return false;
	}

	synchronized Serializable removeFromPeerList(final BigInteger keyListaPeers, final Serializable ID)
	{
		if (ID instanceof BigInteger)
		{
			final var n_ = (BigInteger) ID;

			var s = _data.get(keyListaPeers);
			var HS = (s instanceof HashSet ? (HashSet<BigInteger>) s : new HashSet<BigInteger>());
			HS.remove(n_);

			_data.put(keyListaPeers, HS);

			return true;
		}

		return false;
	}

	synchronized Serializable get(final BigInteger key)
	{
		return _data.get(key);
	}

	synchronized Serializable put(final BigInteger key, final Serializable value)
	{
		return _data.put(key, value);
	}

	synchronized Serializable remove(BigInteger key)
	{
		return _data.remove(key);
	}

	synchronized void forEachAndClearAll(Consumer<Map.Entry<BigInteger, Serializable>> callback)
	{
		for (var e : _data.entrySet())
			callback.accept(e);

		_data.clear();
	}

	synchronized void forEachAndRemoveAll(Consumer<Map.Entry<BigInteger, Serializable>> callback, final List<BigInteger> daRimuovere)
	{
		for (var e : _data.entrySet())
			callback.accept(e);

		for (var del : daRimuovere)
			_data.remove(del);
	}


}