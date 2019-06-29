/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

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

	public static String sha1(String input)
	{
		var sb = new StringBuffer();

		try
		{
			var mDigest = MessageDigest.getInstance("SHA1");

			byte[] result = mDigest.digest(input.getBytes());

			for (var i = 0; i < result.length; i++)
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));

			return sb.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();

			return null;
		}
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