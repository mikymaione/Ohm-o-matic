/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.HomeOuterClass;
import OhmOMatic.Simulation.SmartMeterSimulator;
import OhmOMatic.Sistema.Base.BufferImpl;
import OhmOMatic.Sistema.Base.MeanListener;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

public class Casa implements MeanListener
{

    private SmartMeterSimulator smartMeter;
    private BufferImpl theBuffer;

    private Client client;
    private WebTarget webTarget;

    final private String ID;


    public Casa(String indirizzoREST, String indirizzoServer, int numeroPorta, String id)
    {
        ID = id;

        client = ClientBuilder.newClient();
        webTarget = client.target(indirizzoREST + "/OOM");

        theBuffer = new BufferImpl(24, this);
        smartMeter = new SmartMeterSimulator(theBuffer);
    }


    public void iscrivitiAlCondominio()
    {
        try
        {
            var wt = webTarget
                    .path("iscriviCasa");

            final var par = HomeOuterClass.parametriCasaReq
                    .newBuilder()
                    .setID(ID)
                    .build();

            final var res = wt
                    .request()
                    .put(Entity.entity(par, "application/x-protobuf"), standardRes.class);

            if (res.getOk())
                System.out.println("OK!");
            else
                System.out.println(res.getErrore());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void disiscrivitiDalCondominio()
    {
        try
        {
            var wt = webTarget
                    .path("disiscriviCasa");

            final var par = HomeOuterClass.parametriCasaReq
                    .newBuilder()
                    .setID(ID)
                    .build();

            final var res = wt
                    .request()
                    .put(Entity.entity(par, "application/x-protobuf"), standardRes.class);

            if (res.getOk())
                System.out.println("OK!");
            else
                System.out.println(res.getErrore());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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