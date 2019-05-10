/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Base;

import org.apache.commons.cli.*;
import org.apache.http.client.utils.URIBuilder;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class BaseCommandLineApplication
{

    protected static CommandLine getCommandLine(Options options, String[] args) throws ParseException
    {
        var parser = new DefaultParser();

        return parser.parse(options, args);
    }

    protected static URI StringToURI(String url, String port) throws URISyntaxException
    {
        final var port_ = Integer.parseUnsignedInt(port);

        return new URIBuilder()
                .setScheme("http")
                .setHost(url)
                .setPort(port_)
                .build();
    }

    protected static void StampaOpzioni(String app, Options options)
    {
        var formatter = new HelpFormatter();

        try (final var writer = new PrintWriter(System.out))
        {
            formatter.printUsage(writer, 80, app, options);
            writer.flush();
        }
    }


}