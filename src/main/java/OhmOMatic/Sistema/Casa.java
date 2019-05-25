/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.Global.GB;
import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.Home.casa;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc.HomeServiceBlockingStub;
import OhmOMatic.Simulation.SmartMeterSimulator;
import OhmOMatic.Sistema.Base.BufferImpl;
import OhmOMatic.Sistema.Base.MeanListener;
import OhmOMatic.Sistema.Chord.ChordNode;
import io.grpc.ManagedChannelBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

public class Casa implements MeanListener
{

    private SmartMeterSimulator smartMeterSimulator;
    private BufferImpl theBuffer;

    private Client client;
    private WebTarget webTargetRest;

    private HomeServiceBlockingStub homeServiceBlockingStub;

    private ChordNode chord;

    final private String ID;
    final private String indirizzoREST;
    final private String mioIndirizzo;
    final private int mioPorta;
    final private String indirizzoServerPeer;
    final private int portaServerPeer;


    public Casa(String id, String indirizzoREST_, String mioIndirizzo_, int mioPorta_, String indirizzoServerPeer_, int portaServerPeer_)
    {
        ID = id;

        indirizzoREST = indirizzoREST_;

        mioIndirizzo = mioIndirizzo_;
        mioPorta = mioPorta_;

        indirizzoServerPeer = indirizzoServerPeer_;
        portaServerPeer = portaServerPeer_;
    }


    //region Funzioni P2P
    private HomeServiceBlockingStub getStub()
    {
        if (homeServiceBlockingStub == null)
        {
            var channel = ManagedChannelBuilder
                    .forAddress(indirizzoServerPeer, portaServerPeer)
                    .usePlaintext()
                    .build();

            homeServiceBlockingStub = HomeServiceGrpc.newBlockingStub(channel);
        }

        return homeServiceBlockingStub;
    }

    public void entraNelCondominio() throws Exception
    {
        if (GB.stringIsBlank(indirizzoServerPeer))
        {
            chord = new ChordNode(mioIndirizzo + ":" + mioPorta);
        }
        else
        {
            var stub = getStub();

            var c = casa.newBuilder()
                    .setID("Goku")
                    .setIP("localhost")
                    .setPort(8000)
                    .build();

            var R = stub.entraNelCondominio(c);

            if (R.getOk())
                chord = new ChordNode(mioIndirizzo + ":" + mioPorta, indirizzoServerPeer + ":" + portaServerPeer);
            else
                throw new Exception(R.getErrore());
        }
    }
    //endregion

    //region Chiamate WS
    private WebTarget getWebTarget()
    {
        if (webTargetRest == null)
        {
            client = ClientBuilder.newClient();
            webTargetRest = client.target(indirizzoREST + "/OOM");
        }

        return webTargetRest;
    }

    public void iscriviCasa()
    {
        try
        {
            var webTarget = getWebTarget();

            var wt = webTarget
                    .path("iscriviCasa");

            final var par = casa
                    .newBuilder()
                    .setID(ID)
                    .build();

            final var resListaCase = wt
                    .request()
                    .put(Entity.entity(par, "application/x-protobuf"), listaCase.class);

            final var res = resListaCase.getStandardResponse();

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

    public void disiscriviCasa()
    {
        try
        {
            var webTarget = getWebTarget();

            var wt = webTarget
                    .path("disiscriviCasa");

            final var par = casa
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

    public void inviaStatisticheAlServer(double mean)
    {
        System.out.println(mean);
    }
    //endregion

    //region Funzioni sul calcolo del consumo energetico
    public void calcolaConsumoEnergeticoComplessivo()
    {

    }

    public void richiediAlCondominioDiPoterConsumareOltreLaMedia()
    {

    }
    //endregion

    //region Gestione Smart meter
    private SmartMeterSimulator getSmartMeter()
    {
        if (smartMeterSimulator == null)
        {
            theBuffer = new BufferImpl(24, this);
            smartMeterSimulator = new SmartMeterSimulator(theBuffer);
        }

        return smartMeterSimulator;
    }

    public void avviaSmartMeter()
    {
        var smartMeter = getSmartMeter();

        smartMeter.run();
    }

    public void fermaSmartMeter()
    {
        var smartMeter = getSmartMeter();

        smartMeter.stop();
    }

    @Override
    public void meanGenerated(double mean)
    {
        inviaStatisticheAlServer(mean);
    }
    //endregion


}