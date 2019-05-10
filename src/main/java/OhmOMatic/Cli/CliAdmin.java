/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Sistema.ServerAmministratore;
import org.apache.commons.cli.*;

import java.net.URI;
import java.net.URISyntaxException;

public final class CliAdmin
{

    public static void main(String[] args)
    {
        final Options options = CreaOpzioni();

        CommandLineParser parser = new DefaultParser();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("CliAdmin -u <URL> [OPTIONS]", options);

        try
        {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("u"))
                try
                {
                    final String server_url = cmd.getOptionValue("URL");
                    final ServerAmministratore serverAmministratore = new ServerAmministratore(new URI(server_url));

                    if (cmd.hasOption("a"))
                    {
                        final String id_casa = cmd.getOptionValue("ID");
                        serverAmministratore.iscriviCasa(Integer.parseInt(id_casa));
                    }
                    else if (cmd.hasOption("r"))
                    {
                        final String id_casa = cmd.getOptionValue("ID");
                        serverAmministratore.disiscriviCasa(Integer.parseInt(id_casa));
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

    private static Options CreaOpzioni()
    {
        Option url = Option.builder("u")
                .desc("Server URL")
                .required()
                .hasArg()
                .argName("URL")
                .build();

        Option add = Option.builder("a")
                .desc("Iscrivi casa")
                .hasArg()
                .argName("ID")
                .build();

        Option remove = Option.builder("r")
                .desc("Disiscrivi casa")
                .hasArg()
                .argName("ID")
                .build();

        Options options = new Options();

        options.addOption(url);
        options.addOption(add);
        options.addOption(remove);

        return options;
    }

}