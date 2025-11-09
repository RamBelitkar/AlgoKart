
import java.net.*;
import java.io.*;


public class Server{
    private ServerSocket serverSocket;
    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }
    public void startServer(){
        try{
            System.out.println("Server started on port " + serverSocket.getLocalPort());
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                
                System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread  = new Thread(clientHandler);
                thread.start();

            }
        }catch(Exception e){
            closeServerSocket();
        }
    }
    public void closeServerSocket(){
        try{
            if (serverSocket != null){
                serverSocket.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(4000);
        Server myServer = new Server(server);
        myServer.startServer();
        
    }
}