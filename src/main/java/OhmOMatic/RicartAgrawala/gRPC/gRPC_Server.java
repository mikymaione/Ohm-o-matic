/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.RicartAgrawala.gRPC;

import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Global.GB;
import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.RicartAgrawalaGrpc;
import OhmOMatic.ProtoBuffer.RicartAgrawalaOuterClass;
import OhmOMatic.RicartAgrawala.MutualExclusion;
import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

public class gRPC_Server
{

	static class reqParam
	{
		NodeLink n;
		int sequence_number;

		reqParam(final NodeLink n, final int sequence_number)
		{
			this.n = n;
			this.sequence_number = sequence_number;
		}
	}

	public static RicartAgrawalaGrpc.RicartAgrawalaImplBase getListnerServer(MutualExclusion local)
	{
		return new RicartAgrawalaGrpc.RicartAgrawalaImplBase()
		{

			private void elabora(RicartAgrawalaOuterClass.mutualExMsg request, StreamObserver<Common.standardRes> responseObserver, Consumer<reqParam> callback)
			{
				var _standardRes = Common.standardRes.newBuilder();

				try
				{
					final var nodo = GB.<NodeLink>deserializeT(request.getNodeLink().toByteArray());
					final var our_sequence_number = request.getOurSequenceNumber();

					callback.accept(new reqParam(nodo, our_sequence_number));

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

				responseObserver.onNext(_standardRes.build());
				responseObserver.onCompleted();
			}

			@Override
			public void free(RicartAgrawalaOuterClass.mutualExMsg request, StreamObserver<Common.standardRes> responseObserver)
			{
				elabora(request, responseObserver, p -> local.free(p.n));
			}

			@Override
			public void enter(RicartAgrawalaOuterClass.mutualExMsg request, StreamObserver<Common.standardRes> responseObserver)
			{
				elabora(request, responseObserver, p -> local.enter(p.n));
			}

			@Override
			public void reply(RicartAgrawalaOuterClass.mutualExMsg request, StreamObserver<Common.standardRes> responseObserver)
			{
				elabora(request, responseObserver, p -> local.reply());
			}

			@Override
			public void request(RicartAgrawalaOuterClass.mutualExMsg request, StreamObserver<Common.standardRes> responseObserver)
			{
				elabora(request, responseObserver, p -> local.request(p.sequence_number, p.n));
			}

		};
	}


}