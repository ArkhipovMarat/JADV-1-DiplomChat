package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Server server;
    private PrintWriter out;
    private Scanner in;
    private Socket clientSocket = null;
    private static int clients_count = 0;
    private String clientName = "";

    public ClientHandler(Socket socket, Server server) {
        try {
            clients_count++;
            this.server = server;
            this.clientSocket = socket;
            this.out = new PrintWriter(socket.getOutputStream());
            this.in = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (in.hasNext()) {
                    clientName = in.nextLine();
                    Thread.currentThread().setName(clientName);
                    String message = server.messageBuilder("Новый участник вошел в чат",
                            Thread.currentThread().getName());
                    server.sendMessageToAllClients(message);
                    server.writeToLogFile(message);
                    server.sendMessageToAllClients("Клиентов в чате = " + clients_count);
                    break;
                }
            }

            while (true) {
                if (in.hasNext()) {
                    String clientMessage = in.nextLine();
                    if (clientMessage.equalsIgnoreCase("exit")) {
                        String message = server.messageBuilder("Участник покидает чат",
                                Thread.currentThread().getName());
                        server.sendMessageToAllClients(message);
                        server.writeToLogFile(message);
                        break;
                    }
                    String message = server.messageBuilder(clientMessage, Thread.currentThread().getName());
                    server.sendMessageToAllClients(message);
                    server.writeToLogFile(message);
                }
                Thread.sleep(100);
            }

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            this.close();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.println(msg);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        server.removeClient(this);
        clients_count--;
        server.sendMessageToAllClients("Клиентов в чате = " + clients_count);
    }
}
