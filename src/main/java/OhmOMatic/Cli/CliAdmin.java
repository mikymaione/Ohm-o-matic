/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Sistema.Admin;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.net.URISyntaxException;
import java.util.Scanner;

public final class CliAdmin extends BaseCommandLineApplication
{

	public static void main(String[] args)
	{
		final var options = createOptions();
		printOptions("CliAdmin", options);

		try
		{
			final var cmd = getCommandLine(options, args);

			try
			{
				final var server_url = cmd.getOptionValue("u");
				final var server_port = cmd.getOptionValue("p");

				try (final var admin = new Admin(stringToURI(server_url, server_port, "OOM")))
				{
					try (final var scanner = new Scanner(System.in))
					{
						LeggiComandiInterattivi(admin, scanner);
					}
				}
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	//region Opzioni command line
	private static void LeggiComandiInterattivi(Admin admin, Scanner scanner)
	{
		final var commands = createOptionsInteractiveProgram();

		var inEsecuzione = true;

		do
		{
			printOptions(" ", commands);

			final var line = scanner.nextLine();

			try
			{
				final var cmd = getCommandLine(commands, line.split(" "));

				if (cmd.hasOption("q"))
					inEsecuzione = false;
				else if (cmd.hasOption("e")) //Elenco case
					admin.elencoCase();
				else if (cmd.hasOption("s")) //Ultime N statistiche casa
				{
					final var ops = cmd.getOptionValues("s");
					final var id = ops[0];
					final var n = stringToInt(ops[1], 1);

					admin.ultimeStatisticheCasa(id, n);
				}
				else if (cmd.hasOption("g")) //Ultime N statistiche condominio
				{
					final var n = stringToInt(cmd.getOptionValue("g"), 1);
					admin.ultimeStatisticheCondominio(n);
				}
				else if (cmd.hasOption("y")) //Deviazione standard e media delle ultime N statistiche prodotte da una specifica casa
				{
					final var ops = cmd.getOptionValues("s");
					final var id = ops[0];
					final var n = stringToInt(ops[1], 1);

					admin.deviazioneStandardMediaCasa(id, n);
				}
				else if (cmd.hasOption("x")) //Deviazione standard e media delle ultime N statistiche complessive condominiali
				{
					final var n = stringToInt(cmd.getOptionValue("x"), 1);
					admin.deviazioneStandardMediaCondominio(n);
				}
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

		final var elencoCase = Option.builder("e")
				.desc("Elenco case")
				.build();

		final var ultimeStatisticheCasa = Option.builder("s")
				.desc("Ultime N statistiche casa")
				.numberOfArgs(2)
				.argName("i")
				.argName("N")
				.build();

		final var ultimeStatisticheCondominio = Option.builder("g")
				.desc("Ultime N statistiche condominio")
				.numberOfArgs(1)
				.argName("N")
				.build();

		final var deviazioneStandardMediaCasa = Option.builder("y")
				.desc("Deviazione standard e media delle ultime N statistiche prodotte da una specifica casa")
				.numberOfArgs(2)
				.argName("i")
				.argName("N")
				.build();

		final var deviazioneStandardMediaCondominio = Option.builder("x")
				.desc("Deviazione standard e media delle ultime N statistiche complessive condominiali")
				.numberOfArgs(1)
				.argName("N")
				.build();

		return new Options()
				.addOption(quit)
				.addOption(elencoCase)
				.addOption(ultimeStatisticheCasa)
				.addOption(ultimeStatisticheCondominio)
				.addOption(deviazioneStandardMediaCasa)
				.addOption(deviazioneStandardMediaCondominio);
	}

	private static Options createOptions()
	{
		final var url = Option.builder("u")
				.desc("Server URL")
				.required()
				.hasArg()
				.argName("URL")
				.build();

		final var port = Option.builder("p")
				.desc("Server port")
				.required()
				.hasArg()
				.argName("PORT")
				.build();

		return new Options()
				.addOption(url)
				.addOption(port);
	}
	//endregion


}