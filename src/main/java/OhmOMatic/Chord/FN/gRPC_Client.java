/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord.FN;

import OhmOMatic.ProtoBuffer.Home;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

public class gRPC_Client
{

	public static NodeLink gRPC(NodeLink server, Richiesta req)
	{
		return gRPC(server, req, null, null, null);
	}

	public static NodeLink gRPC(NodeLink server, Richiesta req, BigInteger _id)
	{
		return gRPC(server, req, _id, null, null);
	}

	public static NodeLink gRPC(NodeLink server, Richiesta req, NodeLink setNode)
	{
		return gRPC(server, req, null, null, setNode);
	}

	private static NodeLink gRPC(NodeLink server, Richiesta req, BigInteger _id, Integer indice, NodeLink setNode)
	{
		try
		{
			var response = gRPC_Execute(server, req, _id, indice, setNode);

			if (response.getNullValue())
			{
				return null;
			}
			else if (response.getStandardRes().getOk())
			{
				var c = response.getCasa();

				return new NodeLink(c.getIP(), c.getPort());
			}
			else
			{
				return server;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static Home.casaRes gRPC_Execute(NodeLink server, Richiesta req, BigInteger _id, Integer indice, NodeLink setNode) throws Exception
	{
		try (var hfs = new HomeFastStub())
		{
			var casa_B = Home.casa.newBuilder();

			if (_id != null)
				casa_B.setID(ByteString.copyFrom(_id.toByteArray()));

			if (indice != null)
				casa_B.setIdx(indice);

			if (setNode != null)
			{
				casa_B.setOptionalIP(setNode.IP);
				casa_B.setOptionalPort(setNode.port);
			}

			casa_B
					.setIP(server.IP)
					.setPort(server.port);

			return gestioneErroreRequest(
					doRequest(server, hfs, req, casa_B.build())
			);
		}
	}

	private static Home.casaRes doRequest(NodeLink server, HomeFastStub hfs, Richiesta req, Home.casa c) throws Exception
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
				throw new Exception("Switch " + req + " non implementato");
		}
	}

	private static Home.casaRes gestioneErroreRequest(Home.casaRes CR) throws Exception
	{
		var R = CR.getStandardRes();

		if (R == null)
			throw new Exception("Errore richiesta!");

		if (!R.getOk())
			throw new Exception(R.getErrore());

		return CR;
	}


}