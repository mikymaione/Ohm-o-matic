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

	private final char mBit;
	private final HashMap<Integer, NodeLink> fingers;

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

	public long start(int k)
	{
		var z = getNode(k);

		return start(z, k, mBit);
	}

	public static long start(NodeLink z, int k, char mBit)
	{
		var n = z.key;
		n = n + GB.getPowerOfTwo(k, mBit);
		n = n % GB.getPowerOfTwo(mBit, mBit);

		return n;
	}


}