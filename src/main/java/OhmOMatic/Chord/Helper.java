/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Helper
{

	public static final int mBit = 32;

	private static HashMap<Integer, Long> powerOfTwo = new HashMap<>();

	/**
	 * Constructor
	 */
	public Helper()
	{
		//initialize power of two table
		var base = 1L;

		for (var i = 0; i <= mBit; i++)
		{
			powerOfTwo.put(i, base);
			base *= 2;
		}
	}

	/**
	 * Compute a socket address' 32 bit identifier
	 *
	 * @param addr: socket address
	 * @return 32-bit identifier in long type
	 */
	public static long hashSocketAddress(InetSocketAddress addr)
	{
		int i = addr.hashCode();

		return hashHashCode(i);
	}

	/**
	 * Compute a string's 32 bit identifier
	 *
	 * @param s: string
	 * @return 32-bit identifier in long type
	 */
	public static long hashString(String s)
	{
		int i = s.hashCode();

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
		var hashbytes = new byte[4];
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

			var compressed = new byte[4];

			for (var j = 0; j < 4; j++)
			{
				byte temp = result[j];

				for (var k = 1; k < 5; k++)
					temp = (byte) (temp ^ result[j + k]);

				compressed[j] = temp;
			}

			long ret = (compressed[0] & 0xFF) << 24 | (compressed[1] & 0xFF) << 16 | (compressed[2] & 0xFF) << 8 | (compressed[3] & 0xFF);
			ret = ret & 0xFFFFFFFFl;

			return ret;
		}

		return 0;
	}

	/**
	 * Normalization, computer universal id's value relative to local id
	 * (regard local node as 0)
	 *
	 * @param universal: original/universal identifier
	 * @param local:     node's identifier
	 * @return relative identifier
	 */
	public static long computeRelativeId(long universal, long local)
	{
		long ret = universal - local;

		if (ret < 0)
			ret += powerOfTwo.get(Helper.mBit);

		return ret;
	}

	/**
	 * Compute a socket address' SHA-1 hash in hex
	 * and its approximate position in string
	 *
	 * @param addr
	 * @return
	 */
	public static String hexIdAndPosition(InetSocketAddress addr)
	{
		long hash = hashSocketAddress(addr);

		return (longTo8DigitHex(hash) + " (" + hash * 100 / Helper.getPowerOfTwo(Helper.mBit) + "%)");
	}

	/**
	 * @param l a long type number's 8-digit hex string
	 * @return
	 */
	public static String longTo8DigitHex(long l)
	{
		String hex = Long.toHexString(l);
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
	public static long iThStart(long nodeid, int i)
	{
		return (nodeid + powerOfTwo.get(i - 1)) % powerOfTwo.get(Helper.mBit);
	}

	/**
	 * Get power of 2
	 *
	 * @param k
	 * @return 2^k
	 */
	public static long getPowerOfTwo(int k)
	{
		return powerOfTwo.get(k);
	}

	/**
	 * Generate requested address by sending request to server
	 *
	 * @param server
	 * @param req:   request
	 * @return generated socket address,
	 * might be null if
	 * (1) invalid input
	 * (2) response is null (typically cannot send request)
	 * (3) fail to create address from reponse
	 */
	public static InetSocketAddress requestAddress(InetSocketAddress server, Richiesta req) throws Exception
	{
		return requestAddress(server, req, -1, "");
	}

	public static InetSocketAddress requestAddress(InetSocketAddress server, Richiesta req, long localID, String indirizzo) throws Exception
	{
		// invalid input, return null
		if (server == null || req == null)
			return null;

		// send request to server
		var request = Helper.<Home.casaRes>sendRequest(server, req, localID, indirizzo);

		// if response is null, return null
		if (request == null)
		{
			return null;
			// or server cannot find anything, return server itself
		}
		else if (request.getStandardRes().getMsg().equals("NOTHING"))
		{
			return server;
			// server find something,
			// using response to create, might fail then and return null
		}
		else
		{
			var c = request.getCasa();

			return new InetSocketAddress(c.getIP(), c.getPort());
		}
	}

	private static void gestioneErroreRequest(Common.standardRes R) throws Exception
	{
		if (R == null)
			throw new Exception("Errore richiesta!");

		if (!R.getOk())
			throw new Exception(R.getErrore());
	}

	public static <A> A sendRequest(InetSocketAddress destination, Richiesta req) throws Exception
	{
		return sendRequest(destination, req, -1, "");
	}

	public static <A> A sendRequest(InetSocketAddress destination, Richiesta req, long localID, String indirizzo) throws Exception
	{
		try (var hfs = new HomeFastStub())
		{
			var stub = hfs.getStub(destination);

			var c = Home.casa.newBuilder()
					.setIP(destination.getAddress().getHostAddress())
					.setPort(destination.getPort())
					.setID(localID)
					.build();

			switch (req)
			{
				case join:
					var Res = stub.join(c);
					var R1 = Res.getStandardRes();
					gestioneErroreRequest(R1);
					return (A) Res.getCasa();

				case esciDalCondominio:
					var R2 = stub.esciDalCondominio(c);
					gestioneErroreRequest(R2);
					return (A) R2;

				case FINDSUCC_:
					var R3 = stub.fINDSUCC(c);
					gestioneErroreRequest(R3.getStandardRes());
					return (A) R3;

				case IAMPRE_:
					var R4 = stub.iAMPRE(c);
					gestioneErroreRequest(R4.getStandardRes());
					return (A) R4;

				case KEEP:
					var R5 = stub.kEEP(c);
					gestioneErroreRequest(R5);
					return (A) R5;

				case YOURPRE:
					var R6 = stub.yOURPRE(c);
					gestioneErroreRequest(R6.getStandardRes());
					return (A) R6;

				case YOURSUCC:
					var R7 = stub.yOURPRE(c);
					gestioneErroreRequest(R7.getStandardRes());
					return (A) R7;

				case CLOSEST_:
					var R8 = stub.yOURPRE(c);
					gestioneErroreRequest(R8.getStandardRes());
					return (A) R8;

				default:
					throw new Exception("Switch " + req + " non implementato");
			}
		}
	}


}
