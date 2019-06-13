/*
MIT License
Copyright (c) 2019 Gabriele Civitarese
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Simulation;

public class SmartMeterSimulator extends Simulator
{
	private final double A = 0.4;
	private final double W = 0.01;

	private static int ID = 1;

	private volatile boolean boost;


	public SmartMeterSimulator(String id, Buffer buffer)
	{
		super(id, "Plug", buffer);
	}

	public SmartMeterSimulator(Buffer buffer)
	{
		super("plug-" + (ID++), "Plug", buffer);
	}


	@Override
	public void run()
	{
		double i = rnd.nextInt();
		long waitingTime;

		while (!stopCondition)
		{
			double value = getElecticityValue(i);

			if (boost)
				value += 3;

			addMeasurement(value);

			waitingTime = 100 + (int) (Math.random() * 200);
			sensorSleep(waitingTime);

			i += 0.2;
		}
	}

	private double getElecticityValue(double t)
	{
		return Math.abs(A * Math.sin(W * t) + rnd.nextGaussian() * 0.3);
	}

	public void boost() throws InterruptedException
	{
		boost = true;

		Thread.sleep(5000);

		boost = false;
	}

}