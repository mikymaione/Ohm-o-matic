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
import OhmOMatic.ProtoBuffer.Home.casaRes;
import OhmOMatic.ProtoBuffer.Home.listaCase;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc.HomeServiceBlockingStub;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc.HomeServiceImplBase;
import OhmOMatic.Simulation.SmartMeterSimulator;
import OhmOMatic.Sistema.Base.BufferImpl;
import OhmOMatic.Sistema.Base.MeanListener;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

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

	private final Chord chord;

	private static byte[] toSha1(String ip, int port)
	{
		return GB.sha1(ip + ":" + port);
	}

	public Casa(String indirizzoREST_, String mioIndirizzo_, int miaPorta_, String indirizzoServerPeer_, int portaServerPeer_) throws IOException
	{
		RESTAddress = indirizzoREST_;

		myAddress = mioIndirizzo_;
		myPort = miaPorta_;

		chord = new Chord(mioIndirizzo_, miaPorta_);

		serverAddress = indirizzoServerPeer_;
		serverPort = portaServerPeer_;

		start_gRPC_Listening();
	}


	@Override
	public void close() throws InterruptedException
	{
		if (gRPC_channel != null)
			gRPC_channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);

		if (gRPC_listner != null)
			gRPC_listner.shutdown();
	}

	//region Chord
	private final static int mBit = 160; //sha1

	class Chord
	{
		private final byte[] key;
		private Chord predecessor, successor;

		private volatile Chord[] finger = new Chord[mBit];
		private int next;

		Chord(String mioIndirizzo_, int miaPorta_)
		{
			key = toSha1(mioIndirizzo_, miaPorta_);
			predecessor = null;
			successor = this;
		}
	}

	// ask node n to find the successor of id
	private static Chord find_successor(Chord c, byte[] key)
	{
		if (GB.compreso(key, c.key, c.successor.key))
		{
			return c.successor;
		}
		else
		{
			var n0 = closest_preceding_node(c, key);

			return find_successor(n0, key);
		}
	}

	// search the local table for the highest predecessor of id
	private static Chord closest_preceding_node(Chord c, byte[] key)
	{
		for (var i = mBit - 1; i-- > 0; )
			if (GB.compreso(c.finger[i].key, c.key, key))
				return c.finger[i];

		return c;
	}

	// join a Chord ring containing node n_
	private static void join(Chord c, Chord n_)
	{
		c.predecessor = null;
		c.successor = find_successor(n_, c.key);
	}

	// called periodically. n asks the successor
	// about its predecessor, verifies if n's immediate
	// successor is consistent, and tells the successor about n
	private static void stabilize(Chord c)
	{
		var x = c.successor.predecessor;

		if (GB.compreso(x.key, c.key, c.successor.key))
			c.successor = x;

		notify_(c.successor, c);
	}

	// n_ thinks it might be our predecessor.
	private static void notify_(Chord c, Chord n_)
	{
		if (c.predecessor == null || GB.compreso(n_.key, c.predecessor.key, c.key))
			c.predecessor = n_;
	}

	// called periodically. refreshes finger table entries.
	// next stores the index of the finger to fix
	private static void fix_fingers(Chord c)
	{
		c.next++;

		if (c.next > mBit)
			c.next = 1;

		c.finger[c.next] = find_successor(c, null);
	}

	// called periodically. checks whether predecessor has failed.
	private static void check_predecessor(Chord c)
	{
		if (isActive(c, c.predecessor))
			c.predecessor = null;
	}

	private static boolean isActive(Chord c, Chord e)
	{
		return true;
	}
	//endregion

	//region Funzioni P2P
	private void start_gRPC_Listening() throws IOException
	{
		gRPC_listner = ServerBuilder
				.forPort(myPort)
				.addService(new HomeServiceImplBase()
				{
					@Override
					public void join(casa request, StreamObserver<casaRes> responseObserver)
					{
						var res = casaRes.newBuilder()
								.setStandardRes(
										standardRes.newBuilder()
												.setOk(true)
												.build()
								)
								.setCasa(
										casa.newBuilder()
												.setIP(myAddress)
												.setPort(myPort)
												.build()
								)
								.build();

						responseObserver.onNext(res);
						responseObserver.onCompleted();
					}

					@Override
					public void esciDalCondominio(casa request, StreamObserver<standardRes> responseObserver)
					{
						var res = standardRes.newBuilder()
								.setOk(true)
								.build();

						responseObserver.onNext(res);
						responseObserver.onCompleted();
					}
				})
				.build()
				.start();
	}

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

			var Res = stub.join(c);
			var R = Res.getStandardRes();

			if (R.getOk())
			{
				var k = Res.getCasa();

			}
			else
				throw new Exception(R.getErrore());
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

		var R = stub.esciDalCondominio(c);

		if (!R.getOk())
			throw new Exception(R.getErrore());

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