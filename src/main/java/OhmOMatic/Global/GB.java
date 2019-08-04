/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

import OhmOMatic.Chord.Link.NodeLink;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public final class GB
{

	private static final int randomSeed = new Date().getSeconds();
	private static Random randomFN = new Random(randomSeed);

	private static HashMap<Integer, BigInteger> _powerOfTwo = new HashMap<>();


	public static BigInteger getPowerOfTwo(final Integer k, final Integer mBit)
	{
		if (_powerOfTwo.size() == 0)
		{
			var due = BigInteger.valueOf(2);
			var curVal = BigInteger.valueOf(1); //2^0

			for (Integer i = 0; i <= mBit; i++)
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

	public static boolean inclusoR(BigInteger id, NodeLink a, NodeLink b)
	{
		return inclusoR(id, a.ID, b.ID);
	}

	private static boolean inclusoR(BigInteger id, BigInteger a, BigInteger b)
	{
		return incluso(id, a, b) || id.equals(b);
	}

	private static boolean incluso(BigInteger id, BigInteger x, BigInteger y)
	{
		switch (BigIntegerCompare(x, y))
		{
			case 1:
				return BigIntegerCompare(x, id) == -1 || BigIntegerCompare(y, id) >= 0;
			case -1:
				return BigIntegerCompare(x, id) == -1 && BigIntegerCompare(y, id) >= 0;
			case 0:
				return BigIntegerCompare(x, id) != 0;
			default:
				return false;
		}
	}

	private static int BigIntegerCompare(BigInteger a, BigInteger b)
	{
		//This method returns the value zero if (a == b),
		//if (a < b) then it returns a value less than zero
		//and if (a > b) then it returns a value greater than zero.

		return a.compareTo(b);
	}

	public static byte[] SHA1(String s)
	{
		try
		{
			var sha1 = MessageDigest.getInstance("SHA-1");

			sha1.reset();
			sha1.update(s.getBytes(StandardCharsets.UTF_8));

			return sha1.digest();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

		return null;
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
		var sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

		return sdf.format(d);
	}


}