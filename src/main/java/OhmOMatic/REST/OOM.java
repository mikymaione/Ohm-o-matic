/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST;

import OhmOMatic.Global.GB;
import OhmOMatic.Global.Pair;
import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.Home.casa;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.ProtoBuffer.PushNotification;
import OhmOMatic.ProtoBuffer.Stat;
import OhmOMatic.REST.Backend.Backend;

import javax.ws.rs.*;
import java.util.Date;


@Path("OOM")
public final class OOM extends Backend
{

	//region Notiche push
	@GET
	@Path("/getNotifiche")
	@Produces("application/x-protobuf")
	public PushNotification.notificaRes getNotifiche()
	{
		var R = PushNotification.notificaRes
				.newBuilder()
				.setStandardRes(buildStandardRes());

		GB.waitfor(() ->
		{
			synchronized (notifiche)
			{
				return notifiche.size() > 0;
			}
		}, 500);

		synchronized (notifiche)
		{
			for (var n : notifiche)
				R.addNotifiche(PushNotification.notifica.newBuilder()
						.setData(n.getKey().getTime())
						.setMsg(n.getValue())
				);

			notifiche.clear();

			return R.build();
		}
	}
	//endregion


	//region Casa
	
	@POST
	@Path("/iscriviCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public listaCase iscriviCasa(casa par)
	{
		synchronized (elencoCase)
		{
			elencoCase.add(par);

			synchronized (notifiche)
			{
				notifiche.add(new Pair<>(
					new Date(), 
					"Casa " + par.getIdentificatore() + " si è aggiunta!")
				);
			}

			return buildListaCase();
		}
	}

	@POST
	@Path("/disiscriviCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes disiscriviCasa(casa par)
	{
		synchronized (elencoCase)
		{
			if (elencoCase.remove(par))
			{
				synchronized (notifiche)
				{
					notifiche.add(new Pair<>(new Date(), "Casa " + par.getIdentificatore() + " è uscita!"));
				}

				return buildStandardRes();
			}
			else
				return buildStandardRes("Non sono riuscito a rimuovere la casa " + par.getIdentificatore());
		}
	}

	@GET
	@Path("/elencoCase")
	@Produces("application/x-protobuf")
	public listaCase elencoCase()
	{
		synchronized (elencoCase)
		{
			return buildListaCase();
		}
	}
	//endregion


	//region Statistiche
	@POST
	@Path("/aggiungiStatisticaLocale")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes aggiungiStatisticaLocale(Stat.statisticheCasa par)
	{
		synchronized (statisticheCasa)
		{
			final var casa = par.getCasa();
			final var id = casa.getIdentificatore();
			final var lista = getStatisticaCasa(casa);

			lista.add(par.getStatistiche());

			if (!statisticheCasa.containsKey(id))
				statisticheCasa.put(id, lista);

			return buildStandardRes();
		}
	}

	@POST
	@Path("/aggiungiStatisticaGlobale")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes aggiungiStatisticaGlobale(Stat.statisticheCondominio par)
	{
		synchronized (statisticheCondominio)
		{
			statisticheCondominio.addAll(par.getStatisticheList());

			return buildStandardRes();
		}
	}

	@POST
	@Path("/ultimeStatisticheCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public Stat.statisticheRes ultimeStatisticheCasa(Stat.getStatisticheCasa par)
	{
		synchronized (statisticheCasa)
		{
			return buildStatisticheRes(getStatisticaCasa(par.getCasa()), par.getN());
		}
	}

	@POST
	@Path("/ultimeStatisticheCondominio")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public Stat.statisticheRes ultimeStatisticheCondominio(Stat.getStatisticheCondominio par)
	{
		synchronized (statisticheCondominio)
		{
			return buildStatisticheRes(statisticheCondominio);
		}
	}

	@POST
	@Path("/deviazioneStandardMediaCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public Stat.deviazioneStandardMediaRes deviazioneStandardMediaCasa(Stat.getStatisticheCasa par)
	{
		synchronized (statisticheCasa)
		{
			return buildDeviazioneStandardMedia(getStatisticaCasa(par.getCasa()));
		}
	}

	@POST
	@Path("/deviazioneStandardMediaCondominio")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public Stat.deviazioneStandardMediaRes deviazioneStandardMediaCondominio(Stat.getStatisticheCondominio par)
	{
		synchronized (statisticheCondominio)
		{
			return buildDeviazioneStandardMedia(statisticheCondominio);
		}
	}


	@POST
	@Path("/boostRichiesto")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes boostRichiesto(casa par)
	{
		synchronized (notifiche)
		{
			notifiche.add(new Pair<>(new Date(), "Casa " + par.getIdentificatore() + " ha usato Boost... è super efficace!"));
		}

		return buildStandardRes();
	}
	//endregion


}