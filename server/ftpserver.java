import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.*;

public class ftpserver {

	private static final int sPort = 8888;   //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
        	try {
        		while(true) {
        			new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
        private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client
		
		private String[] username = new String[]{"a", "b", "c", "d", "e"};
		private String[] password = new String[]{"1", "2", "3", "4", "5"};
		
		private boolean login = false;

		public Handler(Socket connection, int no) {
			this.connection = connection;
	    	this.no = no;
	    	}

        public void run() {
 		try{
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(!login)
				{
					//receive the message sent from the client
					message = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + message + " from client " + no);
					String[] splited = message.split("\\s+");
					MESSAGE = "Wrong username or password! Please try again!";
					for (int i = 0; i < username.length; i++) {
						if (username[i].equals(splited[0]) && password[i].equals(splited[1])) {
							login = true;
							MESSAGE = "Log in successfully!";
							break;
						}
					}
					//send MESSAGE back to the client
					sendMessage(MESSAGE);
				}
				
				String current_dir = new java.io.File(".").getCanonicalPath();
				File folder = new File(current_dir);
				File[] list_of_files;
				boolean flag = false;
				
				while(true) {
					flag = false;
					//receive the message sent from the client
					message = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + message + " from client " + no);
					String list_of_filenames = "Files under current folder:";
					if(message.equals("dir")) {
						list_of_files = folder.listFiles();
						for(File file: list_of_files)
							list_of_filenames += "\n" + file.getName();
						MESSAGE = list_of_filenames;
						//send MESSAGE back to the client
						sendMessage(MESSAGE);
					}
					String[] splited = message.split("\\s+");
					if(splited[0].equals("get")) {						
						list_of_files = folder.listFiles();						
						for(File file: list_of_files) {
							if (file.getName().equals(splited[1])) {
								System.out.println("File " + file.getName() + " found");
								flag = true;
								sendFile(file);
							}
						}
						if (flag == false) {
							System.out.println("File not found");
							out.write(0);
				            out.flush();
						}
					}
					
					if(splited[0].equals("upload")) {	
						MESSAGE = "Ready";
						//send MESSAGE back to the client
						sendMessage(MESSAGE);
						message = (String)in.readObject();
						if (message.equals("found")) {
							byte[] content = (byte[]) in.readObject();
							File file = new File("./" + splited[1]);     	
					       	Files.write(file.toPath(), content); 			
					       	System.out.println("Receive " + splited[1] + " Successfully");    
						}
						else
							System.out.println("Fail to Receive " + splited[1]);
					}
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}

	//send a message to the output stream
        public void sendMessage(String msg)
        {
        	try{
        		out.writeObject(msg);	
        		out.flush();
        		System.out.println("Send message: " + msg + " to Client " + no);
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
		        System.out.println("Send file: " + file.getName() + " to Client " + no);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	
    }

}
