/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST;

import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.Home.casa;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.ProtoBuffer.Stat.parametriStatisticaReq;
import OhmOMatic.ProtoBuffer.Stat.parametriStatisticheReq;
import OhmOMatic.REST.Backend.Backend;

import javax.ws.rs.*;
import java.util.LinkedList;


@Path("OOM")
public final class OOM extends Backend
{

	//region Casa
	@PUT
	@Path("/iscriviCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public listaCase iscriviCasa(casa par)
	{
		synchronized (elencoCase)
		{
			elencoCase.add(par);

			return listaCase
					.newBuilder()
					.setStandardResponse(buildStandardRes())
					.addAllCase(elencoCase)
					.build();
		}
	}

	@PUT
	@Path("/disiscriviCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes disiscriviCasa(casa par)
	{
		synchronized (elencoCase)
		{
			if (elencoCase.remove(par))
				return buildStandardRes();
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
			return listaCase
					.newBuilder()
					.setStandardResponse(buildStandardRes())
					.addAllCase(elencoCase)
					.build();
		}
	}
	//endregion


	//region Statistiche
	@PUT
	@Path("/aggiungiStatisticaLocale")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes aggiungiStatisticaLocale(parametriStatisticaReq par)
	{
		synchronized (statisticheCasa)
		{
			final var lista = statisticheCasa.getOrDefault(par.getHomeData(), new LinkedList<>());

			lista.add(par.getParamStats());

			if (!statisticheCasa.containsKey(par.getHomeData()))
				statisticheCasa.put(par.getHomeData(), lista);

			return buildStandardRes();
		}
	}

	@PUT
	@Path("/aggiungiStatisticaGlobale")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes aggiungiStatisticaGlobale(parametriStatisticaReq par)
	{
		synchronized (statisticheCondominio)
		{
			statisticheCondominio.add(par.getParamStats());

			return buildStandardRes();
		}
	}


	@POST
	@Path("/ultimeStatisticheCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes ultimeStatisticheCasa(parametriStatisticaReq par)
	{

		return buildStandardRes();
	}

	@POST
	@Path("/ultimeStatisticheCondominio")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes ultimeStatisticheCondominio(parametriStatisticheReq par)
	{
		final var res = buildStandardRes();

		return res;
	}


	@POST
	@Path("/deviazioneStandardMediaCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes deviazioneStandardMediaCasa(parametriStatisticaReq par)
	{
		final var res = buildStandardRes();

		return res;
	}

	@POST
	@Path("/deviazioneStandardMediaCondominio")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes deviazioneStandardMediaCondominio(parametriStatisticheReq par)
	{
		final var res = buildStandardRes();

		return res;
	}
	//endregion
}