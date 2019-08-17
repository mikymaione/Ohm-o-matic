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
import OhmOMatic.Simulation.SmartMeterSimulator;
import OhmOMatic.Sistema.Base.BufferImplWithOverlap;
import OhmOMatic.Sistema.Base.MeanListener;
import OhmOMatic.Sistema.Grafico.Grafico;
import OhmOMatic.Sistema.gRPC.gRPCtoRESTserver;

import java.net.URISyntaxException;
import java.util.Date;

public class Casa extends gRPCtoRESTserver implements MeanListener, AutoCloseable
{

	private final Thread invioStatisticheCondominiali;
	private final Waiter calcoloStatistiche;
	private final SmartMeterSimulator smartMeterSimulator;

	private final String identificatore;

	private final Grafico grafico;
	private final Chord chord;


	public Casa(final String identificatore_, final String indirizzoREST_, final Chord chord_) throws URISyntaxException
	{
		super(indirizzoREST_);

		chord = chord_;
		identificatore = identificatore_;

		grafico = new Grafico(indirizzoREST_, identificatore, chord);

		smartMeterSimulator = new SmartMeterSimulator(
				new BufferImplWithOverlap(24, 12, this)
		);
		smartMeterSimulator.setName("__smartMeterSimulator");

		calcoloStatistiche = new Waiter("calcoloStatistiche", grafico::calcolaStatistiche, 2000);
		invioStatisticheCondominiali = new Thread(grafico::invioStatisticheCondominiali);
	}

	@Override
	public void close()
	{
		grafico.close();
	}


	//region Gestione Smart meter
	public void avviaSmartMeter()
	{
		invioStatisticheCondominiali.start();
		calcoloStatistiche.start();
		smartMeterSimulator.start();
	}

	public void fermaSmartMeter()
	{
		smartMeterSimulator.stopMeGently();
		calcoloStatistiche.stopMeGently();
		invioStatisticheCondominiali.stop();
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
		aggiungiStatisticaLocale(identificatore, mean.getValue(), mean.getKey());
	}
	//endregion


}