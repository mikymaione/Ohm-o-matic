/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST.Backend;

import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.Stat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Backend
{

	protected final HashSet<Home.casa> elencoCase = new HashSet<>();
	protected final HashMap<String, LinkedList<Stat.statistica>> statisticheCasa = new HashMap<>();
	protected final LinkedList<Stat.statistica> statisticheCondominio = new LinkedList<>();


	protected Stat.deviazioneStandardMedia buildDeviazioneStandardMedia(LinkedList<Stat.statistica> lista)
	{
		var media = 0d;
		for (final var s : lista)
			media += s.getValore();

		media /= lista.size();

		var deviazioneStandard = 0d;
		for (final var s : lista)
			deviazioneStandard += Math.pow(s.getValore() - media, 2);

		deviazioneStandard = Math.sqrt(deviazioneStandard / lista.size());

		return Stat.deviazioneStandardMedia
				.newBuilder()
				.setDeviazioneStandard(deviazioneStandard)
				.setMedia(media)
				.build();
	}

	protected Stat.statisticheRes buildStatisticheRes(Iterable<Stat.statistica> statistiche, final int N)
	{
		final var listaN = new LinkedList<Stat.statistica>();

		var x = 0;
		for (var s : statistiche)
		{
			listaN.add(s);
			x++;

			if (x > N)
				break;
		}

		return buildStatisticheRes(listaN);
	}

	protected Stat.statisticheRes buildStatisticheRes(Iterable<Stat.statistica> statistiche)
	{
		return Stat.statisticheRes
				.newBuilder()
				.setStandardRes(buildStandardRes())
				.addAllStatistiche(statistiche)
				.build();
	}

	protected Home.listaCase buildListaCase()
	{
		return Home.listaCase
				.newBuilder()
				.setStandardResponse(buildStandardRes())
				.addAllCase(elencoCase)
				.build();
	}

	protected Common.standardRes buildStandardRes()
	{
		return Common.standardRes
				.newBuilder()
				.setOk(true)
				.build();
	}

	protected Common.standardRes buildStandardRes(final String errore)
	{
		return Common.standardRes
				.newBuilder()
				.setOk(false)
				.setErrore(errore)
				.build();
	}


}