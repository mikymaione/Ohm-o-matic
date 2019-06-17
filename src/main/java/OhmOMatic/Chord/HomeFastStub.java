package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.net.InetSocketAddress;

public class HomeFastStub implements AutoCloseable
{

	private ManagedChannel chan;

	public HomeServiceGrpc.HomeServiceBlockingStub getStub(InetSocketAddress destination) throws Exception
	{
		var ip = destination.getAddress().getHostAddress();
		var port = destination.getPort();

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