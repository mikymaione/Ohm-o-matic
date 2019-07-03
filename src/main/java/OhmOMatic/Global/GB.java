/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

import OhmOMatic.Chord.FN.NodeLink;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

	public static boolean incluso(NodeLink id, NodeLink x, BigInteger y)
	{
		return incluso(id.ID, x.ID, y);
	}

	public static boolean incluso(NodeLink id, NodeLink x, NodeLink y)
	{
		return incluso(id.ID, x.ID, y.ID);
	}

	public static boolean incluso(BigInteger id, NodeLink x, NodeLink y)
	{
		return incluso(id, x.ID, y.ID);
	}

	public static boolean incluso(BigInteger id, BigInteger x, BigInteger y)
	{
		//5.compareTo(3) == 1
		//5.compareTo(5) == 0
		//5.compareTo(8) == -1

		//x < id < y
		//id > x && id <= y

		if (x.equals(y))
			return true;

		if (y.compareTo(id) < 0)
			return x.compareTo(y) > 0 && x.compareTo(id) < 0;
		else
			return x.compareTo(y) < 0 && x.compareTo(id) < 0 || x.compareTo(y) > 0 && x.compareTo(id) > 0;
	}

	public static byte[] SHA1(String s)
	{
		try
		{
			var sha1 = MessageDigest.getInstance("SHA-1");

			sha1.reset();
			sha1.update(s.getBytes("UTF-8"));

			return sha1.digest();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
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

	public static void executeTimerTask(Timer _timer, final int period, Runnable r)
	{
		_timer.scheduleAtFixedRate(executeTimerTask(r), new Date(), period);
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