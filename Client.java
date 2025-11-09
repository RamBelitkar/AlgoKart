import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private Socket  socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String password;
    public Client(Socket socket,String username,String password){
        try{
            this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
            this.password = password;
        }catch(Exception e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    public void sendMessage(){
        try{
            bufferedWriter.write("LOGIN " + username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.write("PASS " + password);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String serverResponse = bufferedReader.readLine();
            if (serverResponse == null ) {
                System.out.println("No response from server.");
                closeEverything(socket, bufferedReader, bufferedWriter);
                return;
            }
            if (serverResponse.startsWith("ERR")) {
                System.out.println("Login failed: " + serverResponse);
                closeEverything(socket, bufferedReader, bufferedWriter);
                return;
            } else if (serverResponse.equals("OK")) {
                System.out.println("Logged in successfully as " + username);
            } else {
                System.out.println("Unexpected response: " + serverResponse);
            }

            System.out.println("Logged in as " + username);
            ListenForMessage();
            Scanner sc = new Scanner(System.in);
            while(socket.isConnected()){
                String message = sc.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Disconnecting...");
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
                if (message.equalsIgnoreCase("WHO")) {
                    bufferedWriter.write("WHO");
                } else {
                    bufferedWriter.write("MSG: " + message);
                }
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch(Exception e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    public void ListenForMessage(){
        new Thread(new Runnable(){
            @Override
            public  void run(){
                String msgFromClient;
                while(socket.isConnected()){
                    try{
                        msgFromClient = bufferedReader.readLine();
                        System.out.println(msgFromClient);
                    }catch(Exception e){
                        closeEverything(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        }).start();
    }
    public void closeEverything(Socket socket,BufferedReader br ,BufferedWriter bw){
        try{
            if(br!=null)br.close();
            if(bw!=null)bw.close();
            if(socket != null)socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your username for the chat:");
        String username = sc.nextLine();
        System.out.println("Enter your password for the chat:");
        String password = sc.nextLine();
        try{
            Socket socket = new Socket("localhost", 4000);
            Client client = new Client(socket,username,password);
            client.sendMessage();
        }catch(Exception e){
            System.out.println("Unable to connect to server.");
            e.printStackTrace();
        }
    }
}
