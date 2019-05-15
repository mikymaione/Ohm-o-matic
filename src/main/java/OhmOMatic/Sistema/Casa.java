/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.Simulation.SmartMeterSimulator;
import OhmOMatic.Sistema.Base.BufferImpl;
import OhmOMatic.Sistema.Base.MeanListener;

public class Casa implements MeanListener
{

    private SmartMeterSimulator smartMeter;
    private BufferImpl theBuffer;


    public Casa(String indirizzoServer, int numeroPorta, String ID)
    {
        theBuffer = new BufferImpl(24, this);
        smartMeter = new SmartMeterSimulator(theBuffer);
    }


    public void iscrivitiAlCondominio()
    {

    }

    public void disiscrivitiDalCondominio()
    {

    }


    public void calcolaConsumoEnergeticoComplessivo()
    {

    }

    public void richiediAlCondominioDiPoterConsumareOltreLaMedia()
    {

    }

    public void inviaStatisticheAlServer(double mean)
    {
        System.out.println(mean);
    }


    public void avviaSmartMeter()
    {
        smartMeter.run();
    }

    public void fermaSmartMeter()
    {
        smartMeter.stop();
    }

    @Override
    public void meanGenerated(double mean)
    {
        inviaStatisticheAlServer(mean);
    }


}