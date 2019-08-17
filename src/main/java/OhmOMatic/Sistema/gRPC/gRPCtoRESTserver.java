/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema.gRPC;

import OhmOMatic.Global.Pair;
import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.PushNotification;
import OhmOMatic.ProtoBuffer.Stat;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.util.Date;
import java.util.LinkedList;

public class gRPCtoRESTserver implements AutoCloseable
{

	private Client client;
	private WebTarget webTargetRest;

	public gRPCtoRESTserver(final String indirizzo)
	{
		client = ClientBuilder.newClient();
		webTargetRest = client.target(indirizzo);
	}

	@Override
	public void close()
	{
		client.close();
	}


	//region Funzioni comuni
	private WebTarget getWebTarget(final String path)
	{
		return webTargetRest.path(path);
	}
	//endregion


	//region Funzioni case
	public boolean iscriviCasa(final String identificatore, final String myAddress, final int myPort)
	{
		try
		{
			final var par = Home.casa.newBuilder()
					.setIdentificatore(identificatore)
					.setIP(myAddress)
					.setPort(myPort)
					.build();

			final var wt = getWebTarget("iscriviCasa");
			final var resListaCase = wt
					.request()
					.post(Entity.entity(par, "application/x-protobuf"), Home.listaCase.class);

			final var res = resListaCase.getStandardResponse();

			if (res.getOk())
				return true;
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean disiscriviCasa(final String identificatore, final String myAddress, final int myPort)
	{
		try
		{
			final var par = Home.casa.newBuilder()
					.setIdentificatore(identificatore)
					.setIP(myAddress)
					.setPort(myPort)
					.build();

			final var wt = getWebTarget("disiscriviCasa");
			final var res = wt
					.request()
					.post(Entity.entity(par, "application/x-protobuf"), Common.standardRes.class);

			if (res.getOk())
				return true;
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean elencoCase()
	{
		try
		{
			final var wt = getWebTarget("elencoCase");
			final var lista = wt
					.request()
					.get(Home.listaCase.class);

			final var res = lista.getStandardResponse();

			if (res.getOk())
			{
				System.out.println("Lista case presenti nel sistema:");

				final var elenco = lista.getCaseList();

				for (var c : elenco)
					System.out.println(String.format("-Casa %s (%s:%d)", c.getIdentificatore(), c.getIP(), c.getPort()));

				return true;
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	//endregion


	//region Statistiche
	protected boolean aggiungiStatisticaLocale(final String id, final Date data, final double valore)
	{
		try
		{
			final var params = Stat.statisticheCasa
					.newBuilder()
					.setStatistiche(Stat.statistica
							.newBuilder()
							.setData(data.getTime())
							.setValore(valore)
					)
					.setCasa(
							Home.casa
									.newBuilder()
									.setIdentificatore(id)
					)
					.build();

			final var wt = getWebTarget("aggiungiStatisticaLocale");
			final var res = wt
					.request()
					.post(Entity.entity(params, "application/x-protobuf"), Common.standardRes.class);

			if (res.getOk())
				return true;
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean aggiungiStatisticaGlobale(LinkedList<Pair<Date, Double>> daInviareCondominio)
	{
		try
		{
			var paramsB = Stat.statisticheCondominio
					.newBuilder();

			for (var s : daInviareCondominio)
				paramsB.addStatistiche(Stat.statistica
						.newBuilder()
						.setData(s.getKey().getTime())
						.setValore(s.getValue())
				);

			final var wt = getWebTarget("aggiungiStatisticaGlobale");
			final var res = wt
					.request()
					.post(Entity.entity(paramsB.build(), "application/x-protobuf"), Common.standardRes.class);

			if (res.getOk())
				return true;
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean ultimeStatisticheCasa(String id, int n)
	{
		try
		{
			final var params = Stat.getStatisticheCasa
					.newBuilder()
					.setN(n)
					.setCasa(
							Home.casa
									.newBuilder()
									.setIdentificatore(id)
					)
					.build();

			final var wt = getWebTarget("ultimeStatisticheCasa");
			final var stats = wt
					.request()
					.post(Entity.entity(params, "application/x-protobuf"), Stat.statisticheRes.class);

			final var res = stats.getStandardRes();

			if (res.getOk())
			{
				System.out.println("Ultime " + n + " statistiche casa " + id + ":");

				for (var s : stats.getStatisticheList())
					System.out.println(new Date(s.getData()) + ": " + s.getValore());

				return true;
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean ultimeStatisticheCondominio(int n)
	{
		try
		{
			final var params = Stat.getStatisticheCondominio
					.newBuilder()
					.setN(n)
					.build();

			final var wt = getWebTarget("ultimeStatisticheCondominio");
			final var stats = wt
					.request()
					.post(Entity.entity(params, "application/x-protobuf"), Stat.statisticheRes.class);

			final var res = stats.getStandardRes();

			if (res.getOk())
			{
				System.out.println("Ultime " + n + " statistiche condominio:");

				for (var s : stats.getStatisticheList())
					System.out.println(new Date(s.getData()) + ": " + s.getValore());

				return true;
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean deviazioneStandardMediaCasa(String id, int n)
	{
		try
		{
			final var params = Stat.getStatisticheCasa
					.newBuilder()
					.setN(n)
					.setCasa(
							Home.casa
									.newBuilder()
									.setIdentificatore(id)
					)
					.build();

			final var wt = getWebTarget("deviazioneStandardMediaCasa");
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

				return true;
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean deviazioneStandardMediaCondominio(int n)
	{
		try
		{
			final var params = Stat.getStatisticheCondominio
					.newBuilder()
					.setN(n)
					.build();

			final var wt = getWebTarget("deviazioneStandardMediaCondominio");
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

				return true;
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean boostRichiesto(final String identificatore, final String myAddress, final int myPort)
	{
		try
		{
			final var par = Home.casa.newBuilder()
					.setIdentificatore(identificatore)
					.setIP(myAddress)
					.setPort(myPort)
					.build();

			final var wt = getWebTarget("boostRichiesto");
			final var res = wt
					.request()
					.post(Entity.entity(par, "application/x-protobuf"), Common.standardRes.class);

			if (res.getOk())
				return true;
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	//endregion


	//region Notifiche push
	public boolean getNotifiche()
	{
		try
		{
			final var wt = getWebTarget("getNotifiche");
			final var lista = wt
					.request()
					.get(PushNotification.notificaRes.class);

			final var res = lista.getStandardRes();

			if (res.getOk())
			{
				System.out.println("Nuove notifiche:");

				final var elenco = lista.getNotificheList();

				for (var n : elenco)
					System.out.println("-" + new Date(n.getData()) + ": " + n.getMsg());

				return true;
			}
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	//endregion

}