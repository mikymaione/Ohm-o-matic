/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Chord.Chord;
import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Sistema.Casa;
import org.apache.commons.cli.Option;
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

			final var id = cmd.getOptionValue("i");
			final var rest_url = cmd.getOptionValue("r");
			final var mio_peer_address = cmd.getOptionValue("k");
			final var mio_peer_port = stringToInt(cmd.getOptionValue("q"), -1);
			final var peer_address = cmd.getOptionValue("j");
			final var peer_port = stringToInt(cmd.getOptionValue("p"), -1);

			try (var casa = new Casa(rest_url, mio_peer_address, mio_peer_port, peer_address, peer_port))
			{
				System.out.println("Casa avviata!");

				var meLink = new NodeLink(mio_peer_address, mio_peer_port);

				try (var chord = new Chord(meLink))
				{
					if (peer_port > -1)
						chord.join(new NodeLink(peer_address, peer_port));
					else
						chord.join(meLink);

					//casa.iscriviCasa();
					System.out.println("Casa iscritta sul server!");

					//casa.entraNelCondominio();
					System.out.println("Casa nel condominio!");


					// print join info
					System.out.println("Joining the Chord ring.");
					System.out.println("Local IP: " + mio_peer_address + ":" + mio_peer_port);
					chord.printNeighbors();

					try (var scanner = new Scanner(System.in))
					{
						var Esecuzione = true;
						while (Esecuzione)
						{
							final var commands = createOptionsInteractiveProgram();
							printOptions("", commands);

							final var line = scanner.nextLine();
							final var inpts = getCommandLine(commands, line.split(" "));

							if (inpts.hasOption("q"))
							{
								Esecuzione = false;
							}
							else if (inpts.hasOption("i"))
							{
								chord.printDataStructure();
							}
							else if (inpts.hasOption("r"))
							{
								final var key = stringToBigInteger(inpts.getOptionValue("r"), -1);

								System.out.println("-Rimosso oggetto " + key + ": " + chord.remove(key));
							}
							else if (inpts.hasOption("g"))
							{
								final var key = stringToBigInteger(inpts.getOptionValue("g"), -1);

								System.out.println("-Ottenuto oggetto " + key + ": " + chord.get(key));
							}
							else if (inpts.hasOption("p"))
							{
								final var set = inpts.getOptionValues("p");
								final var key = stringToBigInteger(set[0], -1);
								final var obj = set[1];

								System.out.println("-Inserito oggetto " + key + ": " + chord.put(key, obj));
							}
						}

//					casa.avviaSmartMeter();
//					System.out.println("Smart meter avviato!");
//
//					//casa.fermaSmartMeter();
//					System.out.println("Smart meter fermato!");
//
//					//casa.esciDalCondominio();
//					System.out.println("Casa fuori dal condominio!");
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	//region Opzioni command line
	private static Options createOptionsInteractiveProgram()
	{
		final var info = Option.builder("i")
				.desc("Info")
				.hasArg(false)
				.build();

		final var quit = Option.builder("q")
				.desc("Quit")
				.hasArg(false)
				.build();

		final var get = Option.builder("g")
				.desc("Get")
				.argName("Key")
				.build();

		final var put = Option.builder("p")
				.desc("Put")
				.numberOfArgs(2)
				.argName("Key")
				.argName("Value")
				.build();

		final var remove = Option.builder("r")
				.desc("Remove")
				.argName("Key")
				.build();

		final var options = new Options()
				.addOption(info)
				.addOption(quit)
				.addOption(get)
				.addOption(put)
				.addOption(remove);

		return options;
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