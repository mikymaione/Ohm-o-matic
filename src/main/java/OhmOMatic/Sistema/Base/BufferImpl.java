/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema.Base;

import OhmOMatic.Simulation.Buffer;
import OhmOMatic.Simulation.Measurement;

import java.util.ArrayList;

public final class BufferImpl implements Buffer
{

    private final int slidingWindowCount;

    private int buffer_index = -1;
    private Measurement[] buffer;

    private ArrayList<Double> medie;


    public BufferImpl(int SlidingWindowCount)
    {
        slidingWindowCount = SlidingWindowCount;
        buffer = new Measurement[SlidingWindowCount];
        medie = new ArrayList<>();
    }


    public double[] flushMedie()
    {
        synchronized (this)
        {
            var r = new double[medie.size()];
            var i = -1;

            for (final var m : medie)
                r[i += 1] = m;

            medie.clear();

            return r;
        }
    }

    @Override
    public void addMeasurement(Measurement m)
    {
        synchronized (this)
        {
            buffer[buffer_index += 1] = m;

            if (buffer_index + 1 == slidingWindowCount)
            {
                var sum = 0d;

                for (var e : buffer)
                    sum += e.getValue();

                final var media = sum / slidingWindowCount;
                medie.add(media);

                buffer_index = -1;
            }
        }
    }


}