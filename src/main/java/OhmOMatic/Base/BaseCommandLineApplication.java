/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Base;

import org.apache.commons.cli.*;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

public class BaseCommandLineApplication
{

	public static void LeggiComandiInterattivi(Scanner scanner, Supplier<Options> createOptionsInteractiveProgram, Function<CommandLine, Boolean> execute)
	{
		final var commands = createOptionsInteractiveProgram.get();

		var inEsecuzione = true;

		do
		{
			printOptions(commands);

			final var line = scanner.nextLine();

			try
			{
				final var cmd = getCommandLine(commands, line.split(" "));
				inEsecuzione = execute.apply(cmd);
			}
			catch (ParseException e)
			{
				System.out.println("Il comando " + line + " non esiste!");
			}
		}
		while (inEsecuzione);
	}

	protected static CommandLine getCommandLine(Options options, String[] args) throws ParseException
	{
		var parser = new DefaultParser();

		return parser.parse(options, args);
	}

	private final static HelpFormatter formatter = new HelpFormatter();

	protected static void printOptions(final Options options)
	{
		printOptions(" ", options);
	}

	protected static void printOptions(final String app, final Options options)
	{
		formatter.printHelp(app, options);
	}


}