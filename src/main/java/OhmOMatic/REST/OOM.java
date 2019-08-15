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

import javax.ws.rs.*;
import java.util.ArrayList;


@Path("OOM")
public final class OOM
{

	//region Casa
	@PUT
	@Path("/iscriviCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public listaCase iscriviCasa(casa par)
	{
		var iscrizione_casa_nel_DB_ok = true;
		var iscrizione_casa_nel_DB_errore = "excepsions!";

		//iscrizione casa nel DB
		//.........
		//.........
		//.........
		//iscrizione casa nel DB

		final var resElencoCase = listaCase
				.newBuilder()
				.setStandardResponse(buildStandardRes(iscrizione_casa_nel_DB_ok, iscrizione_casa_nel_DB_errore))
				.addAllCase(getElencoCase())
				.build();

		return resElencoCase;
	}

	@PUT
	@Path("/disiscriviCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes disiscriviCasa(casa par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}

	private ArrayList<casa> getElencoCase()
	{
		final var elenco_case = new ArrayList<casa>();

		return elenco_case;
	}

	@GET
	@Path("/elencoCase")
	@Produces("application/x-protobuf")
	public listaCase elencoCase()
	{
		final var res = listaCase
				.newBuilder()
				.setStandardResponse(buildStandardRes(false, "excepsions!"))
				.addAllCase(getElencoCase())
				.build();

		return res;
	}
	//endregion


	//region Statistiche
	@PUT
	@Path("/aggiungiStatisticaLocale")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes aggiungiStatisticaLocale(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}

	@PUT
	@Path("/aggiungiStatisticaGlobale")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes aggiungiStatisticaGlobale(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}


	@POST
	@Path("/ultimeStatisticheCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes ultimeStatisticheCasa(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}

	@POST
	@Path("/ultimeStatisticheCondominio")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes ultimeStatisticheCondominio(parametriStatisticheReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}


	@POST
	@Path("/deviazioneStandardMediaCasa")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes deviazioneStandardMediaCasa(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}

	@POST
	@Path("/deviazioneStandardMediaCondominio")
	@Consumes("application/x-protobuf")
	@Produces("application/x-protobuf")
	public standardRes deviazioneStandardMediaCondominio(parametriStatisticheReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}
	//endregion

	//region Common functions
	private standardRes buildStandardRes(final boolean Ok, final String errore)
	{
		return standardRes
				.newBuilder()
				.setOk(Ok)
				.setErrore(errore)
				.build();
	}
	//endregion
}