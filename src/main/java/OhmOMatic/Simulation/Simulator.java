/*
MIT License
Copyright (c) 2019 Gabriele Civitarese
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Simulation;

import java.util.Calendar;
import java.util.Random;

public abstract class Simulator extends Thread
{

	protected volatile boolean stopCondition = false;

	private long midnight;

	private String id;

	private String type;

	protected Random rnd = new Random();

	private Buffer buffer;


	public abstract void run();


	public Simulator(String id, String type, Buffer buffer)
	{
		this.id = id;
		this.type = type;
		this.buffer = buffer;
		this.midnight = computeMidnightMilliseconds();
	}

	public void stopMeGently()
	{
		stopCondition = true;
	}

	protected void addMeasurement(double measurement)
	{
		buffer.addMeasurement(new Measurement(id, type, measurement, deltaTime()));
	}

	public Buffer getBuffer()
	{
		return buffer;
	}

	protected void sensorSleep(long milliseconds)
	{
		try
		{
			Thread.sleep(milliseconds);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private long computeMidnightMilliseconds()
	{
		Calendar c = Calendar.getInstance();

		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTimeInMillis();
	}

	private long deltaTime()
	{
		return System.currentTimeMillis() - midnight;
	}

	public String getIdentifier()
	{
		return id;
	}

}