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
package OhmOMatic.Chord.FN;

import java.math.BigInteger;

public class NodeLink
{

	public final BigInteger ID;
	public final String IP;
	public final int port;

	public boolean isDead = false;


	public NodeLink(String IP, int port)
	{
		//versione normale con numeri SHA1
		//var b = GB.SHA1(IP + ":" + port);
		//this.ID = new BigInteger(b);

		//versione semplificata con numeri per umani
		this.ID = BigInteger.valueOf(port);

		this.IP = IP;
		this.port = port;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof NodeLink)
		{
			var aNode = (NodeLink) obj;

			return ID.equals(aNode.ID);
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return IP + ":" + port;
	}


}