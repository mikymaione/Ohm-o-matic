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
						private void CSR(StreamObserver<Common.standardRes> responseObserver, boolean ok, String _error, String msg)
						{
							var res = Common.standardRes.newBuilder()
									.setOk(ok)
									.setErrore(_error)
									.setMsg(msg)
									.build();

							responseObserver.onNext(res);
							responseObserver.onCompleted();
						}

						private void CSR(StreamObserver<Common.standardRes> responseObserver, String msg)
						{
							CSR(responseObserver, true, "", msg);
						}

						private void CSRE(StreamObserver<Common.standardRes> responseObserver, String _error)
						{
							CSR(responseObserver, true, _error, "");
						}

						@Override
						public void cLOSEST(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							try
							{
								var result = local.closest_preceding_finger(request.getID());
								var ip = result.getAddress().getHostAddress();
								var port = result.getPort();
								var ret = "MYCLOSEST_" + ip + ":" + port;

								CSR(responseObserver, ret);
							}
							catch (Exception e)
							{
								CSRE(responseObserver, e.getMessage());
							}
						}

						@Override
						public void fINDSUCC(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							try
							{
								var result = local.find_successor(request.getID());
								var ip4 = result.getAddress().getHostAddress();
								var port4 = result.getPort();
								var ret = "FOUNDSUCC_" + ip4 + ":" + port4;

								CSR(responseObserver, ret);
							}
							catch (Exception e)
							{
								CSRE(responseObserver, e.getMessage());
							}
						}

						@Override
						public void iAMPRE(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							try
							{
								var param = request.getIP() + ":" + request.getPort();
								var new_pre = Helper.createSocketAddress(param);
								local.notified(new_pre);

								var ret = "NOTIFIED";

								CSR(responseObserver, ret);
							}
							catch (Exception e)
							{
								CSRE(responseObserver, e.getMessage());
							}
						}

						@Override
						public void kEEP(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							CSR(responseObserver, "ALIVE");
						}

						@Override
						public void requestAddress(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							super.requestAddress(request, responseObserver);
						}

						@Override
						public void yOURPRE(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							try
							{
								var ret = "";
								var result = local.getPredecessor();

								if (result != null)
								{
									var ip3 = result.getAddress().getHostAddress();
									var port3 = result.getPort();

									ret = "MYPRE_" + ip3 + ":" + port3;
								}
								else
								{
									ret = "NOTHING";
								}

								CSR(responseObserver, ret);
							}
							catch (Exception e)
							{
								CSRE(responseObserver, e.getMessage());
							}
						}

						@Override
						public void yOURSUCC(Home.casa request, StreamObserver<Common.standardRes> responseObserver)
						{
							try
							{
								var ret = "";
								var result = local.getSuccessor();

								if (result != null)
								{
									String ip2 = result.getAddress().getHostAddress();
									int port2 = result.getPort();
									ret = "MYSUCC_" + ip2 + ":" + port2;
								}
								else
								{
									ret = "NOTHING";
								}

								CSR(responseObserver, ret);
							}
							catch (Exception e)
							{
								CSRE(responseObserver, e.getMessage());
							}
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