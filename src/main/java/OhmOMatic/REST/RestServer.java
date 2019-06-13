/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST;

import OhmOMatic.Base.BaseCommandLineApplication;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public final class RestServer extends BaseCommandLineApplication
{

	//le rotte (e quindi le funzionalità) si trovano nel file OhmOMatic.REST.OOM.java

	private static final String Server_URI = "http://localhost:8080/OOM/";


	public static void main(String[] args)
	{
		var rc = new ResourceConfig()
				.packages("OhmOMatic");

		GrizzlyHttpServerFactory.createHttpServer(URI.create(Server_URI), rc);

		System.out.println("Ohm-o-matic - Server avviato!");
		System.out.println(String.format("Visita %s", Server_URI));
		System.out.println("Premere invio per terminare...");

		try
		{
			System.in.read();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


}