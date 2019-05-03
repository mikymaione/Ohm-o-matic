/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import Simulation.SmartMeterSimulator;
import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid("8ad801e7-a53d-47cf-85d5-95e9fde97cb7")
public class Casa
{
    @objid("7c5744d7-0c07-44af-a9df-b21c42393d1d")
    private SmartMeterSimulator smartMeter;

    @objid("8d83ef88-638b-4b63-bb86-cbccd7ad0227")
    public void calcolaConsumoEnergeticoComplessivo()
    {
    }

    @objid("57119a10-61a4-4a38-9560-bb7b3c031768")
    public void inviaStatisticheAlServer()
    {
    }

    @objid("d67e3d97-e006-4a08-9d63-3762c67b0e2d")
    public void richiediAlCondominioDiPoterConsumareOltreLaMedia()
    {
    }

    @objid("952733de-5083-423d-ac79-826f8ca359d2")
    public Casa(String indirizzoServer, int numeroPorta, int ID)
    {
    }

    @objid("2e845e41-6899-457f-a1da-6cfe9977b086")
    public void avviaSmartMeter()
    {
    }

    @objid("6b0b951c-c32e-4d90-8277-8693fa228652")
    public void fermaSmartMeter()
    {
    }

    @objid("9b6c1469-2684-4e95-aba1-0459caf84aa7")
    public void iscrivitiAlCondominio()
    {
    }

    @objid("3293b2f6-4c19-41b7-906f-dac78aacc25d")
    public void disiscrivitiDalCondominio()
    {
    }

}
