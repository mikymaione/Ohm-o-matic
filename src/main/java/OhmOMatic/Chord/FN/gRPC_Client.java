/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord.FN;

import OhmOMatic.ProtoBuffer.Home;
import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;

import java.math.BigInteger;

public class gRPC_Client
{

	public static NodeLink gRPC(NodeLink server, Richiesta req)
	{
		return gRPC(server, req, null, null);
	}

	public static NodeLink gRPC(NodeLink server, Richiesta req, BigInteger _id)
	{
		return gRPC(server, req, _id, null);
	}

	public static NodeLink gRPC(NodeLink server, Richiesta req, NodeLink setNode)
	{
		return gRPC(server, req, null, setNode);
	}

	private static NodeLink gRPC(NodeLink server, Richiesta req, BigInteger _id, NodeLink setNode)
	{
		try (var hfs = new HomeFastStub())
		{
			var _casa = Home.casa.newBuilder();

			if (_id != null)
				_casa.setID(ByteString.copyFrom(_id.toByteArray()));

			if (setNode != null)
			{
				_casa.setOptionalIP(setNode.IP);
				_casa.setOptionalPort(setNode.port);
			}

			_casa
					.setIP(server.IP)
					.setPort(server.port);

			var _request = doRequest(server, hfs, req, _casa.build());

			if (_request.getNullValue())
			{
				return null;
			}
			else if (_request.getStandardRes().getOk())
			{
				var c = _request.getCasa();

				return new NodeLink(c.getIP(), c.getPort());
			}
			else
			{
				return server;
			}
		}
		catch (StatusRuntimeException sre)
		{
			server.isDead = true;
		}
		catch (Exception er)
		{
			er.printStackTrace();
		}

		return null;
	}

	private static Home.casaRes doRequest(NodeLink server, HomeFastStub hfs, Richiesta req, Home.casa c)
	{
		var stub = hfs.getStub(server);

		switch (req)
		{
			case notify:
				return stub.notify(c);
			case findSuccessor:
				return stub.findSuccessor(c);
			case predecessor:
				return stub.predecessor(c);
			case ping:
				return stub.ping(c);
			default:
				throw new UnsupportedOperationException("Switch non implementato!");
		}
	}


}