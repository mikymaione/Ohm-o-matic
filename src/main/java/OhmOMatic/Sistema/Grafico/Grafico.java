/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema.Grafico;

import OhmOMatic.Chord.Chord;
import OhmOMatic.Chord.Link.NodeLink;
import OhmOMatic.Global.Pair;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Grafico implements AutoCloseable
{

	private JFrame chart_frame;
	private final XYChart chart;
	private final SwingWrapper<XYChart> swing;
	private final String nome;

	private final HashMap<NodeLink, Integer> ultimoAggiornamentoGrafico = new HashMap<>();
	private final HashMap<Date, Double> condominioGrafico = new HashMap<>();
	private final HashMap<Date, Double> mioGrafico = new HashMap<>();

	private final Chord chord;


	public Grafico(String nome, Chord chord)
	{
		this.nome = nome;
		this.chord = chord;

		final var dim = Toolkit.getDefaultToolkit().getScreenSize();

		chart = new XYChartBuilder()
				.title("Consumo energetico")
				.yAxisTitle("kW⋅h")
				.xAxisTitle("s")
				.width(dim.width / 2)
				.height(dim.height / 2)
				.build();

		var stiler = chart.getStyler();
		stiler.setLegendPosition(Styler.LegendPosition.InsideSE);
		stiler.setLocale(Locale.ITALY);
		stiler.setDatePattern("HH:mm:ss");
		stiler.setYAxisMin(0d);

		swing = new SwingWrapper<>(chart);
	}

	@Override
	public void close()
	{
		if (chart_frame != null)
			chart_frame.dispose();
	}

	private Pair<ArrayList<Double>, ArrayList<Date>> convertiPerChart(ArrayList<Pair<Double, Date>> mie)
	{
		var mioConsumo = new ArrayList<Double>(mie.size());
		var mioTempo = new ArrayList<Date>(mie.size());

		for (final var e : mie)
		{
			mioConsumo.add(e.getKey());
			mioTempo.add(e.getValue());
		}

		return new Pair<>(mioConsumo, mioTempo);
	}

	private void aggiornaChart(Pair<ArrayList<Double>, ArrayList<Date>> mieiConsumi, Pair<ArrayList<Double>, ArrayList<Date>> condominioConsumi)
	{
		if (chart.getSeriesMap().isEmpty())
		{
			chart.addSeries("Mio", mieiConsumi.getValue(), mieiConsumi.getKey(), null).setMarker(SeriesMarkers.NONE);
			chart.addSeries("Condominio", condominioConsumi.getValue(), condominioConsumi.getKey(), null).setMarker(SeriesMarkers.NONE);

			chart_frame = swing.displayChart();
			chart_frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			chart_frame.setTitle(nome);
		}
		else
		{
			chart.updateXYSeries("Mio", mieiConsumi.getValue(), mieiConsumi.getKey(), null);
			chart.updateXYSeries("Condominio", condominioConsumi.getValue(), condominioConsumi.getKey(), null);

			swing.repaintChart();
		}
	}

	private void aggiornaChart(HashMap<Date, Double> mioGrafico, HashMap<Date, Double> condominioGrafico)
	{
		var mieSorted = new ArrayList<Pair<Double, Date>>(mioGrafico.size());
		var condominiali = new ArrayList<Pair<Double, Date>>(condominioGrafico.size());

		for (final var s : condominioGrafico.entrySet())
			condominiali.add(new Pair<>(s.getValue(), s.getKey()));

		for (final var m : mioGrafico.entrySet())
			mieSorted.add(new Pair<>(m.getValue(), m.getKey()));

		condominiali.sort(Comparator.comparing(Pair::getValue));
		mieSorted.sort(Comparator.comparing(Pair::getValue));

		aggiornaChart(convertiPerChart(mieSorted), convertiPerChart(condominiali));
	}


	public synchronized void calcolaStatistiche()
	{
		final var peerList = chord.getPeerList();

		if (peerList != null)
		{
			for (final var peer : peerList)
			{
				final var lastNumero = ultimoAggiornamentoGrafico.getOrDefault(peer, 0);
				final var curNumero = chord.getOrDefault(peer.ID, 0);
				ultimoAggiornamentoGrafico.put(peer, curNumero);

				final var statisticheaAltroPeer = chord.getIncrementals(peer.ID, lastNumero, curNumero);

				for (var statisticaAltroPeer : statisticheaAltroPeer)
					if (statisticaAltroPeer != null)
					{
						final var p = Pair.<Double, Date>fromSerializable(statisticaAltroPeer);
						final var attuale = condominioGrafico.getOrDefault(p.getValue(), 0d) + p.getKey();

						condominioGrafico.put(p.getValue(), attuale);

						if (peer.ID.equals(chord.getID()))
						{
							final var attuale_m = mioGrafico.getOrDefault(p.getValue(), 0d) + p.getKey();
							mioGrafico.put(p.getValue(), attuale_m);
						}
					}
			}

			if (mioGrafico.size() > 0 && condominioGrafico.size() > 0)
				aggiornaChart(mioGrafico, condominioGrafico);
		}
	}


}