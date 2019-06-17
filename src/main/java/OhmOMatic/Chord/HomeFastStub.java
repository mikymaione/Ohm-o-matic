package OhmOMatic.Chord;

import OhmOMatic.ProtoBuffer.HomeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.net.InetSocketAddress;

public class HomeFastStub implements AutoCloseable
{

	private ManagedChannel chan;

	public HomeServiceGrpc.HomeServiceBlockingStub getStub(InetSocketAddress destination)
	{
		chan = ManagedChannelBuilder
				.forAddress(destination.getAddress().getHostAddress(), destination.getPort())
				.usePlaintext()
				.build();

		return HomeServiceGrpc.newBlockingStub(chan);
	}

	@Override
	public void close()
	{
		chan.shutdown();
	}


}