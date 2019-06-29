/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Global.GB;

import java.util.HashMap;

public class FingerTable
{

	private final HashMap<Integer, NodeLink> fingers;
	private char mBit;

	public FingerTable(char mBit)
	{
		this.mBit = mBit;
		this.fingers = new HashMap<>(mBit);
	}

	public synchronized NodeLink getNode(int k)
	{
		return fingers.get(k);
	}

	public synchronized void setNode(int k, NodeLink n)
	{
		fingers.put(k, n);
	}

	public synchronized NodeLink node(int k) throws Exception
	{
		var s = start(k);

		for (var e : fingers.values())
			//if (e.key >= s)
			if (e.key.compareTo(s) >= 0)
				return e;

		return null;
	}

	public double start(int k) throws Exception
	{
		if (!(1 <= k && k <= mBit))
			throw new Exception("k deve essere compreso tra 1 e " + mBit);

		var e = fingers.get(k);
		var n = e.key;
		var l = GB.getPowerOfTwo(k - 1, mBit);
		var r = GB.getPowerOfTwo(mBit, mBit);

		var z = l % r;

		return z;
	}


}