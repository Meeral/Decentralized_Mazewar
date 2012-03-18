import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;


public class MazewarServer extends Thread implements Runnable
{
	private ConnectionInfo cInfo = null;
	ArrayList<MazewarNetworkPacket> networkPacketList = new ArrayList<MazewarNetworkPacket>();
	
	public MazewarServer(int port)
	{
		cInfo = new ConnectionInfo();
		cInfo.multiCastPort = port;
	}
	
	private class ConnectionInfo 
	{
		private int multiCastPort = 4448;
		private InetAddress group = null;
		private String groupAddress = "239.222.222.222";
		private ServerSocketChannel serverSocketChannel = null;
		private InetSocketAddress socketaddress = null;
		private MulticastSocket mcast = null;
	}
	
	public void run()
	{
		try 
		{
			CreateLocalServer(cInfo);
		} 
		catch (Exception e) 
		{
			System.err.println("Could not launch server");
			e.printStackTrace();
		}
	}
	
	private void CreateLocalServer(ConnectionInfo ci) throws Exception
	{
		ConfigureAndOpenServerSocket(ci);
		
		CreateAndConfigureMulticastSocket(ci);
		
		while (true) 
		{
			SendRequest(ci, MazewarNetworkPacket.REQUEST_TO_JOIN);
			ListenForRequests(ci);
		}		
	}
	
	private void CreateAndConfigureMulticastSocket(ConnectionInfo ci) throws Exception
	{
		ci.mcast = new MulticastSocket(ci.multiCastPort);
		ci.group = InetAddress.getByName(ci.groupAddress);
		ci.mcast.joinGroup(ci.group);
	}
	
	private void ConfigureAndOpenServerSocket(ConnectionInfo ci) throws Exception
	{
		ci.socketaddress = new InetSocketAddress(ci.multiCastPort);
		ci.serverSocketChannel = ServerSocketChannel.open();
		ci.serverSocketChannel.socket().bind(ci.socketaddress);
		ci.serverSocketChannel.configureBlocking(false);
	}
	
	private void SendRequest(ConnectionInfo ci, int type) throws Exception
	{
		if (type == MazewarNetworkPacket.REQUEST_TO_JOIN)
		{
			ByteArrayOutputStream optStrm = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(optStrm);
	
			MazewarNetworkPacket mNet = createMazewarNetworkPacket(MazewarNetworkPacket.REQUEST_TO_JOIN);
	
			oos.writeObject(mNet);
			oos.flush();
			
			byte[] Buf= optStrm.toByteArray();
			DatagramPacket packet_to_send = new DatagramPacket(Buf, Buf.length, ci.group, ci.multiCastPort);
			
			ci.mcast.send(packet_to_send);
		}
	}
	
	private void ListenForRequests(ConnectionInfo ci) throws Exception
	{
		MazewarNetworkPacket o = getPacketFromStream(ci);
		
		if (o.type == MazewarNetworkPacket.REQUEST_TO_JOIN)
		{
			if (uniquePacket(o))
			{
				System.out.println("GOT UNIQUE PACKET");
				networkPacketList.add(o);
				System.out.println(networkPacketList.size());
			}
		}
	}
	
	private MazewarNetworkPacket getPacketFromStream(ConnectionInfo ci) throws Exception
	{
		byte[] bufferForNetworkPacket = new byte[5000];
		
		DatagramPacket dp = new DatagramPacket(bufferForNetworkPacket, bufferForNetworkPacket.length);
		ci.mcast.receive(dp); 
		
		//System.out.println(dp);
		
		ByteArrayInputStream byteArrInptStrm= new ByteArrayInputStream(bufferForNetworkPacket);
		ObjectInputStream inStrm = new ObjectInputStream(byteArrInptStrm);
		
		MazewarNetworkPacket o = (MazewarNetworkPacket) inStrm.readObject();
		
		return o;
	}
	
	private MazewarNetworkPacket createMazewarNetworkPacket(int type)
	{
		MazewarNetworkPacket mNet = new MazewarNetworkPacket();
		mNet.type = type;
		mNet.processId = this.getId() + this.cInfo.multiCastPort;
		return mNet;
	}
	
	private boolean uniquePacket(MazewarNetworkPacket mnp)
	{
		for (MazewarNetworkPacket o: networkPacketList)
		{
			if (mnp.processId == o.processId)
			{
				return false;
			}
		}
	
		return true;
	}
	
}
