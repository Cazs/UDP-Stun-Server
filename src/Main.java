import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.StringTokenizer;


public class Main 
{
	private static DatagramSocket server				= null;
	private static ArrayList<AddressMap> addressMaps	= null;
	private static ArrayList<Message> messageQueue		= null;
	private static javax.swing.Timer timer				= null;
	private static int hostalias						= 0;
	private static boolean debugging					= false;
	
	public Main()
	{
		try 
		{
			server = new DatagramSocket(4242);
			addressMaps = new ArrayList<AddressMap>();
			messageQueue = new ArrayList<Message>();
			//server.bind(InetAddress.getByName("localhost"),4242);
		} 
		catch (SocketException e) 
		{
			System.err.println(e.getMessage());
			System.err.println("Buh-Bye!");
			System.exit(0);
		}
	}
	
	public static void main(String[] args) 
	{
		new Main();
		
		Thread t = new Thread(new Runnable()
		{
			
			@Override
			public void run() 
			{
				try 
				{
					initialiseMessageQueueHandler(5000);
					listen();
				} 
				catch (IOException e) 
				{
					System.err.println(e.getMessage());
				}
			}
		});
		t.start();
	}
	
	private static void listen() throws IOException
	{
		System.out.println("WAKING UP...");
		boolean listen = true;
		byte[] buffer = new byte[2048];
		DatagramPacket inbound = new DatagramPacket(buffer, buffer.length);
		System.out.println("AWAKE, LISTENING...");
		while(listen)
		{
			server.receive(inbound);
			System.out.println("RECEIVED REQ FROM: " + inbound.getAddress() + ":" + inbound.getPort());
			
			sendPacket("ACK",InetAddress.getByName(inbound.getAddress().getHostAddress()), 4243);//ACK
			boolean shutdown = processPacket(inbound);
			if(shutdown)
				listen = false;
		}
	}
	
	private static boolean processPacket(DatagramPacket inbound) throws UnknownHostException
	{
		System.out.println("PROCESSING PACKET...");
		String line = new String(inbound.getData(),0,inbound.getLength());
		System.out.println(">>"+line);
		StringTokenizer tokenizer = new StringTokenizer(line,"	");
		String cmd = tokenizer.nextToken();
		switch(cmd)
		{
			case "SEND TO"://format: SEND TO	<MSG>	<DEST>
				if(ipInAddressMap(inbound.getAddress().getHostAddress()))
				{
					//Update port
					AddressMap am = searchMapping(inbound.getAddress().getHostAddress());
					am.setPort(inbound.getPort());
				}
				else
				{
					//New Host has joined - add the're IP,port and alias to the table
					addressMaps.add(new AddressMap(inbound.getAddress().getHostAddress(),inbound.getPort(),"sy.srvr.host"+hostalias));
					hostalias++;
				}
				//To message queue - actual sendingHello, can you hear me
				String msg = tokenizer.nextToken();
				String host = tokenizer.nextToken();
				String ip = getAliasIP("sy.srvr.host" + host);
				if(!ip.equals(""))
						messageQueue.add(new Message(msg, ip, inbound.getAddress().getHostAddress(),StunjProtocolCodes.MESSAGE.getCode()));
				else
				{
					System.out.println("ERROR:	INVALID HOST ALIAS:"+host);
					sendPacket("ERROR	INVALID HOST ALIAS:" + host, InetAddress.getByName(inbound.getAddress().getHostAddress()), 4243);
				}
				break;
			case "GO SLEEP":
				timer.stop();
				System.out.println("GOING TO SLEEP...");
				try 
				{
					systemSleep();
				} 
				catch (IOException e) 
				{
					System.out.println(e.getMessage());
				}
				break;
			case "ADD ME":
				if(ipInAddressMap(inbound.getAddress().getHostAddress()))
				{
					//Update port
					AddressMap am = searchMapping(inbound.getAddress().getHostAddress());
					am.setPort(inbound.getPort());
				}
				else
				{
					//New Host has joined - add the're IP,port and alias to the table
					addressMaps.add(new AddressMap(inbound.getAddress().getHostAddress(),inbound.getPort(),"sy.srvr.host"+hostalias));
				}
				break;
			case "ACK":
				System.out.println("RECEIVED ACK...");
				//For now don't do anything.
				break;
			case "SHUTDOWN":
				timer.stop();
				System.out.println("GOING TO A DEEPER SLEEP...");
				return true;
		}
		return false;
	}
	
	private static void systemSleep() throws IOException
	{
		boolean listen = true;
		byte[] buffer = new byte[2048];
		DatagramPacket inbound = new DatagramPacket(buffer, buffer.length);
		System.out.println("SLEEPING...");
		while(listen)
		{
			server.receive(inbound);
			String cmd = new String(inbound.getData(),0,inbound.getLength());
			if(cmd.equals("WAKE UP"))
			{
				timer.start();
				listen = false;//Wake Up
			}
		}
	}
	
	private static void initialiseMessageQueueHandler(int interval)
	{
		System.out.println("HANDLING MESSAGE QUEUE...");
		timer = new javax.swing.Timer(interval,new ActionListener() 
		{
			
			@Override
			public void actionPerformed(ActionEvent a) 
			{
				try 
				{
					sendMessages();
				} 
				catch (UnknownHostException e) 
				{
					System.err.println(e.getMessage());
				}
			}
		});
		timer.start();
	}
	
	private static void sendMessages() throws UnknownHostException
	{
		try
		{
			if(!messageQueue.isEmpty())
			{
				Iterator<Message> i = messageQueue.iterator();
				for(Message m:messageQueue)
				{
					if(i.hasNext())
					{
						i.next();
						int port = searchMapping(m.getDestination()).getPort();
						String ip = m.getDestination();
						sendPacket(m.getCode() + "	" + m.getMessage(),InetAddress.getByName(ip),port);
						i.remove();
					}
				}
			}
			else
			{
				if(debugging)System.out.println("MESSAGE QUEUE IS EMPTY :)");
			}
		}
		catch(ConcurrentModificationException cme)
		{
			System.err.println(cme.getMessage());
		}
	}
	
	private static AddressMap searchMapping(String ip)
	{
		for(AddressMap am:addressMaps)
		{
			if(am.getIP().equals(ip))
			{
				return am;
			}
		}
		return null;
	}
	
	private static boolean ipInAddressMap(String ip)
	{
		for(AddressMap a:addressMaps)
		{
			if(a.getIP().equals(ip))
			{
				return true;
			}
		}
		return false;
	}
	
	/*private static boolean aliasInAddressMap(String alias)
	{
		for(AddressMap a:addressMaps)
		{
			if(a.getAlias().equals(alias))
			{
				return true;
			}
		}
		return false;
	}*/
	
	private static String getAliasIP(String alias)
	{
		for(AddressMap a:addressMaps)
		{
			if(a.getAlias().equals(alias))
			{
				return a.getIP();
			}
		}
		return "";
	}
	
	private static void sendPacket(String msg,InetAddress dest_ip,int dest_port)
	{
		
		DatagramPacket outbound;
		try 
		{
			outbound = new DatagramPacket(msg.getBytes(), msg.getBytes().length,dest_ip,dest_port);
			server.send(outbound);
			System.out.println("SENT PACKET TO: " + outbound.getAddress() + ":" + outbound.getPort());
		} 
		catch (UnknownHostException e) 
		{
			System.err.println(e.getMessage());
		}
		catch (IOException e) 
		{
			System.err.println(e.getMessage());
		}
	}
}
