/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.ProtoBuffer.Stat;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Date;

public final class Admin implements AutoCloseable
{

	private Client client;
	private WebTarget webTarget;


	public Admin(URI indirizzo)
	{
		client = ClientBuilder.newClient();
		webTarget = client.target(indirizzo + "/OOM");
	}

	@Override
	public void close()
	{
		client.close();
	}

	//region Funzioni comuni
	private WebTarget getWebTarget(final String path)
	{
		return webTarget.path(path);
	}
	//endregion

	//region Funzioni case
	public void elencoCase()
	{
		try
		{
			final var wt = getWebTarget("elencoCase");

			final var lista = wt
					.request()
					.get(listaCase.class);

			final var res = lista.getStandardResponse();

			if (res.getOk())
			{
				System.out.println("Lista case presenti nel sistema:");

				final var elenco = lista.getCaseList();

				for (var c : elenco)
					System.out.println(String.format("-Casa %s (%s:%d)", c.getIdentificatore(), c.getIP(), c.getPort()));
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
		try
		{
			final var wt = getWebTarget("ultimeStatisticheCasa");

			final var params = Stat.getStatisticheCasa
					.newBuilder()
					.setN(n)
					.setCasa(
							Home.casa
									.newBuilder()
									.setIdentificatore(id)
					)
					.build();

			final var stats = wt
					.request()
					.post(Entity.entity(params, "application/x-protobuf"), Stat.statisticheRes.class);

			final var res = stats.getStandardRes();

			if (res.getOk())
			{
				System.out.println("Ultime " + n + " statistiche casa " + id + ":");

				for (var s : stats.getStatisticheList())
					System.out.println(new Date(s.getData()) + ": " + s.getValore());
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void ultimeStatisticheCondominio(int n)
	{
		try
		{
			final var wt = getWebTarget("ultimeStatisticheCondominio");

			final var params = Stat.getStatisticheCondominio
					.newBuilder()
					.setN(n)
					.build();

			final var stats = wt
					.request()
					.post(Entity.entity(params, "application/x-protobuf"), Stat.statisticheRes.class);

			final var res = stats.getStandardRes();

			if (res.getOk())
			{
				System.out.println("Ultime " + n + " statistiche condominio:");

				for (var s : stats.getStatisticheList())
					System.out.println(new Date(s.getData()) + ": " + s.getValore());
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void deviazioneStandardMediaCasa(String id, int n)
	{
		try
		{
			final var wt = getWebTarget("deviazioneStandardMediaCasa");

			final var params = Stat.getStatisticheCasa
					.newBuilder()
					.setN(n)
					.setCasa(
							Home.casa
									.newBuilder()
									.setIdentificatore(id)
					)
					.build();

			final var stats = wt
					.request()
					.post(Entity.entity(params, "application/x-protobuf"), Stat.deviazioneStandardMediaRes.class);

			final var res = stats.getStandardRes();

			if (res.getOk())
			{
				final var statistiche = stats.getStatistiche();

				System.out.println("Casa " + id + ":");
				System.out.println("-Media: " + statistiche.getMedia());
				System.out.println("-Deviazione standard: " + statistiche.getDeviazioneStandard());
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void deviazioneStandardMediaCondominio(int n)
	{
		try
		{
			final var wt = getWebTarget("deviazioneStandardMediaCondominio");

			final var params = Stat.getStatisticheCondominio
					.newBuilder()
					.setN(n)
					.build();

			final var stats = wt
					.request()
					.post(Entity.entity(params, "application/x-protobuf"), Stat.deviazioneStandardMediaRes.class);

			final var res = stats.getStandardRes();

			if (res.getOk())
			{
				final var statistiche = stats.getStatistiche();

				System.out.println("Condominio:");
				System.out.println("-Media: " + statistiche.getMedia());
				System.out.println("-Deviazione standard: " + statistiche.getDeviazioneStandard());
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


}