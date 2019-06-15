package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

/**
 * Listener thread that keeps listening to a port and asks talker thread to process
 * when a request is accepted.
 *
 * @author Chuan Xia
 */

public class Listener extends Thread
{

	private Server gRPC_listner;
	private Node local;
	private final int port;


	public Listener(Node n)
	{
		var localAddress = n.getAddress();
		port = localAddress.getPort();
		local = n;
	}

	@Override
	public void run()
	{
		try
		{
			gRPC_listner = ServerBuilder
					.forPort(port)
					.addService(new HomeServiceGrpc.HomeServiceImplBase()
					{
						@Override
						public void cLOSEST(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
						{
							Common.standardRes sr;
							var cr = Home.casa.newBuilder()
									.build();

							try
							{
								var result = local.closest_preceding_finger(request.getID());
								var _ip = result.getAddress().getHostAddress();
								var _port = result.getPort();
								var ret = "MYCLOSEST_" + _ip + ":" + _port;

								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setMsg(ret)
										.build();

								cr = Home.casa.newBuilder()
										.setIP(_ip)
										.setPort(_port)
										.build();
							}
							catch (Exception e)
							{
								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setErrore(e.getMessage())
										.build();
							}

							var res = Home.casaRes.newBuilder()
									.setStandardRes(sr)
									.setCasa(cr)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}

						@Override
						public void fINDSUCC(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
						{
							Common.standardRes sr;
							var cr = Home.casa.newBuilder()
									.build();

							try
							{
								var result = local.find_successor(request.getID());
								var _ip = result.getAddress().getHostAddress();
								var _port = result.getPort();
								var ret = "FOUNDSUCC_" + _ip + ":" + _port;

								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setMsg(ret)
										.build();

								cr = Home.casa.newBuilder()
										.setIP(_ip)
										.setPort(_port)
										.build();
							}
							catch (Exception e)
							{
								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setErrore(e.getMessage())
										.build();
							}

							var res = Home.casaRes.newBuilder()
									.setStandardRes(sr)
									.setCasa(cr)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}

						@Override
						public void iAMPRE(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
						{
							Common.standardRes sr;
							var cr = Home.casa.newBuilder()
									.build();

							try
							{
								var param = request.getIP() + ":" + request.getPort();
								var new_pre = Helper.createSocketAddress(param);
								local.notified(new_pre);
								var ret = "NOTIFIED";

								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setMsg(ret)
										.build();
							}
							catch (Exception e)
							{
								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setErrore(e.getMessage())
										.build();
							}

							var res = Home.casaRes.newBuilder()
									.setStandardRes(sr)
									.setCasa(cr)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}

						@Override
						public void kEEP(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							var res = Common.standardRes.newBuilder()
									.setOk(true)
									.setMsg("ALIVE")
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}

						@Override
						public void requestAddress(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							super.requestAddress(request, responseObserver);
						}

						@Override
						public void yOURPRE(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
						{
							Common.standardRes sr;
							var cr = Home.casa.newBuilder()
									.build();

							try
							{
								var ret = "";
								var result = local.getPredecessor();

								if (result != null)
								{
									var _ip = result.getAddress().getHostAddress();
									var _port = result.getPort();

									ret = "MYPRE_" + _ip + ":" + _port;

									cr = Home.casa.newBuilder()
											.setIP(_ip)
											.setPort(_port)
											.build();
								}
								else
								{
									ret = "NOTHING";
								}

								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setMsg(ret)
										.build();
							}
							catch (Exception e)
							{
								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setErrore(e.getMessage())
										.build();
							}

							var res = Home.casaRes.newBuilder()
									.setStandardRes(sr)
									.setCasa(cr)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}

						@Override
						public void yOURSUCC(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
						{
							Common.standardRes sr;
							var cr = Home.casa.newBuilder()
									.build();

							try
							{
								var ret = "";
								var result = local.getSuccessor();

								if (result != null)
								{
									var _ip = result.getAddress().getHostAddress();
									var _port = result.getPort();
									ret = "MYSUCC_" + _ip + ":" + _port;

									cr = Home.casa.newBuilder()
											.setIP(_ip)
											.setPort(_port)
											.build();
								}
								else
								{
									ret = "NOTHING";
								}

								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setMsg(ret)
										.build();
							}
							catch (Exception e)
							{
								sr = Common.standardRes.newBuilder()
										.setOk(true)
										.setErrore(e.getMessage())
										.build();
							}

							var res = Home.casaRes.newBuilder()
									.setStandardRes(sr)
									.setCasa(cr)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}


						@Override
						public void join(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
						{
							var res = Home.casaRes.newBuilder()
									.setStandardRes(
											Common.standardRes.newBuilder()
													.setOk(true)
													.build()
									)
									.setCasa(
											Home.casa.newBuilder()
													.build()
									)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}

						@Override
						public void esciDalCondominio(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							var res = Common.standardRes.newBuilder()
									.setOk(true)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}
					})
					.build()
					.start();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot talk.\nServer port: " + local.getAddress().getPort() + "; Talker port: " + port, e);
		}
	}

	public void toDie()
	{
		gRPC_listner.shutdown();
	}


}