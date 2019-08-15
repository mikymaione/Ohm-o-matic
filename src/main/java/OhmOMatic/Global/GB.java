/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

import OhmOMatic.Chord.Link.NodeLink;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA3_512;

public final class GB
{

	public static final Integer numberOfFingers = 32;

	private static final int shaBits = 512;
	public static final BigInteger shaBitsB = BigInteger.valueOf(shaBits);
	private static final DigestUtils duSHA3_512 = new DigestUtils(SHA3_512);
	private static final HashMap<String, BigInteger> _shaStrings = new HashMap<>();
	private static final HashMap<Integer, BigInteger> _powerOfTwo = new HashMap<>();

	private static final int randomSeed = new Date().getSeconds();
	private static final Random randomFN = new Random(randomSeed);


	public static BigInteger SHA(final String s)
	{
		if (_shaStrings.containsKey(s))
			return _shaStrings.get(s);

		final var b = new BigInteger(duSHA3_512.digest(s));
		_shaStrings.put(s, b);

		return b;
	}

	public static BigInteger getPowerOfTwo(final Integer k)
	{
		if (_powerOfTwo.size() == 0)
		{
			final var due = BigInteger.valueOf(2);
			var curVal = BigInteger.valueOf(1); // 2^0

			for (Integer i = 0; i <= shaBits; i++)
			{
				_powerOfTwo.put(i, curVal);
				curVal = curVal.multiply(due);
			}
		}

		return _powerOfTwo.get(k);
	}

	public static boolean incluso(NodeLink id, NodeLink a, BigInteger b)
	{
		return incluso(id.ID, a.ID, b);
	}

	public static boolean incluso(NodeLink id, NodeLink a, NodeLink b)
	{
		return incluso(id.ID, a.ID, b.ID);
	}

	public static boolean incluso(BigInteger id, NodeLink a, NodeLink b)
	{
		return incluso(id, a.ID, b.ID);
	}

	private static boolean incluso(BigInteger id, BigInteger start, BigInteger end)
	{
		if (end.compareTo(start) <= 0)
			return start.compareTo(id) < 0 || id.compareTo(end) <= 0;
		else
			return start.compareTo(id) < 0 && id.compareTo(end) <= 0;
	}

	public static boolean finger_incluso(NodeLink key, NodeLink x, BigInteger y)
	{
		return finger_incluso(key.ID, x.ID, y);
	}

	private static boolean finger_incluso(BigInteger key, BigInteger start, BigInteger end)
	{
		if (start.equals(end))
			return true;
		else if (end.compareTo(start) < 0)
			return start.compareTo(key) < 0 || key.compareTo(end) < 0;
		else
			return start.compareTo(key) < 0 && key.compareTo(end) < 0;
	}

	public static byte[] serialize(final Serializable obj) throws IOException
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

	public static <G extends Serializable> G deserializeT(final byte[] data) throws IOException, ClassNotFoundException
	{
		return (G) deserialize(data);
	}

	public static Serializable deserialize(final byte[] data) throws IOException, ClassNotFoundException
	{
		try (
				var in = new ByteArrayInputStream(data);
				var is = new ObjectInputStream(in)
		)
		{
			return (Serializable) is.readObject();
		}
	}

	public static boolean stringIsBlank(final String s)
	{
		return (s == null || s.equals("") || s.equals(" "));
	}

	public static int randomInt(final int da, final int a)
	{
		return randomFN.nextInt(a) + da;
	}

	public static String DateToString()
	{
		return DateToString(new Date());
	}

	private static String DateToString(Date d)
	{
		final var sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

		return sdf.format(d);
	}

	public static void waitfor(Callable<Boolean> callback, int millisecondi)
	{
		try
		{
			_waitfor(callback, millisecondi);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void _waitfor(Callable<Boolean> callback, int millisecondi) throws Exception
	{
		while (!callback.call())
			GB.sleep(millisecondi);
	}

	public static void sleep(int millisecondi)
	{
		try
		{
			Thread.sleep(millisecondi);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static Date Now()
	{
		final var n = new Date();

		return new Date(
				n.getYear(),
				n.getMonth(),
				n.getDate(),
				n.getHours(),
				n.getMinutes(),
				n.getSeconds()
		);
	}

	public static <A, B> int countThisValue(final HashMap<A, B> mappa, final B valore)
	{
		var x = 0;

		for (var e : mappa.values())
			if (e.equals(valore))
				x++;

		return x;
	}

}