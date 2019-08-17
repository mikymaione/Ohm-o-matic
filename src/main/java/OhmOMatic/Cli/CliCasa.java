/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Chord.Chord;
import OhmOMatic.Global.GB;
import OhmOMatic.Sistema.Casa;
import org.apache.commons.cli.Options;

import java.util.Scanner;

public final class CliCasa extends BaseCommandLineApplication
{

	public static void main(String[] args)
	{
		final var options = createOptionsStartProgram();
		printOptions("CliCasa", options);

		try
		{
			final var cmd = getCommandLine(options, args);

			final var rest_url = cmd.getOptionValue("r");
			final var mio_peer_address = cmd.getOptionValue("k");
			final var mio_peer_port = GB.stringToInt(cmd.getOptionValue("q"), -1);
			final var peer_address = cmd.getOptionValue("j");
			final var peer_port = GB.stringToInt(cmd.getOptionValue("p"), -1);
			final var identificatore = cmd.getOptionValue("i");

			try (
					final var chord = new Chord(identificatore, mio_peer_address, mio_peer_port);
					final var casa = new Casa(identificatore, rest_url, mio_peer_address, mio_peer_port, chord)
			)
			{
				System.out.println("Casa " + identificatore + " avviata!");

				if (casa.iscriviCasa(identificatore, mio_peer_address, mio_peer_port))
				{
					System.out.println("Casa iscritta sul server!");

					if (peer_port > -1)
						chord.join(peer_address, peer_port);
					else
						chord.join(mio_peer_address, mio_peer_port);

					System.out.println("Casa nel condominio!");

					casa.avviaSmartMeter();
					System.out.println("Smart meter avviato!");

					try (final var scanner = new Scanner(System.in))
					{
						LeggiComandiInterattivi(scanner, CliCasa::createOptionsInteractiveProgram, inpts ->
						{
							if (inpts.hasOption("q"))
								return false;
							else if (inpts.hasOption("i"))
								chord.stampaStato();
							else if (inpts.hasOption("d"))
								chord.stampaData();
							else if (inpts.hasOption("l"))
								chord.stampaListaPeer();
							else if (inpts.hasOption("b"))
								casa.boost();

							return true;
						});
					}

					casa.fermaSmartMeter();
					System.out.println("Smart meter fermato!");

					if (casa.disiscriviCasa(identificatore, mio_peer_address, mio_peer_port))
						System.out.println("Casa fuori dal condominio!");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Fine.");
	}

	//region Opzioni command line
	private static Options createOptionsInteractiveProgram()
	{
		return new Options()
				.addOption(OP("q", "Quit"))
				.addOption(OP("i", "Info"))
				.addOption(OP("d", "Data"))
				.addOption(OP("l", "List peer"))
				.addOption(OP("b", "Boost"));
	}

	private static Options createOptionsStartProgram()
	{
		return new Options()
				.addOption(OP("r", "REST URL", "URL", true))
				.addOption(OP("i", "ID", "ID", true))
				.addOption(OP("k", "My P2P Address", "Address", true))
				.addOption(OP("q", "My P2P Port", "Port", true))
				.addOption(OP("j", "P2P Server Address", "Address", false))
				.addOption(OP("p", "P2P Server Port", "Port", false));
	}
	//endregion


}