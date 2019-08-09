/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.Chord.Chord;
import OhmOMatic.Global.Pair;
import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.Home.casa;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.Simulation.SmartMeterSimulator;
import OhmOMatic.Sistema.Base.BufferImplWithOverlap;
import OhmOMatic.Sistema.Base.MeanListener;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.util.Date;

public class Casa implements MeanListener, AutoCloseable
{

	private final SmartMeterSimulator smartMeterSimulator;
	private final BufferImplWithOverlap theBuffer;

	private Client client;
	private WebTarget webTargetRest;

	private final String RESTAddress;

	private final String myAddress;
	private final int myPort;

	private final Chord chord;

	private Double consumoEnergeticoMio;


	public Casa(String indirizzoREST_, String mioIndirizzo_, int miaPorta_, Chord chord_)
	{
		chord = chord_;

		RESTAddress = indirizzoREST_;

		myAddress = mioIndirizzo_;
		myPort = miaPorta_;

		theBuffer = new BufferImplWithOverlap(24, 12, this);

		smartMeterSimulator = new SmartMeterSimulator(theBuffer);
		smartMeterSimulator.setName("smartMeterSimulator");
	}


	@Override
	public void close()
	{
		//
	}

	//region Chiamate WS
	private WebTarget getWebTarget()
	{
		if (webTargetRest == null)
		{
			client = ClientBuilder.newClient();
			webTargetRest = client.target(RESTAddress + "/OOM");
		}

		return webTargetRest;
	}

	public void iscriviCasa()
	{
		try
		{
			var webTarget = getWebTarget();

			var wt = webTarget
					.path("iscriviCasa");

			final var par = casa.newBuilder()
					.setIP(myAddress)
					.setPort(myPort)
					.build();

			final var resListaCase = wt
					.request()
					.put(Entity.entity(par, "application/x-protobuf"), listaCase.class);

			final var res = resListaCase.getStandardResponse();

			if (res.getOk())
				System.out.println("OK!");
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void disiscriviCasa()
	{
		try
		{
			var webTarget = getWebTarget();

			var wt = webTarget
					.path("disiscriviCasa");

			final var par = casa.newBuilder()
					.setIP(myAddress)
					.setPort(myPort)
					.build();

			final var res = wt
					.request()
					.put(Entity.entity(par, "application/x-protobuf"), standardRes.class);

			if (res.getOk())
				System.out.println("OK!");
			else
				System.out.println(res.getErrore());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	//endregion

	//region Funzioni sul calcolo del consumo energetico
	private void inviaStatistiche(final Pair<Double, Date> mean)
	{
		chord.put(chord.getID(), mean);
		consumoEnergeticoMio = mean.getKey();
	}

	private Double calcolaConsumoEnergeticoComplessivo()
	{
		Double consumoEnergeticoTotale = 0d;

		final var peerList = chord.getPeerList();

		if (peerList != null)
			for (final var peer : peerList)
			{
				final var valore = chord.get(peer);

				if (valore instanceof Pair)
				{
					var p = (Pair<Double, Date>) valore;

					consumoEnergeticoTotale += p.getKey();
				}
			}

		return consumoEnergeticoTotale;
	}

	public void richiediAlCondominioDiPoterConsumareOltreLaMedia()
	{

	}

	public void stampaConsumo()
	{
		System.out.println(
				"Consumo mio: " +
						System.lineSeparator() +
						consumoEnergeticoMio +
						System.lineSeparator() +
						"Consumo energetico del condominio: " +
						System.lineSeparator() +
						calcolaConsumoEnergeticoComplessivo()
		);
	}
	//endregion

	//region Gestione Smart meter
	public void avviaSmartMeter()
	{
		smartMeterSimulator.start();
	}

	public void fermaSmartMeter()
	{
		smartMeterSimulator.stopMeGently();
	}

	@Override
	public void meanGenerated(Pair<Double, Date> mean)
	{
		inviaStatistiche(mean);
	}
	//endregion


}