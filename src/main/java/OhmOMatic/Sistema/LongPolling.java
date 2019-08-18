/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Implementazione in Java di Long polling:
-Long polling is itself not a true push; long polling is a variation of the traditional polling technique, but it allows emulating a push mechanism under circumstances where a real push is not possible, such as sites with security policies that require rejection of incoming HTTP/S Requests.
-With long polling, the client requests information from the server exactly as in normal polling, but with the expectation the server may not respond immediately. If the server has no new information for the client when the poll is received, instead of sending an empty response, the server holds the request open and waits for response information to become available. Once it does have new information, the server immediately sends an HTTP/S response to the client, completing the open HTTP/S Request. Upon receipt of the server response, the client often immediately issues another server request. In this way the usual response latency (the time between when the information first becomes available and the next client request) otherwise associated with polling clients is eliminated.
-For example, BOSH is a popular, long-lived HTTP technique used as a long-polling alternative to a continuous TCP connection when such a connection is difficult or impossible to employ directly (e.g., in a web browser); it is also an underlying technology in the XMPP, which Apple uses for its iCloud push support.
-Fonte: http://en.wikipedia.org/wiki/Push_technology#Long_polling
*/
package OhmOMatic.Sistema;

import OhmOMatic.Global.GB;
import OhmOMatic.Sistema.gRPC.gRPCtoRESTserver;

public class LongPolling implements AutoCloseable
{

	private final gRPCtoRESTserver _gRPCtoRESTserver;
	private final Thread longPolling;
	private boolean inEsecuzione = true;

	private final Object sharedVars = new Object();


	public LongPolling(final String indirizzoREST)
	{
		_gRPCtoRESTserver = new gRPCtoRESTserver(indirizzoREST);
		longPolling = new Thread(this::doPolling);
	}

	@Override
	public void close()
	{
		stop();

		_gRPCtoRESTserver.close();
	}

	public void start()
	{
		longPolling.start();
	}

	public void stop()
	{
		synchronized (sharedVars)
		{
			inEsecuzione = false;
		}

		longPolling.stop();
	}

	private void start()
	{
		GB.loopUntil(() ->
		{
			_gRPCtoRESTserver.getNotifiche();

			synchronized (sharedVars)
			{
				return !inEsecuzione;
			}
		});
	}


}