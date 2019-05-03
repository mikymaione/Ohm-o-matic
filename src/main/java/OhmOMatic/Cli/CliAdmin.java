/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import org.apache.commons.cli.*;

public final class CliAdmin
{
    public static void main(String[] args)
    {
        final Options options = new Options();
        options
                .addOption("a", "add", false, "Iscrivi casa")
                .addOption("r", "remove", false, "Disiscrivi casa");

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Ohm-o-matic - CliAdmin", options);

        CommandLineParser parser = new DefaultParser();

        try
        {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("a"))
            {

            }
            else if (cmd.hasOption("r"))
            {

            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

}