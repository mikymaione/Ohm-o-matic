/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Chord.Chord;
import OhmOMatic.Sistema.Casa;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
			final var mio_peer_port = stringToInt(cmd.getOptionValue("q"), -1);
			final var peer_address = cmd.getOptionValue("j");
			final var peer_port = stringToInt(cmd.getOptionValue("p"), -1);
			final var identificatore = cmd.getOptionValue("i");

			try (final var chord = new Chord(identificatore, mio_peer_address, mio_peer_port))
			{
				try (final var casa = new Casa(identificatore, rest_url, mio_peer_address, mio_peer_port, chord))
				{
					System.out.println("Casa " + identificatore + " avviata!");

					if (peer_port > -1)
						chord.join(peer_address, peer_port);
					else
						chord.join(mio_peer_address, mio_peer_port);

					//casa.iscriviCasa();
					System.out.println("Casa iscritta sul server!");

					//casa.entraNelCondominio();
					System.out.println("Casa nel condominio!");

					casa.avviaSmartMeter();
					System.out.println("Smart meter avviato!");

					try (var scanner = new Scanner(System.in))
					{
						LeggiComandiInterattivi(casa, chord, scanner);
					}

					casa.fermaSmartMeter();
					System.out.println("Smart meter fermato!");

					//casa.disiscriviCasa();
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
	private static void LeggiComandiInterattivi(Casa casa, Chord chord, Scanner scanner)
	{
		var inEsecuzione = true;

		do
		{
			final var commands = createOptionsInteractiveProgram();
			printOptions("", commands);

			final var line = scanner.nextLine();

			try
			{
				final var inpts = getCommandLine(commands, line.split(" "));

				if (inpts.hasOption("q"))
					inEsecuzione = false;
				else if (inpts.hasOption("i"))
					chord.stampaStato();
				else if (inpts.hasOption("d"))
					chord.stampaData();
				else if (inpts.hasOption("l"))
					chord.stampaListaPeer();
				else if (inpts.hasOption("b"))
					casa.boost();
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
				.hasArg(false)
				.build();

		final var info = Option.builder("i")
				.desc("Info")
				.hasArg(false)
				.build();

		final var data = Option.builder("d")
				.desc("Data")
				.hasArg(false)
				.build();

		final var list = Option.builder("l")
				.desc("List peer")
				.hasArg(false)
				.build();

		final var boost = Option.builder("b")
				.desc("Boost")
				.hasArg(false)
				.build();

		return new Options()
				.addOption(quit)
				.addOption(info)
				.addOption(data)
				.addOption(list)
				.addOption(boost);
	}

	private static Options createOptionsStartProgram()
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

		return new Options()
				.addOption(rest_url)
				.addOption(id)
				.addOption(mio_peer_address)
				.addOption(mio_peer_port)
				.addOption(peer_address)
				.addOption(peer_port);
	}
	//endregion


}