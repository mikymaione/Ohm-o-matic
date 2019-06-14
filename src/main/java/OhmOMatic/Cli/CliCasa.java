/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Sistema.Casa;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public final class CliCasa extends BaseCommandLineApplication
{

	public static void main(String[] args)
	{
		final var options = createOptions();
		printOptions("CliCasa", options);

		try
		{
			final var cmd = getCommandLine(options, args);

			final var id = cmd.getOptionValue("i");
			final var rest_url = cmd.getOptionValue("r");
			final var mio_peer_address = cmd.getOptionValue("k");
			final var mio_peer_port = stringToInt(cmd.getOptionValue("q"), -1);
			final var peer_address = cmd.getOptionValue("j");
			final var peer_port = stringToInt(cmd.getOptionValue("p"), -1);

			try (var casa = new Casa(rest_url, mio_peer_address, mio_peer_port, peer_address, peer_port))
			{
				System.out.println("Casa avviata!");

				//casa.iscriviCasa();
				System.out.println("Casa iscritta sul server!");

				casa.entraNelCondominio();
				System.out.println("Casa nel condominio!");

				casa.avviaSmartMeter();
				System.out.println("Smart meter avviato!");

				casa.fermaSmartMeter();
				System.out.println("Smart meter fermato!");

				casa.esciDalCondominio();
				System.out.println("Casa fuori dal condominio!");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	//region Opzioni command line
	private static Options createOptions()
	{
		final var rest_url = Option.builder("r")
				.desc("REST URL")
				.required()
				.hasArg()
				.argName("URL")
				.build();

		final var id = Option.builder("i")
				.desc("ID")
				.required()
				.hasArg()
				.argName("ID")
				.build();

		final var mio_peer_address = Option.builder("k")
				.desc("My P2P Address")
				.required()
				.hasArg()
				.argName("MyAddress")
				.build();

		final var mio_peer_port = Option.builder("q")
				.desc("My P2P Port")
				.required()
				.hasArg()
				.argName("MyPort")
				.build();

		final var peer_address = Option.builder("j")
				.desc("P2P Server Address")
				.hasArg()
				.argName("Address")
				.build();

		final var peer_port = Option.builder("p")
				.desc("P2P Server Port")
				.hasArg()
				.argName("Port")
				.build();


		final var options = new Options()
				.addOption(rest_url)
				.addOption(id)
				.addOption(mio_peer_address)
				.addOption(mio_peer_port)
				.addOption(peer_address)
				.addOption(peer_port);

		return options;
	}
	//endregion


}