/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST;

import OhmOMatic.Base.BaseCommandLineApplication;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.Scanner;

public final class RestServer extends BaseCommandLineApplication
{

	// Le rotte (e quindi le funzionalità) si trovano nel file OhmOMatic.REST.OOM.java
	private static final String Server_URI = "http://localhost:8080/OOM/";


	public static void main(String[] args)
	{
		final var rc = new ResourceConfig()
				.packages("OhmOMatic");

		final var server = GrizzlyHttpServerFactory.createHttpServer(URI.create(Server_URI), rc);

		System.out.println("Ohm-o-matic - Server avviato!");
		System.out.println(String.format("Visita %s", Server_URI));

		try (final var scanner = new Scanner(System.in))
		{
			LeggiComandiInterattivi(scanner);
		}

		server.shutdown();
	}


	//region Opzioni command line
	private static void LeggiComandiInterattivi(Scanner scanner)
	{
		final var commands = createOptionsInteractiveProgram();

		var inEsecuzione = true;

		do
		{
			printOptions(commands);

			final var line = scanner.nextLine();

			try
			{
				final var cmd = getCommandLine(commands, line.split(" "));

				if (cmd.hasOption("q"))
					inEsecuzione = false;
			}
			catch (ParseException e)
			{
				System.out.println("Il comando " + line + " non esiste!");
			}
		}
		while (inEsecuzione);
	}

	private static Options createOptionsInteractiveProgram()
	{
		final var quit = Option.builder("q")
				.desc("Quit")
				.build();

		return new Options()
				.addOption(quit);
	}
	//endregion


}