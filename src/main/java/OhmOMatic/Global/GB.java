/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

import OhmOMatic.Chord.FN.NodeLink;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class GB
{

	private static Random random = new Random(new Date().getSeconds());

	private static HashMap<Integer, Double> _powerOfTwo = new HashMap<>();


	public static Double getPowerOfTwo(int k, char mBit)
	{
		if (_powerOfTwo.size() == 0)
			for (var i = -1; i <= mBit; i++)
			{
				var e = Math.pow(2, i);
				_powerOfTwo.put(i, e);
			}

		return _powerOfTwo.get(k);
	}

	/*public static long computeRelativeId(NodeLink universal, long local, char mBit)
	{
		var univ = hashSocketAddress(universal);

		return computeRelativeId(univ, local, mBit);
	}*/

	/*public static long computeRelativeId(long universal, long local, char mBit)
	{
		long ret = universal - local;

		if (ret < 0)
			ret += getPowerOfTwo(32, mBit);

		return ret;
	}*/

	public static void clearScreen()
	{
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	public static byte[] serialize(Object obj) throws IOException
	{
		try (
				var out = new ByteArrayOutputStream();
				var os = new ObjectOutputStream(out)
		)
		{
			os.writeObject(obj);

			return out.toByteArray();
		}
	}

	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException
	{
		try (
				var in = new ByteArrayInputStream(data);
				var is = new ObjectInputStream(in)
		)
		{
			return is.readObject();
		}
	}

	/**
	 * Compute a socket address' SHA-1 hash in hex
	 * and its approximate position in string
	 *
	 * @param addr
	 * @return
	 */
	public static String hexIdAndPosition(NodeLink addr, char mBit)
	{
		long hash = addr.ID;

		return (longTo8DigitHex(hash) + " (" + hash * 100 / getPowerOfTwo(mBit, mBit) + "%)");
	}

	/**
	 * @param l generate a long type number's 8-digit hex string
	 * @return
	 */
	public static String longTo8DigitHex(long l)
	{
		var hex = Long.toHexString(l);

		int lack = 8 - hex.length();

		var sb = new StringBuilder();

		for (var i = lack; i > 0; i--)
			sb.append("0");

		sb.append(hex);

		return sb.toString();
	}

	/**
	 * Return a node's finger[i].start, universal
	 *
	 * @param nodeid: node's identifier
	 * @param i:      finger table index
	 * @return finger[i].start's identifier
	 */
	public static long ithStart(long nodeid, int i, char mBit)
	{
		var n = nodeid;
		var l = getPowerOfTwo(i - 1, mBit);
		var r = getPowerOfTwo(mBit, mBit);

		Double z = (n + l) % r;

		return z.longValue();
	}

	public static long hashSocketAddress(String addr)
	{
		int i = addr.hashCode();

		return hashHashCode(i);
	}

	/**
	 * Compute a 32 bit integer's identifier
	 *
	 * @param i: integer
	 * @return 32-bit identifier in long type
	 */
	private static long hashHashCode(int i)
	{
		//32 bit regular hash code -> byte[4]
		byte[] hashbytes = new byte[4];
		hashbytes[0] = (byte) (i >> 24);
		hashbytes[1] = (byte) (i >> 16);
		hashbytes[2] = (byte) (i >> 8);
		hashbytes[3] = (byte) (i /*>> 0*/);

		// try to create SHA1 digest
		MessageDigest md = null;
		try
		{
			md = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// successfully created SHA1 digest
		// try to convert byte[4]
		// -> SHA1 result byte[]
		// -> compressed result byte[4]
		// -> compressed result in long type
		if (md != null)
		{
			md.reset();
			md.update(hashbytes);
			byte[] result = md.digest();

			byte[] compressed = new byte[4];
			for (int j = 0; j < 4; j++)
			{
				byte temp = result[j];
				for (int k = 1; k < 5; k++)
				{
					temp = (byte) (temp ^ result[j + k]);
				}
				compressed[j] = temp;
			}

			long ret = (compressed[0] & 0xFF) << 24 | (compressed[1] & 0xFF) << 16 | (compressed[2] & 0xFF) << 8 | (compressed[3] & 0xFF);
			ret = ret & (long) 0xFFFFFFFFl;
			return ret;
		}
		return 0;
	}

	public static boolean stringIsBlank(String s)
	{
		return (s == null || s.equals("") || s.equals(" "));
	}

	public static int randomInt(int da, int a)
	{
		return random.nextInt(a) + da;
	}

	public static boolean gt(final byte[] a, final byte[] b)
	{
		return Arrays.compare(a, b) > 0;
	}

	public static boolean ge(final byte[] a, final byte[] b)
	{
		return Arrays.compare(a, b) >= 0;
	}

	public static boolean lt(final byte[] a, final byte[] b)
	{
		return Arrays.compare(a, b) < 0;
	}

	public static boolean le(final byte[] a, final byte[] b)
	{
		return Arrays.compare(a, b) <= 0;
	}

	public static byte[] shiftLeft(byte[] byteArray, int shiftBitCount)
	{
		//https://github.com/patrickfav/bytes-java

		final var shiftMod = shiftBitCount % 8;
		final var carryMask = (byte) ((1 << shiftMod) - 1);
		final var offsetBytes = (shiftBitCount / 8);

		int sourceIndex;

		for (var i = 0; i < byteArray.length; i++)
		{
			sourceIndex = i + offsetBytes;

			if (sourceIndex >= byteArray.length)
			{
				byteArray[i] = 0;
			}
			else
			{
				final var src = byteArray[sourceIndex];
				var dst = (byte) (src << shiftMod);

				if (sourceIndex + 1 < byteArray.length)
					dst |= byteArray[sourceIndex + 1] >>> (8 - shiftMod) & carryMask;

				byteArray[i] = dst;
			}
		}

		return byteArray;
	}

	public static TimerTask executeTimerTask(Runnable r)
	{
		return new TimerTask()
		{
			@Override
			public void run()
			{
				r.run();
			}
		};
	}


}