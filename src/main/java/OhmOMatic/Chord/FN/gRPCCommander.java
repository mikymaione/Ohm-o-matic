package OhmOMatic.Chord.FN;

import OhmOMatic.Chord.Chord;
import OhmOMatic.ProtoBuffer.Common;
import OhmOMatic.ProtoBuffer.Home;
import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.function.Function;

public class gRPCCommander
{

	private static void gestioneErroreRequest(Common.standardRes R) throws Exception
	{
		if (R == null)
			throw new Exception("Errore richiesta!");

		if (!R.getOk())
			throw new Exception(R.getErrore());
	}

	public static NodeLink gRPC_A(NodeLink server, Richiesta req)
	{
		return gRPC_A(server, req, -1);
	}

	public static NodeLink gRPC_A(NodeLink server, Richiesta req, long _id)
	{
		try
		{
			var response = gRPC_E(server, req, _id);

			if (response.getNullValue())
			{
				return null;
			}
			else if (response.getStandardRes().getOk())
			{
				var c = response.getCasa();

				return new NodeLink(c.getIP(), c.getPort());
			}
			else
			{
				return server;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static Home.casaRes gRPC_E(NodeLink server, Richiesta req, long _id) throws Exception
	{
		return gRPC_E(server, req, _id, -1, NodeLink.Empty());
	}

	public static Home.casaRes gRPC_E(NodeLink server, Richiesta req, NodeLink setNode) throws Exception
	{
		return gRPC_E(server, req, -1, -1, setNode);
	}

	public static Home.casaRes gRPC_E(NodeLink server, Richiesta req, long _id, int indice, NodeLink setNode) throws Exception
	{
		try (var hfs = new HomeFastStub())
		{
			var stub = hfs.getStub(server);

			var c = Home.casa.newBuilder()
					.setID(_id)
					.setIdx(indice)
					.setIP(server.IP)
					.setPort(server.port)
					.setOptionalIP(setNode.IP)
					.setOptionalPort(setNode.port)
					.build();

			Home.casaRes CR;
			Common.standardRes R;

			switch (req)
			{
				case Notify:
					CR = stub.notify(c);
					break;

				case SetPredecessor:
					CR = stub.setPredecessor(c);
					break;

				case UpdateFingerTable:
					CR = stub.updateFingerTable(c);
					break;

				case ClosestPrecedingFinger:
					CR = stub.closestPrecedingFinger(c);
					break;

				case FindSuccessor:
					CR = stub.findSuccessor(c);
					break;

				case Predecessor:
					CR = stub.predecessor(c);
					break;

				case Successor:
					CR = stub.successor(c);
					break;

				default:
					throw new Exception("Switch " + req + " non implementato");
			}

			R = CR.getStandardRes();
			gestioneErroreRequest(R);

			return CR;
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public static HomeServiceGrpc.HomeServiceImplBase getListnerServer(Chord local)
	{
		return new HomeServiceGrpc.HomeServiceImplBase()
		{
			private void STD_PRO(Home.casa request, StreamObserver<Home.casaRes> responseObserver, java.util.function.Consumer<NodeLink> pro)
			{
				Common.standardRes sr;
				var cr = Home.casa.newBuilder()
						.build();

				try
				{

					NodeLink n_ = null;

					if (request.getOptionalPort() > 0)
						n_ = new NodeLink(request.getOptionalIP(), request.getOptionalPort());

					pro.accept(n_);

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(false)
							.setErrore(e.getMessage())
							.build();
				}

				var res = Home.casaRes.newBuilder()
						.setStandardRes(sr)
						.setCasa(cr)
						.setNullValue(false)
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}

			private void STD_FUN(Home.casa request, StreamObserver<Home.casaRes> responseObserver, Function<NodeLink, NodeLink> fun)
			{
				var NullValue = false;

				Common.standardRes sr;
				var cr = Home.casa.newBuilder()
						.build();

				try
				{
					var n_ = new NodeLink(request.getOptionalIP(), request.getOptionalPort());

					var R = fun.apply(n_);

					if (R == null)
						NullValue = true;
					else
						cr = Home.casa.newBuilder()
								.setIP(R.IP)
								.setPort(R.port)
								.build();

					sr = Common.standardRes.newBuilder()
							.setOk(true)
							.build();
				}
				catch (Exception e)
				{
					sr = Common.standardRes.newBuilder()
							.setOk(false)
							.setErrore(e.getMessage())
							.build();
				}

				var res = Home.casaRes.newBuilder()
						.setStandardRes(sr)
						.setCasa(cr)
						.setNullValue(NullValue)
						.build();

				responseObserver.onNext(res);
				responseObserver.onCompleted();
			}

			@Override
			public void notify(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				STD_PRO(request, responseObserver, n_ ->
						local.notify(n_));
			}

			@Override
			public void updateFingerTable(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				STD_PRO(request, responseObserver, n_ ->
				{
					try
					{
						local.update_finger_table(n_, request.getIdx());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				});
			}

			@Override
			public void setPredecessor(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				STD_PRO(request, responseObserver, n_ ->
						local.setPredecessor(n_));
			}

			@Override
			public void findSuccessor(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				STD_FUN(request, responseObserver, n_ ->
						local.find_successor(request.getID()));
			}

			@Override
			public void closestPrecedingFinger(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				STD_FUN(request, responseObserver, n_ ->
						local.closest_preceding_finger(request.getID()));
			}

			@Override
			public void predecessor(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				STD_FUN(request, responseObserver, n_ ->
						local.getPredecessor());
			}

			@Override
			public void successor(Home.casa request, StreamObserver<Home.casaRes> responseObserver)
			{
				STD_FUN(request, responseObserver, n_ ->
						local.getSuccessor());
			}
		};
	}


}