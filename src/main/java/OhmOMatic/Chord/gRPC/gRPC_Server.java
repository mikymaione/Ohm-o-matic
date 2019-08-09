/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord.gRPC;

import OhmOMatic.Chord.Chord;
import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Global.GB;
import OhmOMatic.Global.Pair;
import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.function.Function;

public class gRPC_Server
{

	public static HomeServiceGrpc.HomeServiceImplBase getListnerServer(Chord local)
	{
		return new HomeServiceGrpc.HomeServiceImplBase()
		{

			//region Funzioni Chord
			private void elaboraChord(Home.messaggio request, StreamObserver<Home.casaRes> responseObserver, Function<NodeLink, NodeLink> callback)
			{
				var _standardRes = Common.standardRes.newBuilder();
				var _casa = Home.casa.newBuilder();
				var _casaRes = Home.casaRes.newBuilder();

				try
				{
					final var destinatario = request.getDestinatario();
					final var n_ = new NodeLink(destinatario.getIdentificatore(), destinatario.getIP(), destinatario.getPort());

					final var R = callback.apply(n_);

					if (R == null)
						_casaRes
								.setNullValue(true);
					else
						_casa
								.setIdentificatore(R.identificatore)
								.setIP(R.IP)
								.setPort(R.port);

					_standardRes
							.setOk(true);
				}
				catch (Exception e)
				{
					final var _errorMessage = e.getMessage();

					if (_errorMessage != null)
						_standardRes.setErrore(_errorMessage);

					_standardRes.setOk(false);
				}

				_casaRes
						.setStandardRes(_standardRes.build())
						.setCasa(_casa.build());

				responseObserver.onNext(_casaRes.build());
				responseObserver.onCompleted();
			}


			@Override
			public void notify(Home.messaggio request, StreamObserver<Home.casaRes> responseObserver)
			{
				elaboraChord(request, responseObserver, local::notify);
			}

			@Override
			public void ping(Home.messaggio request, StreamObserver<Home.casaRes> responseObserver)
			{
				elaboraChord(request, responseObserver, n ->
						local.ping());
			}

			@Override
			public void findSuccessor(Home.messaggio request, StreamObserver<Home.casaRes> responseObserver)
			{
				elaboraChord(request, responseObserver, n ->
				{
					final var mittente = request.getMittente();
					final var mID = mittente.getID();

					return local.find_successor(new BigInteger(mID.toByteArray()));
				});
			}

			@Override
			public void getPredecessor(Home.messaggio request, StreamObserver<Home.casaRes> responseObserver)
			{
				elaboraChord(request, responseObserver, n ->
						local.getPredecessor());
			}
			//endregion

			//region Funzioni DHT
			private void elaboraDHT(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver, Function<Pair<BigInteger, Serializable>, Serializable> callback)
			{
				var _standardRes = Common.standardRes.newBuilder();
				var _oggetto = Home.oggetto.newBuilder();
				var _oggettoRes = Home.oggettoRes.newBuilder();

				try
				{
					final var _key = new BigInteger(request.getKey().toByteArray());
					final var _obj = GB.deserialize(request.getObj().toByteArray());

					final var R = callback.apply(new Pair<>(_key, _obj));

					_oggetto
							.setKey(request.getKey())
							.setObj(ByteString.copyFrom(GB.serialize(R)));

					_standardRes
							.setOk(true);
				}
				catch (Exception e)
				{
					final var _errorMessage = e.getMessage();

					if (_errorMessage != null)
						_standardRes.setErrore(_errorMessage);

					_standardRes.setOk(false);
				}

				_oggettoRes
						.setObj(_oggetto)
						.setStandardRes(_standardRes);

				responseObserver.onNext(_oggettoRes.build());
				responseObserver.onCompleted();
			}


			@Override
			public void getPeerList(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.getPeerList());
			}

			@Override
			public void addToPeerList(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.addToPeerList(e.getKey(), e.getValue()));
			}

			@Override
			public void removeFromPeerList(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.removeFromPeerList(e.getKey(), e.getValue()));
			}


			@Override
			public void transfer(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.transfer(e.getKey(), e.getValue()));
			}

			@Override
			public void incBigInteger(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.incBigInteger(e.getKey()));
			}

			@Override
			public void get(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.get(e.getKey()));
			}

			@Override
			public void put(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.put(e.getKey(), e.getValue()));
			}

			@Override
			public void remove(Home.oggetto request, StreamObserver<Home.oggettoRes> responseObserver)
			{
				elaboraDHT(request, responseObserver, e ->
						local.remove(e.getKey()));
			}
			//endregion

		};
	}


}