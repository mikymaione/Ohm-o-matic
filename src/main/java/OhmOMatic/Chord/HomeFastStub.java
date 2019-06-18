package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class HomeFastStub implements AutoCloseable
{

	private ManagedChannel chan;

	public HomeServiceGrpc.HomeServiceBlockingStub getStub(NodeLink destination) throws Exception
	{
		var ip = destination.IP;
		var port = destination.port;

		chan = ManagedChannelBuilder
				.forAddress(ip, port)
				.usePlaintext()
				.build();

		var stato = chan.getState(true);

		switch (stato)
		{
			case SHUTDOWN:
			case TRANSIENT_FAILURE:
				throw new Exception("Server " + ip + ":" + port + " is closed.");
		}

		return HomeServiceGrpc.newBlockingStub(chan);
	}

	@Override
	public void close()
	{
		chan.shutdown();
	}


}