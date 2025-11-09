

import java.net.*;
import java.io.*;  
import java.util.*;
public class ClientHandler implements Runnable {
    public static Vector<ClientHandler> clientHandlers = new Vector<>();
	private static Map<String, String> userCred = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
	private String password;
	public ClientHandler(Socket socket){
		try{
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String loginLine = bufferedReader.readLine();
            if (loginLine == null || !loginLine.startsWith("LOGIN ")) {
                bufferedWriter.write("ERR invalid-login");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                closeEverything(socket, bufferedReader, bufferedWriter);
                return;
            }
			this.username = loginLine.substring(6).trim();
			for (ClientHandler client : clientHandlers) {
                if (client.username.equalsIgnoreCase(this.username)) {
                    bufferedWriter.write("ERR username-taken");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    return;
                }
            }
			String passString =bufferedReader.readLine();
			if (passString==null || !passString.startsWith("PASS ")) {
				bufferedWriter.write("ERR invalid-password");
				bufferedWriter.newLine();
				bufferedWriter.flush();
				closeEverything(socket, bufferedReader, bufferedWriter);
				return;
			}
			this.password = passString.substring(5).trim();
			synchronized(userCred){
				if(userCred.containsKey(this.username)){
					String savedPass = userCred.get(this.username);
					if(!userCred.get(this.username).equals(this.password)){
						bufferedWriter.write("ERR wrong-password");
						bufferedWriter.newLine();
						bufferedWriter.flush();
						closeEverything(socket, bufferedReader, bufferedWriter);
						return;
					}
				} else {
					userCred.put(this.username, this.password);
				}
			}
			bufferedWriter.write("OK");
            bufferedWriter.newLine();
            bufferedWriter.flush();
			clientHandlers.add(this);
			broadCastMessage("INFO:" + this.username + " has entered into the chat");
		}catch(Exception e){
			closeEverything(socket,bufferedReader,bufferedWriter);
		}
	}
	@Override
	public void run(){
	String message;
	while(!socket.isClosed()){
		try{
			message = bufferedReader.readLine();
			if (message == null) {
				closeEverything(socket, bufferedReader, bufferedWriter);
				break;
			}
			if (message.startsWith("MSG: ")) {
				String text = message.substring(5).trim();
					if (text.startsWith("@")) {
						handlePrivateMessage(text);
						continue;
					}else broadCastMessage("MSG: " + username + ": " + text);
			} else if (message.equalsIgnoreCase("WHO")) {
				sendActiveUsers();
			} else {
				bufferedWriter.write("ERR unknown-command");
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		}catch(Exception e){
			closeEverything(socket,bufferedReader,bufferedWriter);
			break;
		}
	}
	}
	private void handlePrivateMessage(String message) {
        int firstSpaceIndex = message.indexOf(' ');
        if (firstSpaceIndex == -1) {
            sendResponse("ERR invalid-private-message");
            return;
        }

        String targetUser = message.substring(1, firstSpaceIndex).trim();
        String privateMsg = message.substring(firstSpaceIndex + 1).trim();

        boolean found = false;
        for (ClientHandler client : clientHandlers) {
            if (client.username.equalsIgnoreCase(targetUser)) {
                found = true;
                try {
                    client.bufferedWriter.write("[DM from " + username + "]: " + privateMsg);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                    sendResponse("[DM to " + targetUser + "]: " + privateMsg);
                } catch (IOException e) {
                    sendResponse("ERR failed-to-send");
                }
                break;
            }
        }

        if (!found) {
            sendResponse("ERR user-not-found");
        }
    }
	 private void sendResponse(String msg) {
        try {
            bufferedWriter.write(msg);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	public void broadCastMessage(String message){
		for (ClientHandler clientHandler:clientHandlers){
			try{
				if (!clientHandler.username.equals(username)){
					clientHandler.bufferedWriter.write(message);
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
				}
			}catch(Exception e){
				closeEverything(socket,bufferedReader,bufferedWriter);
			}
		}
	}

	public void broadCastInfo(String infoMessage) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(infoMessage);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
	private void sendActiveUsers() {
        try {
            for (ClientHandler client : clientHandlers) {
                bufferedWriter.write("USER: " + client.username);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
	public void removeClientHandler(){
		if (clientHandlers.contains(this)){
			clientHandlers.remove(this);
			broadCastMessage("SERVER: " +username + " has Left the Chat!!!");
		}
		
	}
	public void closeEverything(Socket socket,BufferedReader br ,BufferedWriter bw){
		removeClientHandler();
		try{
			if(br!=null)br.close();
			if(bw!=null)bw.close();
			if(socket != null)socket.close();
		}catch(Exception e){
			e.printStackTrace();
		} 
	}
}

