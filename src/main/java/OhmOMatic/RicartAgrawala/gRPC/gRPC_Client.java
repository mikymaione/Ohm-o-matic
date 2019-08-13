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
import OhmOMatic.ProtoBuffer.RicartAgrawalaOuterClass;
import OhmOMatic.RicartAgrawala.Enums.RichiestaRicartAgrawala;
import OhmOMatic.gRPC.FastStub;
import com.google.protobuf.ByteString;

import java.io.IOException;

public class gRPC_Client
{
	//region Ricart & Agrawala gRPC
	public static boolean gRPC(NodeLink server, RichiestaRicartAgrawala req, NodeLink me)
	{
		return gRPC(server, req, -1, me);
	}

	public static boolean gRPC(NodeLink server, RichiestaRicartAgrawala req, int our_sequence_number, NodeLink me)
	{
		if (server != null)
			try (final var hfs = new FastStub())
			{
				final var _oggetto = RicartAgrawalaOuterClass.mutualExMsg.newBuilder()
						.setNodeLink(ByteString.copyFrom(GB.serialize(me)))
						.setOurSequenceNumber(our_sequence_number)
						.build();

				final var stub = hfs.getRicartAgrawala(server);

				final Common.standardRes _request;
				switch (req)
				{
					case free:
						_request = stub.free(_oggetto);
						break;
					case enter:
						_request = stub.enter(_oggetto);
						break;
					case reply:
						_request = stub.reply(_oggetto);
						break;
					case request:
						_request = stub.request(_oggetto);
						break;
					default:
						throw new UnsupportedOperationException("Switch non implementato!");
				}

				return _request.getOk();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		return false;
	}
	//endregion


}