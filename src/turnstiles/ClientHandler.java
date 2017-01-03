package turnstiles;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nickl
 */
public class ClientHandler extends Thread {

    private String id = "default";

    private Scanner input;
    private PrintWriter writer;
    private Socket socket;

    private Server server;

    ClientHandler(Socket socket, Server server) {
        try {
            input = new Scanner(socket.getInputStream());
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {

        }
        this.socket = socket;
        this.server = server;

    }

    @Override
    public void run() {

        try {
            send("Welcome to the server");
            String message = input.nextLine(); //IMPORTANT blocking call
            while (!message.equals(ProtocolStrings.STOP)) {
                System.out.println(message);
                if (message.equals(ProtocolStrings.TURNSTILE)) {
                    send("Waiting for turnstile id");
                    message = input.nextLine();
                    System.out.println(message.substring(0, 6));
                    while (!message.substring(0, 6).equals(ProtocolStrings.ID)) {
                        message = input.nextLine();
                    }
                    while (id.equals("default")) {
                        if (checkTurnstileId(message.substring(6))) {
                            id = message.substring(6);
                            server.registerTurnstile(this);
                            send("Turnstile registered as: " + id);
                        } else {
                            send("Id already in use");
                            message = input.nextLine();
                        }
                    }
                    message = input.nextLine();
                }

                if (message.equals(ProtocolStrings.MONITOR)) {
                    send("Waiting for monitor id");
                    message = input.nextLine();
                    System.out.println(message.substring(0, 6));
                    while (!message.substring(0, 6).equals(ProtocolStrings.ID)) {
                        message = input.nextLine();
                    }
                    while (id.equals("default")) {
                        if (checkMonitorId(message.substring(6))) {
                            id = message.substring(6);
                            server.registerMonitor(this);
                            send("Monitor registered as: " + id);
                        } else {
                            send("Id already in use");
                            message = input.nextLine();
                        }
                    }
                    message = input.nextLine();
                }

                switch (message) {

                    case ProtocolStrings.INCREMENT:
                        server.incrementSpectatorNum();
                        break;
                    case ProtocolStrings.SPECTATORS:
                        send("Amount of spectators: " + server.getSpectatorNum());
                        break;
                    default:
                        send("Unknown Command");
                }

                message = input.nextLine(); //IMPORTANT blocking call
            }
            writer.println(ProtocolStrings.STOP);//Echo the stop message back to the client for a nice closedown

            writer.close();
            input.close();
            socket.close();
            server.removeHandler(this);
            System.out.println("Closed a Connection");
        } catch (IOException ex) {
            System.out.println("something went wrong while closing thread:" + this.getName());
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void send(String message) {
        writer.println(message);
    }

    public String getClientId() {
        return id;
    }

    public void setClientId(String id) {
        this.id = id;
    }

    private boolean checkTurnstileId(String id) {

        for (ClientHandler ch : server.turnstiles) {
            if (id.equals(ch.getClientId())) {
                return false;
            }
        }

        return true;
    }

    private boolean checkMonitorId(String id) {
        for (ClientHandler ch : server.monitors) {
            if (id.equals(ch.getClientId())) {
                return false;
            }
        }
        return true;
    }

}
