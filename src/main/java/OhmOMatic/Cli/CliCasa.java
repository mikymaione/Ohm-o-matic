/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Base.BaseCommandLineApplication;
import OhmOMatic.Sistema.Casa;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class CliCasa extends BaseCommandLineApplication
{

    public static void main(String[] args)
    {
        final var options = createOptions();
        printOptions("CliCasa", options);

        try
        {
            final var cmd = getCommandLine(options, args);

            final var id = cmd.getOptionValue("i");
            final var rest_url = cmd.getOptionValue("r");
            final var server_url = cmd.getOptionValue("s");
            final var server_port_s = cmd.getOptionValue("p");
            final var server_port = Integer.parseUnsignedInt(server_port_s);

            System.out.println("Casa avviata!");

            var casa = new Casa(rest_url, server_url, server_port, id);

            casa.iscrivitiAlCondominio();
            casa.avviaSmartMeter();
            //casa.fermaSmartMeter();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    //region Opzioni command line
    private static Options createOptions()
    {
        final var url_rest = Option.builder("r")
                .desc("REST URL")
                .required()
                .hasArg()
                .argName("URL")
                .build();

        final var url = Option.builder("s")
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

        final var id = Option.builder("i")
                .desc("ID")
                .required()
                .hasArg()
                .argName("ID")
                .build();


        final var options = new Options()
                .addOption(url_rest)
                .addOption(url)
                .addOption(port)
                .addOption(id);

        return options;
    }
    //endregion


}