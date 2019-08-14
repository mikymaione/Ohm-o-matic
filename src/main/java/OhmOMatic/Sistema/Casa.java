/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.Chord.Chord;
import OhmOMatic.Global.Pair;
import OhmOMatic.Global.Waiter;
import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.Home.casa;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.Simulation.SmartMeterSimulator;
import OhmOMatic.Sistema.Base.BufferImplWithOverlap;
import OhmOMatic.Sistema.Base.MeanListener;
import OhmOMatic.Sistema.Grafico.Grafico;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.util.Date;

public class Casa implements MeanListener, AutoCloseable
{

	private final Waiter calcoloStatistiche;
	private final SmartMeterSimulator smartMeterSimulator;

	private final WebTarget webTargetRest;

	private final String identificatore;
	private final String RESTAddress;
	private final String myAddress;
	private final int myPort;

	private final Grafico grafico;
	private final Chord chord;


	public Casa(final String identificatore_, final String indirizzoREST_, final String mioIndirizzo_, final int miaPorta_, final Chord chord_)
	{
		chord = chord_;

		identificatore = identificatore_;
		RESTAddress = indirizzoREST_;

		myAddress = mioIndirizzo_;
		myPort = miaPorta_;

		grafico = new Grafico(identificatore, chord);

		smartMeterSimulator = new SmartMeterSimulator(
				new BufferImplWithOverlap(24, 12, this)
		);
		smartMeterSimulator.setName("__smartMeterSimulator");

		calcoloStatistiche = new Waiter("calcoloStatistiche", grafico::calcolaStatistiche, 2000);

		webTargetRest = ClientBuilder.newClient().target(RESTAddress + "/OOM");
	}


	@Override
	public void close()
	{
		grafico.close();
	}

	//region Chiamate WS
	public void iscriviCasa()
	{
		try
		{
			var wt = webTargetRest.path("iscriviCasa");

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
			var wt = webTargetRest.path("disiscriviCasa");

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


	//region Gestione Smart meter
	public void avviaSmartMeter()
	{
		calcoloStatistiche.start();
		smartMeterSimulator.start();
	}

	public void fermaSmartMeter()
	{
		calcoloStatistiche.stopMeGently();
		smartMeterSimulator.stopMeGently();
	}

	public void boost()
	{
		System.out.println("Richiesta boost...");

		chord.invokeMutualExclusion(() ->
		{
			System.out.println("Boost!");

			try
			{
				smartMeterSimulator.boost();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			System.out.println("Fine boost.");
		});
	}

	@Override
	public void meanGenerated(Pair<Double, Date> mean)
	{
		chord.putIncremental(mean);
	}
	//endregion


}