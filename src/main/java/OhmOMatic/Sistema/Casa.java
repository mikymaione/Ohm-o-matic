/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Casa implements MeanListener, AutoCloseable
{

	private SmartMeterSimulator smartMeterSimulator;
	private BufferImpl theBuffer;

	private Client client;
	private WebTarget webTargetRest;

	private Server gRPC_listner;
	private ManagedChannel gRPC_channel;
	private HomeServiceBlockingStub homeServiceBlockingStub;

	private final String RESTAddress;

	private final String myAddress;
	private final int myPort;

	private final String serverAddress;
	private final int serverPort;


	public Casa(String indirizzoREST_, String mioIndirizzo_, int miaPorta_, String indirizzoServerPeer_, int portaServerPeer_) throws IOException
	{
		RESTAddress = indirizzoREST_;

		myAddress = mioIndirizzo_;
		myPort = miaPorta_;

		serverAddress = indirizzoServerPeer_;
		serverPort = portaServerPeer_;
	}


	@Override
	public void close() throws InterruptedException
	{
		if (gRPC_channel != null)
			gRPC_channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);

		if (gRPC_listner != null)
			gRPC_listner.shutdown();
	}

	//region Funzioni P2P
	private HomeServiceBlockingStub getStub()
	{
		if (homeServiceBlockingStub == null)
		{
			gRPC_channel = ManagedChannelBuilder
					.forAddress(serverAddress, serverPort)
					.usePlaintext()
					.build();

			homeServiceBlockingStub = HomeServiceGrpc.newBlockingStub(gRPC_channel);
		}

		return homeServiceBlockingStub;
	}

	public boolean entraNelCondominio() throws Exception
	{
		if (GB.stringIsBlank(serverAddress))
		{

		}
		else
		{
			var stub = getStub();

			var c = casa.newBuilder()
					.setIP(myAddress)
					.setPort(myPort)
					.build();

			//var Res = stub.join(c);
			//var R = Res.getStandardRes();

			/*if (R.getOk())
			{
				var k = Res.getCasa();

			}
			else
				throw new Exception(R.getErrore());*/
		}

		return true;
	}

	public boolean esciDalCondominio() throws Exception
	{
		var stub = getStub();

		var c = casa.newBuilder()
				.setIP(myAddress)
				.setPort(myPort)
				.build();

		/*var R = stub.esciDalCondominio(c);

		if (!R.getOk())
			throw new Exception(R.getErrore());*/

		return true;
	}
	//endregion

	//region Chiamate WS
	private WebTarget getWebTarget()
	{
		if (webTargetRest == null)
		{
			client = ClientBuilder.newClient();
			webTargetRest = client.target(RESTAddress + "/OOM");
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

			final var par = casa.newBuilder()
					.setIP(myAddress)
					.setPort(myPort)
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

			final var par = casa.newBuilder()
					.setIP(myAddress)
					.setPort(myPort)
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

	public void inviaStatistiche(double mean)
	{
		//var ts = System.currentTimeMillis();
		//var k = GB.sha1(ID + "_" + ts);

		System.out.println("Mean: " + mean);
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
		inviaStatistiche(mean);
	}
	//endregion


}