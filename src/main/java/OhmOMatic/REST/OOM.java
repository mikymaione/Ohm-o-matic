/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST;

import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.Home.casa;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.ProtoBuffer.Stat.parametriStatisticaReq;
import OhmOMatic.ProtoBuffer.Stat.parametriStatisticheReq;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.util.ArrayList;


@Path("OOM")
public final class OOM
{

	//region Casa
	@PUT
	@Path("/iscriviCasa")
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
	public standardRes aggiungiStatisticaLocale(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}

	@PUT
	@Path("/aggiungiStatisticaGlobale")
	public standardRes aggiungiStatisticaGlobale(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}


	@GET
	@Path("/ultimeStatisticheCasa")
	public standardRes ultimeStatisticheCasa(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}

	@GET
	@Path("/ultimeStatisticheCondominio")
	public standardRes ultimeStatisticheCondominio(parametriStatisticheReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}


	@GET
	@Path("/deviazioneStandardMediaCasa")
	public standardRes deviazioneStandardMediaCasa(parametriStatisticaReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}

	@GET
	@Path("/deviazioneStandardMediaCondominio")
	public standardRes deviazioneStandardMediaCondominio(parametriStatisticheReq par)
	{
		final var res = buildStandardRes(false, "excepsions!");

		return res;
	}
	//endregion

	//region Common functions
	private standardRes buildStandardRes(boolean Ok, String errore)
	{
		return standardRes
				.newBuilder()
				.setOk(Ok)
				.setErrore(errore)
				.build();
	}
	//endregion
}