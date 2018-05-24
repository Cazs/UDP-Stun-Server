import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.speech.Central;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;
import javax.swing.JOptionPane;


public class Client 
{
	private static DatagramSocket client				= null;
	private static boolean debugging					= false;
	private static boolean listening					= false;
	
	public Client(String modeName)
	{
		try 
		{
			client = new DatagramSocket(4243);
			System.out.println("Datagram Socket initialised.");
			sendPacket("ADD ME");
		} 
		catch (SocketException e) 
		{
			System.err.println(e.getMessage());
			System.err.println("Buh-Bye!");
			System.exit(0);
		}
		//
		/* Create a template that tells JSAPI what kind of speech
         * synthesizer we are interested in.  In this case, we're
         * just looking for a general domain synthesizer for US
         * English.
         */ 
        /*SynthesizerModeDesc required = new SynthesizerModeDesc(
            null,      // engine name
            modeName,  // mode name
            Locale.US, // locale
            null,      // running
            null);     // voices

        /* Contact the primary entry point for JSAPI, which is
         * the Central class, to discover what synthesizers are
         * available that match the template we defined above.
         *
        EngineList engineList = Central.availableSynthesizers(required);
        for (int i = 0; i < engineList.size(); i++) 
        {
            
            SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList.get(i);
            System.out.println("    " + desc.getEngineName()
                               + " (mode=" + desc.getModeName()
                               + ", locale=" + desc.getLocale() + "):");*
            Voice[] voices = desc.getVoices();
            for (int j = 0; j < voices.length; j++) {
                System.out.println("        " + voices[j].getName());
            }
        }*/
		
	}
	
	/**
     * Returns a "no synthesizer" message, and asks 
     * the user to check if the "speech.properties" file is
     * at <code>user.home</code> or <code>java.home/lib</code>.
     *
     * @return a no synthesizer message
     */
     private static String noSynthesizerMessage() 
     {
        String message =
            "No synthesizer created.  This may be the result of any\n" +
            "number of problems.  It's typically due to a missing\n" +
            "\"speech.properties\" file that should be at either of\n" +
            "these locations: \n\n";
        message += "user.home    : " + System.getProperty("user.home") + "\n";
        message += "java.home/lib: " + System.getProperty("java.home") +
          File.separator + "lib\n\n" +
            "Another cause of this problem might be corrupt or missing\n" +
            "voice jar files in the freetts lib directory.  This problem\n" +
            "also sometimes arises when the freetts.jar file is corrupt\n" +
            "or missing.  Sorry about that.  Please check for these\n" +
            "various conditions and then try again.\n";
        return message;
    }
	
	public static void main(final String[] args) 
	{
		new Client("general");
		
		Thread tListen = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				try 
				{
					listen(args);
				} 
				catch (IOException e) 
				{
					System.err.println(e.getMessage());
				}
			}
		});
		
		tListen.start();
		
		String cmd = "";
		while(!cmd.equals("QUIT_ME"))
		{
			if(listening)
			{
				//System.out.println("\nType QUIT_ME & press return to quit:");
				//System.out.println("Type message to be sent to a host:");
				cmd = JOptionPane.showInputDialog("Type QUIT_ME & press return to quit;\nType message to be sent to a host:");
				//Scanner in = new Scanner(System.in);
				
				//cmd = in.nextLine();
				String msg = cmd;
				//in.close();
				//System.out.println("Type a host number to send message to:");
				//in = new Scanner(System.in);
				//cmd = in.nextLine();
				cmd = JOptionPane.showInputDialog("Type a host number to send message to:");
				String host = cmd;
				
				System.out.println("\nSending:\n" + msg+" >> "+host+"\n");
				sendPacket("SEND TO	" + msg + "	" + host);
			}
		}
	}
	
	private static void processPacket(DatagramPacket inbound,String[] args)
	{
		String line = new String(inbound.getData(),0,inbound.getLength());
		StringTokenizer tokenizer = new StringTokenizer(line,"	");
		String cmd = tokenizer.nextToken();
		switch(cmd)
		{
			case "MESSAGE INBOUND":
				String msg = tokenizer.nextToken();
				System.out.println(">" + msg);
				synthesize(msg,args);
				break;
			case "ACK":
				if(debugging)System.out.println("ACK RECEIVED FROM: " + inbound.getAddress()+":"+inbound.getPort());
				break;
			case "ERROR":
				System.err.println("ERROR: " + tokenizer.nextToken());
				break;
			default:
				System.out.println("Unkown Code.");
				break;
		}
	}
	
	private static void synthesize(String msg,String[] args)
	{
		String voiceName = (args.length > 0)
	            ? args[0]
	            : "kevin16";
	        
	        /*System.out.println();
	        System.out.println("Using voice: " + voiceName);*/
	        
	      try 
	      {
	          /* Find a synthesizer that has the general domain voice
	             * we are looking for.  NOTE:  this uses the Central class
	             * of JSAPI to find a Synthesizer.  The Central class
	             * expects to find a speech.properties file in user.home
	             * or java.home/lib.
	             *
	             * If your situation doesn't allow you to set up a
	             * speech.properties file, you can circumvent the Central
	             * class and do a very non-JSAPI thing by talking to
	             * FreeTTSEngineCentral directly.  See the WebStartClock
	             * demo for an example of how to do this.
	             */
	          SynthesizerModeDesc desc = new SynthesizerModeDesc(
	                null,          // engine name
	                "general",     // mode name
	                Locale.US,     // locale
	                null,          // running
	                null);         // voice
	          Synthesizer synthesizer = Central.createSynthesizer(desc);

	            /* Just an informational message to guide users that didn't
	             * set up their speech.properties file. 
	             */
	          if (synthesizer == null) {
	            System.err.println(noSynthesizerMessage());
	            System.exit(1);
	          }

	          /* Get the synthesizer ready to speak
	             */
	          synthesizer.allocate();
	          synthesizer.resume();

	            /* Choose the voice.
	             */
	            desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
	            Voice[] voices = desc.getVoices();
	            Voice voice = null;
	            for (int i = 0; i < voices.length; i++) {
	                if (voices[i].getName().equals(voiceName)) {
	                    voice = voices[i];
	                    break;
	                }
	            }
	            if (voice == null) {
	                System.err.println(
	                    "Synthesizer does not have a voice named "
	                    + voiceName + ".");
	                System.exit(1);
	            }
	            synthesizer.getSynthesizerProperties().setVoice(voice);

	           /* The the synthesizer to speak and wait for it to
	             * complete.
	             */
	          synthesizer.speakPlainText(msg, null);
	          synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
	          
	           /* Clean up and leave.
	             */
	          synthesizer.deallocate();
	            System.exit(0);
	            
	      } 
	      catch (Exception e) 
	      {
	          e.printStackTrace();
	      }
	}
	
	private static void listen(String[] args) throws IOException
	{
		boolean listen = true;
		byte[] buffer = new byte[2048];
		DatagramPacket inbound = new DatagramPacket(buffer, buffer.length);
		
		System.out.println("LISTENING...");
		
		while(listen)
		{
			listening = true;
			client.receive(inbound);
			//sendPacket("ACK");//ACK
			if(debugging)System.out.println("RECEIVED REQ FROM: " + inbound.getAddress().getHostAddress() + ":" + inbound.getPort());
			
			processPacket(inbound,args);
		}
	}
	
	private static void sendPacket(String msg)
	{
		DatagramPacket outbound;
		try 
		{
			outbound = new DatagramPacket(msg.getBytes(), msg.getBytes().length,InetAddress.getByName("104.236.97.104"),4242);
			client.send(outbound);
			System.out.println("SENT PACKET TO: " + outbound.getAddress().getHostAddress() + ":" + outbound.getPort());
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
