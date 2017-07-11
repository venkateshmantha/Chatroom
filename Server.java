import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.ServerSocket;

public class Server {

	// The server socket
	private static ServerSocket serverSocket = null;
	// The client socket
	private static Socket clientSocket = null;

	private static final int maxClientsCount = 1024;
	private static final clientThread[] threads = new clientThread[maxClientsCount];

	public static void main(String args[]) {

		// The default port number
		int portNumber = 2222;
		if (args.length < 1) {
			System.out.println("Server started on port " + portNumber);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			System.out.println("Server started on port " + portNumber);
		}

		/*
		 * Open a server socket on the portNumber (default 2222)
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a client socket for each connection and pass it on to a new client thread
		 */
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						(threads[i] = new clientThread(clientSocket, threads)).start();
						break;
					}
				}
				if (i == maxClientsCount) {
					ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
					os.writeObject("Server too busy, try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room. And as long as it receives data, echos that data back to all
 * other clients. When a client leaves the chat room, this thread informs all
 * the clients about the event and terminates.
 */

class clientThread extends Thread {

	private String clientName = null;
	private ObjectInputStream is = null;
	private ObjectOutputStream os = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int maxClientsCount;

	public clientThread(Socket clientSocket, clientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
	}

	public void run() {
		int maxClientsCount = this.maxClientsCount;
		clientThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client
			 */
			is = new ObjectInputStream(clientSocket.getInputStream());
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			String name;
			while (true) {
				os.writeObject("Enter the desired client name");
				try {
					name = (String)is.readObject();
					if (name.indexOf('@') == -1) {
						break;
					} else {
						os.writeObject("The name cannot contain the character '@'");
					}
				} catch (ClassNotFoundException e) {

					e.printStackTrace();
				}

			}

			System.out.println("Client " + name + " has joined the chatroom");
			os.writeObject("Welcome to the chat room " + name + ". To leave, enter </quit> in a new line.");
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] == this) {
						clientName = "@" + name;
						break;
					}
				}
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].os.writeObject("A new user " + name + " has joined the chat room");
					}
				}
			}

			while (true) {
				String line;
				try {

					/*
					 * Reading the user command
					 */

					line = (String) is.readObject();

					// Quit if user types /quit
					if (line.startsWith("/quit"))
					{
						break;
					}

					/*
					 * Handle file transfers
					 */

					else if (line.startsWith("file"))
					{
						String[] words = line.split("\\s", 4);

						if(words[1].equals("broadcast"))
						{

							if (words.length > 1 && words[2] != null)
							{
								words[2] = words[2].trim();
								if (!words[2].isEmpty())
								{

									String filepath = words[2];
									File file = new File(filepath);
									byte[] buffer = new byte[(int) file.length()];
									FileInputStream fis = new FileInputStream(file);
									BufferedInputStream in = new BufferedInputStream(fis);
									System.out.println("File broadcasted by " + name);

									in.read(buffer,0,buffer.length);

									synchronized (this)
									{
										for (int i = 0; i < maxClientsCount; i++)
										{
											if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
												threads[i].os.writeObject("FILE INCOMING");
												threads[i].os.writeObject(threads[i].clientName.substring(1));
												threads[i].os.writeObject(filepath.substring(filepath.lastIndexOf('/') + 1));
												threads[i].os.write(buffer, 0, buffer.length);
												threads[i].os.flush();
											}
										}
									}
								}
							}

						}

						else if(words[1].equals("unicast"))
						{

							if (words.length > 1 && words[3] != null)
							{
								words[3] = words[3].trim();
								if (!words[3].isEmpty())
								{

									String filepath = words[3];
									File file = new File(filepath);
									byte[] buffer = new byte[(int) file.length()];
									FileInputStream fis = new FileInputStream(file);
									BufferedInputStream in = new BufferedInputStream(fis);
									System.out.println("File unicast by " + name +" to " + words[2].substring(1));

									in.read(buffer,0,buffer.length);

									synchronized (this)
									{
										for (int i = 0; i < maxClientsCount; i++)
										{
											if (threads[i] != null && threads[i] != this
													&& threads[i].clientName != null
													&& threads[i].clientName.equals(words[2])) {
												threads[i].os.writeObject("FILE INCOMING");
												threads[i].os.writeObject(threads[i].clientName.substring(1));
												threads[i].os.writeObject(filepath.substring(filepath.lastIndexOf('/') + 1));
												threads[i].os.write(buffer, 0, buffer.length);
												threads[i].os.flush();
											}
										}
									}
								}
							}

						}

					}

					/*
					 * Handle message transfers
					 */

					else if (line.startsWith("unicast"))
					{
						String[] words = line.split("\\s", 3);
						if (words.length > 1 && words[2] != null)
						{
							words[2] = words[2].trim();
							System.out.println("Message unicast by " + name + " to " + words[1].substring(1) );
							if (!words[2].isEmpty())
							{
								synchronized (this)
								{
									for (int i = 0; i < maxClientsCount; i++)
									{
										if (threads[i] != null && threads[i] != this
												&& threads[i].clientName != null
												&& threads[i].clientName.equals(words[1]))
										{
											threads[i].os.writeObject("<<" + name + ">> " + words[2]);
											/*
											 * Echo this message to let the client know the private
											 * message was sent
											 */
											this.os.writeObject("Private message sent to " + words[1].substring(1));
											break;
										}
									}
								}
							}
						}
					}

					else if (line.startsWith("blockcast"))
					{
						String[] words = line.split("\\s", 3);
						if (words.length > 1 && words[2] != null)
						{
							words[2] = words[2].trim();
							System.out.println("Message blockcasted by " + name + " excluding " + words[1].substring(1) );
							if (!words[2].isEmpty())
							{
								synchronized (this)
								{
									for (int i = 0; i < maxClientsCount; i++)
									{
										if (threads[i] != null && threads[i] != this
												&& threads[i].clientName != null
												&& !threads[i].clientName.equals(words[1]))
										{
											threads[i].os.writeObject("<" + name + "> " + words[2]);

										}
									}
								}
							}
						}
					}

					else if (line.startsWith("broadcast"))
					{
						String[] words = line.split("\\s", 2);
						if (words.length > 1 && words[1] != null)
						{
							words[1] = words[1].trim();
							System.out.println("Message broadcasted by " + name);
							if (!words[1].isEmpty())
							{
								synchronized (this)
								{
									for (int i = 0; i < maxClientsCount; i++)
									{
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
											threads[i].os.writeObject("<" + name + "> " + words[1]);
										}
									}
								}
							}
						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this
							&& threads[i].clientName != null) {
						threads[i].os.writeObject("The user " + name
								+ " has left the chatroom");
					}
				}
			}
			os.writeObject("~ Bye " + name);
			System.out.println(name + " has left the chatroom");

			/*
			 * Clean up. Set the current thread variable to null so that a new client
			 * could be accepted by the server.
			 */
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, input stream and the socket
			 */
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}
}

