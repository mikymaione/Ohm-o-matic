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

	public static final int FingerTableLength = 4;
	public static final int ShaBit = 512;
	private static final BigInteger ShaBitB = BigInteger.valueOf(ShaBit);
	private static final int randomSeed = new Date().getSeconds();
	private static final Random randomFN = new Random(randomSeed);
	private static final HashMap<Integer, BigInteger> _powerOfTwo = new HashMap<>();


	private static BigInteger stringToBigInteger(String s)
	{
		final var du = new DigestUtils(SHA3_512);
		final var bytes = du.digest(s);

		return new BigInteger(bytes);
	}

	public static long getPowerOfTwo(final Integer k)
	{
		return _getPowerOfTwo(k).longValueExact();
	}

	private static BigInteger _getPowerOfTwo(final Integer k)
	{
		if (_powerOfTwo.size() == 0)
		{
			var curVal = BigInteger.valueOf(1); // 2^0

			for (Integer i = 0; i <= FingerTableLength; i++)
			{
				_powerOfTwo.put(i, curVal);
				curVal = curVal.multiply(BigInteger.TWO);
			}
		}

		return _powerOfTwo.get(k);
	}

	public static Long stringToModBit(String s)
	{
		var bi = stringToBigInteger(s);
		bi = bi.mod(ShaBitB);

		return bi.longValueExact();
	}

	public static boolean incluso(NodeLink id, NodeLink a, NodeLink b)
	{
		return incluso(id.ID, a.ID, b.ID);
	}

	public static boolean incluso(String id, NodeLink a, NodeLink b)
	{
		return incluso(id, a.ID, b.ID);
	}

	public static boolean incluso(NodeLink iThFinger, NodeLink n, String id)
	{
		return incluso(iThFinger.ID, n.ID, id);
	}

	private static boolean incluso(String id, String start, String end)
	{
		final var _id = stringToModBit(id);
		final var _start = stringToModBit(start);
		final var _end = stringToModBit(end);

		if (_end.compareTo(_start) <= 0)
			return _start.compareTo(_id) < 0 || _id.compareTo(_end) < 0;
		else
			return _start.compareTo(_id) < 0 && _id.compareTo(_end) < 0;
	}

	public static boolean inclusoR(String id, NodeLink start, NodeLink end)
	{
		return inclusoR(id, start.ID, end.ID);
	}

	private static boolean inclusoR(String id, String start, String end)
	{
		final var _id = stringToModBit(id);
		final var _start = stringToModBit(start);
		final var _end = stringToModBit(end);

		if (_end.compareTo(_start) <= 0)
			return _start.compareTo(_id) < 0 || _id.compareTo(_end) <= 0;
		else
			return _start.compareTo(_id) < 0 && _id.compareTo(_end) <= 0;
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

	public static String creaChiaveValore(final String key, final int n)
	{
		return key + "_valore_" + n;
	}


}