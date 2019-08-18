/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Global.GB;
import OhmOMatic.Sistema.Admin;
import OhmOMatic.Sistema.LongPolling;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
			final var rest_url = cmd.getOptionValue("r");

			try (
					final var admin = new Admin(rest_url);
					final var scanner = new Scanner(System.in);
					final var longPolling = new LongPolling(rest_url)
			)
			{
				longPolling.start();

				LeggiComandiInterattivi(scanner, CliAdmin::createOptionsInteractiveProgram, inpts ->
				{
					if (inpts.hasOption("q"))
						return false;
					else if (inpts.hasOption("e")) //Elenco case
						admin.elencoCase();
					else if (inpts.hasOption("s")) //Ultime N statistiche casa
					{
						final var ops = inpts.getOptionValues("s");
						final var id = ops[0];
						final var n = GB.stringToInt(ops[1], 1);

						admin.ultimeStatisticheCasa(id, n);
					}
					else if (inpts.hasOption("g")) //Ultime N statistiche condominio
					{
						final var n = GB.stringToInt(inpts.getOptionValue("g"), 1);
						admin.ultimeStatisticheCondominio(n);
					}
					else if (inpts.hasOption("y")) //Deviazione standard e media delle ultime N statistiche prodotte da una specifica casa
					{
						final var ops = inpts.getOptionValues("y");
						final var id = ops[0];
						final var n = GB.stringToInt(ops[1], 1);

						admin.deviazioneStandardMediaCasa(id, n);
					}
					else if (inpts.hasOption("x")) //Deviazione standard e media delle ultime N statistiche complessive condominiali
					{
						final var n = GB.stringToInt(inpts.getOptionValue("x"), 1);
						admin.deviazioneStandardMediaCondominio(n);
					}

					return true;
				});
			}
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		System.exit(0);
	}

	//region Opzioni command line
	private static Options createOptionsInteractiveProgram()
	{
		return new Options()
				.addOption(OP("q", "Quit"))
				.addOption(OP("e", "Elenco case"))
				.addOption(OP("s", "Ultime N statistiche casa", "ID N", 2, false))
				.addOption(OP("g", "Ultime N statistiche condominio", "N", 1, false))
				.addOption(OP("y", "Deviazione standard e media delle ultime N statistiche prodotte da una specifica casa", "ID N", 2, false))
				.addOption(OP("x", "Deviazione standard e media delle ultime N statistiche complessive condominiali", "N", 1, false));
	}

	private static Options createOptions()
	{
		return new Options()
				.addOption(OP("r", "REST URL", "URL", true));
	}
	//endregion


}