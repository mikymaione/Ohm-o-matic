/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord.FN;

import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class gRPCCommander
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
			ret += getPowerOfTwo(gRPCCommander.mBit);

		return ret;
	}

	public static String hexIdAndPosition(NodeLink addr)
	{
		long hash = hashNodeLink(addr);

		return (longTo8DigitHex(hash) + " (" + hash * 100 / gRPCCommander.getPowerOfTwo(gRPCCommander.mBit) + "%)");
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
		return (nodeid + getPowerOfTwo(i - 1)) % getPowerOfTwo(gRPCCommander.mBit);
	}

	public static NodeLink requestAddress(NodeLink server, Richiesta req) throws Exception
	{
		return requestAddress(server, req, -1);
	}

	public static NodeLink requestAddress(NodeLink server, Richiesta req, long localID) throws Exception
	{
		return requestAddress(server, req, localID, NodeLink.Empty());
	}

	public static NodeLink requestAddress(NodeLink server, Richiesta req, long localID, NodeLink optional) throws Exception
	{
		if (server == null || req == null)
			return null;

		var response = gRPCCommander.<Home.casaRes>sendRequest(server, req, localID, optional);

		if (response == null)
		{
			return null;
		}
		else if (!response.getStandardRes().getOk())
		{
			return server;
		}
		else
		{
			var c = response.getCasa();

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

	public static <A> A sendRequest(NodeLink server, Richiesta req) throws Exception
	{
		return sendRequest(server, req, -1, NodeLink.Empty());
	}

	public static <A> A sendRequest(NodeLink server, Richiesta req, long localID, NodeLink optional) throws Exception
	{
		try (var hfs = new HomeFastStub())
		{
			var stub = hfs.getStub(server);

			var c = Home.casa.newBuilder()
					.setIP(server.IP)
					.setPort(server.port)
					.setID(localID)
					.setOptionalIP(optional.IP)
					.setOptionalPort(optional.port)
					.build();

			Home.casaRes CR;
			Common.standardRes R;

			switch (req)
			{
				case join:
					throw new UnsupportedOperationException();

				case esciDalCondominio:
					throw new UnsupportedOperationException();

				case Ping:
					R = stub.kEEP(c);
					gestioneErroreRequest(R);
					return (A) R;

				case ClosestPrecedingFinger:
					CR = stub.cLOSEST(c);
					break;

				case FindSuccessor:
					CR = stub.fINDSUCC(c);
					break;

				case ImPredecessor:
					CR = stub.iAMPRE(c);
					break;

				case Predecessor:
					CR = stub.yOURPRE(c);
					break;

				case Successor:
					CR = stub.yOURSUCC(c);
					break;

				default:
					throw new Exception("Switch " + req + " non implementato");
			}

			R = CR.getStandardRes();
			gestioneErroreRequest(R);

			return (A) CR;
		}
		catch (io.grpc.StatusRuntimeException sre)
		{
			throw sre;
		}
	}

	public static HomeServiceGrpc.HomeServiceImplBase getListnerServer(NodeLink local)
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

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.build();

					cr = Home.casa.newBuilder()
							.setIP(_ip)
							.setPort(_port)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(false)
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

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.build();

					cr = Home.casa.newBuilder()
							.setIP(_ip)
							.setPort(_port)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(false)
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
					var new_pre = new NodeLink(request.getOptionalIP(), request.getOptionalPort());

					local.notified(new_pre);

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(false)
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
					var result = local.getPredecessor();
					var ok = (result != null);

					if (ok)
					{
						var _ip = result.IP;
						var _port = result.port;

						cr = Home.casa.newBuilder()
								.setIP(_ip)
								.setPort(_port)
								.build();
					}

					sr = Common.standardRes.newBuilder()
							.setOk(ok)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(false)
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
					var result = local.getSuccessor();
					var ok = (result != null);

					if (ok)
					{
						var _ip = result.IP;
						var _port = result.port;

						cr = Home.casa.newBuilder()
								.setIP(_ip)
								.setPort(_port)
								.build();
					}

					sr = Common.standardRes.newBuilder()
							.setOk(ok)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(false)
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
