/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema.Base;

import OhmOMatic.Simulation.Buffer;
import OhmOMatic.Simulation.Measurement;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class BufferImpl implements Buffer
{

    public final int SlidingWindowCount;

    public ArrayList<Double> Medie = new ArrayList<>();

    private ArrayDeque<Measurement> buffer;


    public BufferImpl()
    {
        this(24);
    }

    public BufferImpl(int SlidingWindowCount)
    {
        this.SlidingWindowCount = SlidingWindowCount;
        buffer = new ArrayDeque<>(SlidingWindowCount);
    }


    private void Elabora()
    {
        var somma = 0.0;

        for (var e : buffer)
            somma += e.getValue();

        var media = somma / SlidingWindowCount;
        Medie.add(media);

        for (var i = 0; i < SlidingWindowCount / 2; i++)
            buffer.remove();
    }

    @Override
    public void addMeasurement(Measurement m)
    {
        synchronized (this)
        {
            buffer.add(m);

            if (buffer.size() == SlidingWindowCount)
                Elabora();
        }
    }

}