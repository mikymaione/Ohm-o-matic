package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.ManagedChannelBuilder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * A helper method that does the following things:
 * (1) Hashing - for string, for socket address, and integer number
 * (2) Computation - relative id (one node is how far behind another node),
 * a address' hex string and its percentage position in the ring (so we can
 * easily draw the picture of ring!), address' 8-digit hex string, the ith
 * start of a node's finger table, power of two (to avoid computation of power
 * of 2 everytime we need it)
 * (3) Network and address services - send request to a node to get desired
 * socket address/response, create socket address object using string, read
 * string from an input stream.
 *
 * @author Chuan Xia
 */

public class Helper
{

	private static HashMap<Integer, Long> powerOfTwo = null;

	/**
	 * Constructor
	 */
	public Helper()
	{
		//initialize power of two table
		powerOfTwo = new HashMap<Integer, Long>();

		long base = 1;

		for (var i = 0; i <= 32; i++)
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
		var ret = universal - local;

		if (ret < 0)
			ret += powerOfTwo.get(32);

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
		var hash = hashSocketAddress(addr);

		return (longTo8DigitHex(hash) + " (" + hash * 100 / Helper.getPowerOfTwo(32) + "%)");
	}

	/**
	 * @param l a long type number's 8-digit hex string
	 * @return
	 */
	public static String longTo8DigitHex(long l)
	{
		var hex = Long.toHexString(l);
		var lack = 8 - hex.length();
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
	public static long ithStart(long nodeid, int i)
	{
		return (nodeid + powerOfTwo.get(i - 1)) % powerOfTwo.get(32);
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
		var request = Helper.<String>sendRequest(server, req, localID, indirizzo);

		// if response is null, return null
		if (request == null)
			return null;
			// or server cannot find anything, return server itself
		else if (request.startsWith("NOTHING"))
			return server;
			// server find something,
			// using response to create, might fail then and return null
		else
			return Helper.createSocketAddress(request.split("_")[1]);
	}

	private static HomeServiceGrpc.HomeServiceBlockingStub getStub(InetSocketAddress destination)
	{
		var gRPC_channel = ManagedChannelBuilder
				.forAddress(destination.getAddress().getHostAddress(), destination.getPort())
				.usePlaintext()
				.build();

		return HomeServiceGrpc.newBlockingStub(gRPC_channel);
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
		var stub = getStub(destination);

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
				gestioneErroreRequest(R3);
				return (A) R3;

			default:
				throw new Exception("Switch " + req + " non implementato");
		}
	}

	/**
	 * Create InetSocketAddress using ip address and port number
	 *
	 * @param addr: socket address string, e.g. 127.0.0.1:8080
	 * @return created InetSocketAddress object;
	 * return null if:
	 * (1) not valid input
	 * (2) cannot find split input into ip and port strings
	 * (3) fail to parse ip address.
	 */
	public static InetSocketAddress createSocketAddress(String addr)
	{
		// input null, return null
		if (addr == null)
		{
			return null;
		}

		// split input into ip string and port string
		String[] splitted = addr.split(":");

		// can split string
		if (splitted.length >= 2)
		{
			//get and pre-process ip address string
			String ip = splitted[0];
			if (ip.startsWith("/"))
			{
				ip = ip.substring(1);
			}

			//parse ip address, if fail, return null
			InetAddress m_ip = null;
			try
			{
				m_ip = InetAddress.getByName(ip);
			}
			catch (UnknownHostException e)
			{
				System.out.println("Cannot create ip address: " + ip);
				return null;
			}

			// parse port number
			String port = splitted[1];
			int m_port = Integer.parseInt(port);

			// combine ip addr and port in socket address
			return new InetSocketAddress(m_ip, m_port);
		}

		// cannot split string
		else
		{
			return null;
		}

	}


}
