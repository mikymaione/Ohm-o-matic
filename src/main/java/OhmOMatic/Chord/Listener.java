/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Listener extends Thread
{

	private Server gRPC_listner;
	private Node local;
	private final int port;


	public Listener(Node _local)
	{
		var localAddress = _local.getAddress();
		port = localAddress.port;
		local = _local;
	}

	public void close()
	{
		gRPC_listner.shutdown();
	}

	@Override
	public void run()
	{
		try
		{
			gRPC_listner = ServerBuilder
					.forPort(port)
					.addService(Helper.getListnerServer(local))
					.build()
					.start();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot talk.\nServer port: " + local.getAddress().port + "; Talker port: " + port, e);
		}
	}


}