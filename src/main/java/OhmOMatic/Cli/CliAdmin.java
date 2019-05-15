/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Sistema.ServerAmministratore;
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

                var serverAmministratore = new ServerAmministratore(stringToURI(server_url, server_port, "OOM"));

                if (cmd.hasOption("a"))
                {
                    final var id_casa_s = cmd.getOptionValue("a");
                    final var id_casa = Integer.parseUnsignedInt(id_casa_s);

                    serverAmministratore.iscriviCasa(id_casa);
                }

                if (cmd.hasOption("r"))
                {
                    final var id_casa_s = cmd.getOptionValue("r");
                    final var id_casa = Integer.parseUnsignedInt(id_casa_s);

                    serverAmministratore.disiscriviCasa(id_casa);
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

        final var add = Option.builder("a")
                .desc("Iscrivi casa")
                .hasArg()
                .argName("ID")
                .build();

        final var remove = Option.builder("r")
                .desc("Disiscrivi casa")
                .hasArg()
                .argName("ID")
                .build();

        final var options = new Options()
                .addOption(url)
                .addOption(port)
                .addOption(add)
                .addOption(remove);

        return options;
    }


}