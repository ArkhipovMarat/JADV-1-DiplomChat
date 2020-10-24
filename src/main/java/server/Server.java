package server;

import server.entity.Settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Server {
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private LocalDateTime now;
    private int port;
    private String logfilePath;

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private FileWriter fileWriter;

    private final ArrayList<ClientHandler> clients = new ArrayList<>();

    public Server(String settingsPath) {
        try {
            serverInitialization(settingsPath);

            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Сервер остановлен");
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessageToAllClients(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    private boolean readSettings(String path) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            Settings settings = gson.fromJson(new FileReader(path), Settings.class);
            if (settings != null) {
                String portFromJson = settings.getPort();
                String logFileFromJson = settings.getLogFilePath();
                if (!("".equals(portFromJson)) & !("".equals(logFileFromJson))) {
                    port = Integer.parseInt(portFromJson);
                    logfilePath = logFileFromJson;
                    return true;
                }
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String messageBuilder(String message, String clientName) {
        now = LocalDateTime.now();
        String msg = clientName + ": " + message + ", " + dtf.format(now);
        return msg;
    }

    private void serverInitialization(String settingsPath) throws IOException {
        if (!readSettings(settingsPath)) {
            System.out.println("Сервер не запущен!");
            System.out.println("ошибка в файле settings");
            System.exit(1);
        } else {
            serverSocket = new ServerSocket(port);
            fileWriter = new FileWriter(logfilePath, true);
            System.out.println("Сервер запущен!");
        }
    }

    public void writeToLogFile(String message) {
        try {
            fileWriter.write(message);
            fileWriter.append('\n');
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
