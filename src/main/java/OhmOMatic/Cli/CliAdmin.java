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

				var admin = new Admin(stringToURI(server_url, server_port, "OOM"));


				if (cmd.hasOption("e")) //Elenco case
				{
					admin.elencoCase();
				}


				if (cmd.hasOption("s")) //Ultime N statistiche casa
				{
					final var ops = cmd.getOptionValues("s");
					final var id = ops[0];
					final var n = stringToInt(ops[1], 1);

					admin.ultimeStatisticheCasa(id, n);
				}

				if (cmd.hasOption("g")) //Ultime N statistiche condominio
				{
					final var n_s = cmd.getOptionValue("g");
					final var n = stringToInt(n_s, 1);

					admin.ultimeStatisticheCondominio(n);
				}

				if (cmd.hasOption("y")) //Deviazione standard e media delle ultime N statistiche prodotte da una specifica casa
				{
					final var ops = cmd.getOptionValues("s");
					final var id = ops[0];
					final var n = stringToInt(ops[1], 1);

					admin.deviazioneStandardMediaCasa(id, n);
				}

				if (cmd.hasOption("x")) //Deviazione standard e media delle ultime N statistiche complessive condominiali
				{
					final var n_s = cmd.getOptionValue("g");
					final var n = stringToInt(n_s, 1);

					admin.deviazioneStandardMediaCondominio(n);
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


		final var elencoCase = Option.builder("e")
				.desc("Elenco case")
				.build();


		final var ultimeStatisticheCasa = Option.builder("s")
				.desc("Ultime N statistiche casa")
				.hasArgs()
				.argName("i")
				.argName("N")
				.build();

		final var ultimeStatisticheCondominio = Option.builder("g")
				.desc("Ultime N statistiche condominio")
				.hasArg()
				.argName("N")
				.build();


		final var deviazioneStandardMediaCasa = Option.builder("y")
				.desc("Deviazione standard e media delle ultime N statistiche prodotte da una specifica casa")
				.hasArgs()
				.argName("i")
				.argName("N")
				.build();

		final var deviazioneStandardMediaCondominio = Option.builder("x")
				.desc("Deviazione standard e media delle ultime N statistiche complessive condominiali")
				.hasArg()
				.argName("N")
				.build();


		final var options = new Options()
				.addOption(url)
				.addOption(port)
				.addOption(elencoCase)
				.addOption(ultimeStatisticheCasa)
				.addOption(ultimeStatisticheCondominio)
				.addOption(deviazioneStandardMediaCasa)
				.addOption(deviazioneStandardMediaCondominio);

		return options;
	}
	//endregion


}