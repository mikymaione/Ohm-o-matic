/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema.Base;

import OhmOMatic.Simulation.Buffer;
import OhmOMatic.Simulation.Measurement;

public final class BufferImplWithoutOverlap implements Buffer
{

	private final int slidingWindowCount;

	private int buffer_index = -1;
	private final Measurement[] buffer;

	private final MeanListener listener;


	public BufferImplWithoutOverlap(final int SlidingWindowCount, MeanListener Listener)
	{
		listener = Listener;
		slidingWindowCount = SlidingWindowCount;
		buffer = new Measurement[SlidingWindowCount];
	}

	private synchronized addMeasurementResult addMeasurement_sync(Measurement m)
	{
		buffer[buffer_index += 1] = m;

		// riempito sliding window
		if (buffer_index + 1 == slidingWindowCount)
		{
			buffer_index = -1;

			var sum = 0d;

			for (var e : buffer)
				sum += e.getValue();

			final var media = sum / slidingWindowCount;

			return new addMeasurementResult(media, true);
		}
		else
		{
			return new addMeasurementResult(0d, false);
		}
	}

	@Override
	public void addMeasurement(Measurement m)
	{
		final var r = addMeasurement_sync(m);

		if (r.sendStats)
			listener.meanGenerated(r.media);
	}


	private static class addMeasurementResult
	{
		final double media;
		final boolean sendStats;

		addMeasurementResult(final double media, final boolean sendStats)
		{
			this.media = media;
			this.sendStats = sendStats;
		}
	}


}