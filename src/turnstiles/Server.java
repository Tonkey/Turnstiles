package turnstiles;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private static String ip = "localhost";
    private static int port = 1337;
    
    List<ClientHandler> clients = new ArrayList();
    List<ClientHandler> turnstiles = new ArrayList();
    List<ClientHandler> monitors = new ArrayList();
    
    private int SpectatorNum = 0;
    
    public static void stopServer() {
        keepRunning = false;
    }
    
    public void send(String msg) {
        for (ClientHandler client : clients) {
            client.send(msg);
        }
    }
    
    public synchronized void incrementSpectatorNum() {
        SpectatorNum++;
    }
    
    public int getSpectatorNum() {
        return SpectatorNum;
    }
    
    public void registerType(ClientHandler ch) {
        String infoMsg = "Client: " + ch.getClientId() + " of Type: " + ch.getType() + " registered in appropiate client list";
        
        
        switch (ch.getType()) {
            case ProtocolStrings.TURNSTILE:
                this.turnstiles.add(ch);
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, infoMsg);
                break;
            case ProtocolStrings.MONITOR:
                this.monitors.add(ch);
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, infoMsg);
                break;
            default:
                Logger.getLogger(Log.LOG_NAME).log(Level.WARNING, "Type not registered on client: " + ch.getClientId());
        }
    }
    
    public void removeHandler(ClientHandler ch) {
        clients.remove(ch);
        
        switch(ch.getType()){
            case ProtocolStrings.TURNSTILE:
                turnstiles.remove(ch);
                break;
            case ProtocolStrings.MONITOR:
                monitors.remove(ch);
                break;
        }
        
        String msg1 = "Client: " + ch.getName() + " disconnected";
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, msg1);
        String msg2 = "Remaining amount of clients connected: " + clients.size();
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, msg2);
    }
    
    private void runServer() {
        System.out.println("inside runServer");
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Starting the Server");
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Server started. Listening on: " + port + ", bound to: " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", 1337));
            do {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket, this);
                
                clients.add(client);
                
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Current amount of clients connected: " + clients.size());
                
                client.start();
                
            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(Log.LOG_NAME).log(Level.SEVERE, null, ex);
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        try {
            Log.setLogFile("logFile.txt", "ServerLog");
            new Server().runServer();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            Log.closeLogger();
        }
    }
}
