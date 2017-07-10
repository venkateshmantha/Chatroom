import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

  private static Socket clientSocket = null;
  private static ObjectOutputStream os = null;
  private static ObjectInputStream is = null;
  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args) {

    // The default port.
    int portNumber = 2222;
    // The default host.
    String host = "localhost";

    if (args.length < 2) {
      System.out.println("Client connected to " + host + " on portNumber " + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try {
	      clientSocket = new Socket(host, portNumber);
	      inputLine = new BufferedReader(new InputStreamReader(System.in));
	      os = new ObjectOutputStream(clientSocket.getOutputStream());
	      is = new ObjectInputStream(clientSocket.getInputStream());
	    } catch (UnknownHostException e) {
	      System.err.println("Don't know about host " + host);
	    } catch (IOException e) {
	      System.err.println("Couldn't get I/O for the connection to the host "
	          + host);
	    }

    if (clientSocket != null && os != null && is != null) {
      try {
    	  	new Thread(new Client()).start();
	        while (!closed) {
	        	System.out.println("Please enter your command in the next line");
	        	os.writeObject(inputLine.readLine().trim());
	        }
	        
        /*
         * Close the output stream, the input stream and the socket.
         */
	        os.close();
	        is.close();
	        clientSocket.close();
	      } catch (IOException e) {
	        System.err.println("IOException:  " + e);
	      }
    }
  }

  /*
   * Create a thread to read from the server
   */
  
  public void run() {
    /*
     * Keep on reading from the socket until we receive "Bye" from the
     * server
     */
    String responseLine;
    try {
      while ((responseLine = (String)is.readObject()) != null) {
    	  
    	  if(responseLine.equals("FILE INCOMING")){
    		  String cname = (String)is.readObject();
    		  String filename = (String)is.readObject();
    		  
    		  int test = is.read();
    		  	while(test!=-1)
    		  	{

	    		  	int size = 8096;
	    		  	int byteread;
	    		  	
	    		    byte[] buffer = new byte[size];
	    		    File f = new File(cname + "/" +filename);
	    		    if(!f.getParentFile().exists()){
	    		        f.getParentFile().mkdirs();
	    		    }
	    		    
	    		    try {
	    		        	f.createNewFile();
	    		        } catch (Exception e) {
	    		        	e.printStackTrace();
	    		        }
	    		    
	    		    File file = new File(f.getParentFile(), f.getName());
	    		    System.out.println("The file " + filename + " has been received");
	    		    try {
		    		    FileOutputStream fos = new FileOutputStream(file);
		    	        BufferedOutputStream out = new BufferedOutputStream(fos);
		    	        while ((byteread = is.read(buffer, 0, buffer.length)) != -1) {
		    	        	  out.write(buffer, 0, byteread);
		    	        	  out.flush();
		    	        	}
		    	        
		    	        fos.close();
		    	        out.close();
		    	        
		    	        break;
	    	        } catch(Exception e) {
		            	System.out.println("Exception: " +e);
		            }
	    		    
	    		    
	    	  }
    	  }
    	  
    	  else {
	        System.out.println(responseLine);
	        if (responseLine.indexOf("~ Bye") != -1)
	          break;
    	  }
      }
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
  }
}
