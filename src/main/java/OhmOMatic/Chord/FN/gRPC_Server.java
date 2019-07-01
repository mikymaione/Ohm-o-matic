/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord.FN;

import OhmOMatic.Chord.Chord;
import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.math.BigInteger;
import java.util.function.Function;

public class gRPC_Server
{

	public static HomeServiceGrpc.HomeServiceImplBase getListnerServer(Chord local)
	{
		return new HomeServiceGrpc.HomeServiceImplBase()
		{
			private void PROC(Home.casa request, StreamObserver<Home.casaRes> responseObserver, java.util.function.Consumer<NodeLink> pro)
			{
				var _standardRes = Common.standardRes.newBuilder();
				var _casa = Home.casa.newBuilder();
				var _casaRes = Home.casaRes.newBuilder();

				try
				{
					var n_ = (request.getOptionalPort() > 0
							? new NodeLink(request.getOptionalIP(), request.getOptionalPort())
							: null);

					pro.accept(n_);

					_standardRes
							.setOk(true);
				}
				catch (Exception e)
				{
					_standardRes
							.setOk(false)
							.setErrore(e.getMessage());
				}

				_casaRes
						.setStandardRes(_standardRes.build())
						.setCasa(_casa.build())
						.setNullValue(false);

				responseObserver.onNext(_casaRes.build());
				responseObserver.onCompleted();
			}

			private void FUNC(Home.casa request, StreamObserver<Home.casaRes> responseObserver, Function<NodeLink, NodeLink> fun)
			{
				var _standardRes = Common.standardRes.newBuilder();
				var _casa = Home.casa.newBuilder();
				var _casaRes = Home.casaRes.newBuilder();

				try
				{
					var n_ = new NodeLink(request.getOptionalIP(), request.getOptionalPort());

					var R = fun.apply(n_);

					if (R == null)
						_casaRes
								.setNullValue(true);
					else
						_casa
								.setIP(R.IP)
								.setPort(R.port);

					_standardRes
							.setOk(true);
				}
				catch (Exception e)
				{
					_standardRes
							.setOk(false)
							.setErrore(e.getMessage());
				}

				_casaRes
						.setStandardRes(_standardRes.build())
						.setCasa(_casa.build());

				responseObserver.onNext(_casaRes.build());
				responseObserver.onCompleted();
			}

			@Override
			public void notify(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				PROC(request, responseObserver, n ->
						local.notify(n));
			}

			@Override
			public void ping(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				FUNC(request, responseObserver, n ->
						local.ping());
			}

			@Override
			public void findSuccessor(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				FUNC(request, responseObserver, n ->
						local.find_successor(new BigInteger(request.getID().toByteArray())));
			}

			@Override
			public void predecessor(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				FUNC(request, responseObserver, n ->
						local.getPredecessor());
			}

		};
	}


}