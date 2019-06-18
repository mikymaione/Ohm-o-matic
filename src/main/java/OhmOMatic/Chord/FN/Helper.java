/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord.FN;

import OhmOMatic.Chord.Node;
import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Helper
{

	public static final int mBit = 32;

	private static HashMap<Integer, Long> _powerOfTwo = new HashMap<>();


	public static long getPowerOfTwo(int k)
	{
		if (_powerOfTwo.size() == 0)
		{
			var base = 1L;

			for (var i = 0; i <= mBit; i++)
			{
				_powerOfTwo.put(i, base);
				base *= 2;
			}
		}

		return _powerOfTwo.get(k);
	}

	public static long hashNodeLink(NodeLink addr)
	{
		int i = addr.hashCode();

		return hashHashCode(i);
	}

	public static long hashString(String s)
	{
		int i = s.hashCode();

		return hashHashCode(i);
	}

	private static long hashHashCode(int i)
	{
		var hashbytes = new byte[4];
		hashbytes[0] = (byte) (i >> 24);
		hashbytes[1] = (byte) (i >> 16);
		hashbytes[2] = (byte) (i >> 8);
		hashbytes[3] = (byte) (i /*>> 0*/);

		MessageDigest md = null;
		try
		{
			md = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

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

	public static long computeRelativeId(long universal, long local)
	{
		long ret = universal - local;

		if (ret < 0)
			ret += getPowerOfTwo(Helper.mBit);

		return ret;
	}

	public static String hexIdAndPosition(NodeLink addr)
	{
		long hash = hashNodeLink(addr);

		return (longTo8DigitHex(hash) + " (" + hash * 100 / Helper.getPowerOfTwo(Helper.mBit) + "%)");
	}

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

	public static long iThStart(long nodeid, int i)
	{
		return (nodeid + getPowerOfTwo(i - 1)) % getPowerOfTwo(Helper.mBit);
	}

	public static NodeLink requestAddress(NodeLink server, Richiesta req) throws Exception
	{
		return requestAddress(server, req, -1, "");
	}

	public static NodeLink requestAddress(NodeLink server, Richiesta req, long localID, String indirizzo) throws Exception
	{
		if (server == null || req == null)
			return null;

		var request = Helper.<Home.casaRes>sendRequest(server, req, localID, indirizzo);

		if (request == null)
		{
			return null;
		}
		else if (request.getStandardRes().getMsg().equals("NOTHING"))
		{
			return server;
		}
		else
		{
			var c = request.getCasa();

			return new NodeLink(c.getIP(), c.getPort());
		}
	}

	private static void gestioneErroreRequest(Common.standardRes R) throws Exception
	{
		if (R == null)
			throw new Exception("Errore richiesta!");

		if (!R.getOk())
			throw new Exception(R.getErrore());
	}

	public static <A> A sendRequest(NodeLink destination, Richiesta req) throws Exception
	{
		return sendRequest(destination, req, -1, "");
	}

	public static <A> A sendRequest(NodeLink destination, Richiesta req, long localID, String indirizzo) throws Exception
	{
		try (var hfs = new HomeFastStub())
		{
			var stub = hfs.getStub(destination);

			var c = Home.casa.newBuilder()
					.setIP(destination.IP)
					.setPort(destination.port)
					.setID(localID)
					.build();

			switch (req)
			{
				case join:
					throw new UnsupportedOperationException();

				case esciDalCondominio:
					throw new UnsupportedOperationException();

				case FindSuccessor:
					var R3 = stub.fINDSUCC(c);
					gestioneErroreRequest(R3.getStandardRes());
					return (A) R3;

				case ImPredecessor:
					var R4 = stub.iAMPRE(c);
					gestioneErroreRequest(R4.getStandardRes());
					return (A) R4;

				case Ping:
					var R5 = stub.kEEP(c);
					gestioneErroreRequest(R5);
					return (A) R5;

				case Predecessor:
					var R6 = stub.yOURPRE(c);
					gestioneErroreRequest(R6.getStandardRes());
					return (A) R6;

				case Successor:
					var R7 = stub.yOURPRE(c);
					gestioneErroreRequest(R7.getStandardRes());
					return (A) R7;

				case ClosestPrecedingFinger:
					var R8 = stub.yOURPRE(c);
					gestioneErroreRequest(R8.getStandardRes());
					return (A) R8;

				default:
					throw new Exception("Switch " + req + " non implementato");
			}
		}
		catch (io.grpc.StatusRuntimeException sre)
		{
			throw sre;
		}
	}

	public static HomeServiceGrpc.HomeServiceImplBase getListnerServer(Node local)
	{
		return new HomeServiceGrpc.HomeServiceImplBase()
		{
			@Override
			public void cLOSEST(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				Common.standardRes sr;
				var cr = Home.casa.newBuilder()
						.build();

				try
				{
					var result = local.closest_preceding_finger(request.getID());
					var _ip = result.IP;
					var _port = result.port;
					var ret = "MYCLOSEST_" + _ip + ":" + _port;

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setMsg(ret)
							.build();

					cr = Home.casa.newBuilder()
							.setIP(_ip)
							.setPort(_port)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setErrore(e.getMessage())
							.build();
				}

				var res = Home.casaRes.newBuilder()
						.setStandardRes(sr)
						.setCasa(cr)
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}

			@Override
			public void fINDSUCC(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				Common.standardRes sr;
				var cr = Home.casa.newBuilder()
						.build();

				try
				{
					var result = local.find_successor(request.getID());
					var _ip = result.IP;
					var _port = result.port;
					var ret = "FOUNDSUCC_" + _ip + ":" + _port;

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setMsg(ret)
							.build();

					cr = Home.casa.newBuilder()
							.setIP(_ip)
							.setPort(_port)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setErrore(e.getMessage())
							.build();
				}

				var res = Home.casaRes.newBuilder()
						.setStandardRes(sr)
						.setCasa(cr)
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}

			@Override
			public void iAMPRE(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				Common.standardRes sr;
				var cr = Home.casa.newBuilder()
						.build();

				try
				{
					var new_pre = new NodeLink(request.getIP(), request.getPort());

					local.notified(new_pre);

					var ret = "NOTIFIED";

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setMsg(ret)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setErrore(e.getMessage())
							.build();
				}

				var res = Home.casaRes.newBuilder()
						.setStandardRes(sr)
						.setCasa(cr)
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}

			@Override
			public void kEEP(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
			{
				var res = Common.standardRes.newBuilder()
						.setOk(true)
						.setMsg("ALIVE")
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}

			@Override
			public void requestAddress(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
			{
				super.requestAddress(request, responseObserver);
			}

			@Override
			public void yOURPRE(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				Common.standardRes sr;
				var cr = Home.casa.newBuilder()
						.build();

				try
				{
					var ret = "";
					var result = local.getPredecessor();

					if (result != null)
					{
						var _ip = result.IP;
						var _port = result.port;

						ret = "MYPRE_" + _ip + ":" + _port;

						cr = Home.casa.newBuilder()
								.setIP(_ip)
								.setPort(_port)
								.build();
					}
					else
					{
						ret = "NOTHING";
					}

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setMsg(ret)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setErrore(e.getMessage())
							.build();
				}

				var res = Home.casaRes.newBuilder()
						.setStandardRes(sr)
						.setCasa(cr)
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}

			@Override
			public void yOURSUCC(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				Common.standardRes sr;
				var cr = Home.casa.newBuilder()
						.build();

				try
				{
					var ret = "";
					var result = local.getSuccessor();

					if (result != null)
					{
						var _ip = result.IP;
						var _port = result.port;
						ret = "MYSUCC_" + _ip + ":" + _port;

						cr = Home.casa.newBuilder()
								.setIP(_ip)
								.setPort(_port)
								.build();
					}
					else
					{
						ret = "NOTHING";
					}

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setMsg(ret)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.setErrore(e.getMessage())
							.build();
				}

				var res = Home.casaRes.newBuilder()
						.setStandardRes(sr)
						.setCasa(cr)
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}


			@Override
			public void join(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void esciDalCondominio(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
			{
				throw new UnsupportedOperationException();
			}
		};
	}


}
