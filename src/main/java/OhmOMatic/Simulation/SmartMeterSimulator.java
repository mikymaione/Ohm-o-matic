/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Simulation;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid("6c637d92-6ae8-4e47-bcf3-b36e3ca247ac")
public class SmartMeterSimulator extends Simulator
{
    @objid("6c297c7d-aace-4ed1-8e88-3e5184a1cc92")
    private final double A = 0.4;

    @objid("f1e33dcd-f992-4d64-82b7-8a9144aa59b1")
    private final double W = 0.01;

    @objid("8aecdb0b-d458-49b2-ad1b-68eaab6754f3")
    private static int ID = 1;

    @objid("394fcfaa-d3d1-45ea-94fd-dfc5d1cc472f")
    private volatile boolean boost;

    @objid("3f6c61f5-1fb4-42da-bb2a-d87c77f509e2")
    public SmartMeterSimulator(String id, Buffer buffer)
    {
        super(id, "Plug", buffer);
    }

    @objid("7b926570-f76a-4288-b141-7cc3e3ee21c3")
    public SmartMeterSimulator(Buffer buffer)
    {
        super("plug-" + (ID++), "Plug", buffer);
    }

    @objid("9e46387f-3fbb-47f8-86fe-90f01e9ecbab")
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

    @objid("04d0646d-c1c9-4d69-9917-2e6c211b315a")
    private double getElecticityValue(double t)
    {
        return Math.abs(A * Math.sin(W * t) + rnd.nextGaussian() * 0.3);
    }

    @objid("a4d13ed1-f2f6-4175-893c-3d4ea7feffc3")
    public void boost() throws InterruptedException
    {
        boost = true;

        Thread.sleep(5000);

        boost = false;
    }

}
