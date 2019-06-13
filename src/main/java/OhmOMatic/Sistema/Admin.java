/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.ProtoBuffer.Home.listaCase;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

public final class Admin
{

	private Client client;
	private WebTarget webTarget;


	public Admin(URI indirizzo)
	{
		client = ClientBuilder.newClient();
		webTarget = client.target(indirizzo + "/OOM");
	}


	//region Funzioni case
	public void elencoCase()
	{
		try
		{
			var wt = webTarget
					.path("elencoCase");

			final var lista = wt
					.request()
					.get(listaCase.class);

			final var res = lista.getStandardResponse();

			if (res.getOk())
			{
				System.out.println("OK!");
				System.out.println("Lista case presenti nel sistema:");

				final var elenco = lista.getCaseList();

				for (var c : elenco)
					System.out.println(String.format("-Casa %s (%s:%d)", c.getID(), c.getIP(), c.getPort()));
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	//endregion

	//region Statistiche
	public void ultimeStatisticheCasa(String id, int n)
	{

	}

	public void ultimeStatisticheCondominio(int n)
	{

	}

	public void deviazioneStandardMediaCasa(String id, int n)
	{

	}

	public void deviazioneStandardMediaCondominio(int n)
	{

	}
	//endregion


}