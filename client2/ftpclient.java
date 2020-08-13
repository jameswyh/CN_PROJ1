import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.*;

public class ftpclient {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	boolean connection = false;
	boolean login = false; 

	public void Client() {}

	void run()
	{
		try{
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(!connection) {
				try {
				System.out.print("Please input ftp commands:");
				message = bufferedReader.readLine();
				String[] ftpSplited = message.split("\\s+");
				if(ftpSplited[0].equals("ftpclient")) {
					if (ftpSplited.length != 3) {
						System.out.println("Wrong Command!");
					}
					else if (ftpSplited.length == 3) {
						if (!ftpSplited[1].equals("localhost")) {
							System.err.println("You are trying to connect to an unknown host!");
						}
						else {
							requestSocket = new Socket(ftpSplited[1], Integer.parseInt(ftpSplited[2]));
							connection = true;
							System.out.println("Connected to " + ftpSplited[1] + " in port " + ftpSplited[2]);
						}
					}	
				}
				else {
					System.out.println("Please build the connection first!");
					}
				}
				catch (ConnectException e) {
	    			System.err.println("Connection refused. You need to initiate a server first.");
	    			} 
				catch (UnknownHostException unknownHost){
					System.err.println("You are trying to connect to an unknown host!");
					}
				catch (IOException ioException){
					ioException.printStackTrace();
				}	
			}

			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			String current_dir = new java.io.File(".").getCanonicalPath();
			File folder = new File(current_dir);
			File[] list_of_files;
			boolean flag = false;
			
			while(!login)
			{
				System.out.print("Please input username and password:");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				//Send the sentence to the server
				sendMessage(message);
				//Receive the upperCase sentence from the server
				MESSAGE = (String)in.readObject();
				//show the message to the user
				System.out.println(MESSAGE);
				if (MESSAGE.equals("Log in successfully!"))
					login = true;
			}
			while(true) {
				System.out.print("Please input ftp commands:");
				message = bufferedReader.readLine();
				//Send the sentence to the server
				String[] splited = message.split("\\s+");
				if(splited[0].equals("ftpclient")) {
					System.out.println("You cannot build another connection since you are already connected");
				}
				else if(splited[0].equals("get")) {
					if (splited.length != 2) {
						System.out.println("Wrong Command!");
					}
					else {
						sendMessage(message);
				        if (in.read() == 0)
				        	System.out.println("File does not exist in the server");
				        else {
				        	File file = new File("./" + splited[1]);
				        	byte[] content = (byte[]) in.readObject();
				        	Files.write(file.toPath(), content); 			
				        	System.out.println("Get " + splited[1] + " Successfully");    
				        }
					}
				}
				else if(splited[0].equals("upload")) {
					if (splited.length != 2) {
						System.out.println("Wrong Command!");
					}
					else {
						sendMessage(message);
						flag = false;
						MESSAGE = (String)in.readObject();
						if(MESSAGE.equals("Ready")) {
							list_of_files = folder.listFiles();						
							for(File file: list_of_files) {
								if (file.getName().equals(splited[1])) {
									System.out.println("File " + file.getName() + " found");
									flag = true;
									message = "found";
									sendMessage(message);
									sendFile(file);
								}
							}
							if (flag == false) {
								System.out.println("File not found");
								message = "notfound";
								sendMessage(message);
							}
						}	
					}
				}
				else if(message.equals("dir")){
					sendMessage(message);
					MESSAGE = (String)in.readObject();
					//show the message to the user
					System.out.println(MESSAGE);
				}
				else
					System.out.println("Undefined Command!");
			}
		}
		catch (ClassNotFoundException e ) {
			System.err.println("Class not found");
			} 
		catch(IOException ioException){
			ioException.printStackTrace();
			}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//send a file to the output stream
	public void sendFile(File file)
	{
		try{
			byte[] content = Files.readAllBytes(file.toPath());			
			out.writeObject(content);	        
			out.flush();        
			System.out.println("Upload " + file.getName() + " to Server Successfully");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//main method
	public static void main(String args[])
	{
		ftpclient client = new ftpclient();
		client.run();
	}
}
