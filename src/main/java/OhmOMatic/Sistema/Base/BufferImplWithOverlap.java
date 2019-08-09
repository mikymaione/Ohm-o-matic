/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema.Base;

import OhmOMatic.Global.GB;
import OhmOMatic.Global.Pair;
import OhmOMatic.Simulation.Buffer;
import OhmOMatic.Simulation.Measurement;

import java.util.LinkedList;

public final class BufferImplWithOverlap implements Buffer
{

	private final int slidingWindowCount;
	private final int overlapCount;

	private final LinkedList<Measurement> buffer;

	private final MeanListener listener;


	public BufferImplWithOverlap(final int SlidingWindowCount, final int OverlapCount, MeanListener Listener)
	{
		listener = Listener;
		slidingWindowCount = SlidingWindowCount;
		overlapCount = OverlapCount;
		buffer = new LinkedList<>();
	}

	private Double _getSum()
	{
		var sum = 0d;

		for (var b : buffer)
			sum += b.getValue();

		return sum / slidingWindowCount;
	}

	private synchronized Double addMeasurement_sync(Measurement m)
	{
		buffer.offer(m);

		// riempito sliding window
		if (buffer.size() == slidingWindowCount)
		{
			return _getSum();
		}
		else if (buffer.size() == slidingWindowCount + overlapCount)
		{
			for (var i = 0; i < overlapCount; i++)
				buffer.poll();

			var s = _getSum();

			buffer.clear();

			return s;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void addMeasurement(Measurement m)
	{
		final var r = addMeasurement_sync(m);

		if (r != null)
			listener.meanGenerated(new Pair<>(r, GB.Now()));
	}


}