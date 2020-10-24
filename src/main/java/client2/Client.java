package client2;

import client2.entity.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private Scanner scanner;
    FileWriter fileWriter;

    private int port;
    private String ip;
    private String logfilePath;

    public Client(String settingsPath) {
        try {
            clientInitialization(settingsPath);

            System.out.println("Введите ваше имя: ");
            out.println(scanner.nextLine());

            Resender resend = new Resender();
            resend.start();

            String message = "";

            while (!message.equals("exit")) {
                message = scanner.nextLine();
                out.println(message);
            }

            resend.setStop();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean readSettings(String settingsPath) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            Settings settings = gson.fromJson(new FileReader(settingsPath), Settings.class);
            if (settings != null) {
                String portFromJson = settings.getPort();
                String hostFromJson = settings.getHost();
                String logFilePathFromJson = settings.getLogFilePath();

                if (!("".equals(portFromJson)) & !("".equals(hostFromJson)) & !("".equals(logFilePathFromJson))) {
                    port = Integer.parseInt(portFromJson);
                    ip = hostFromJson;
                    logfilePath = logFilePathFromJson;
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

    private void clientInitialization(String settingsPath) throws IOException {
        if (!readSettings(settingsPath)) {
            System.out.println("Клиент не запущен!");
            System.out.println("ошибка в файле settings");
            System.exit(1);
        } else {
            scanner = new Scanner(System.in);
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            fileWriter = new FileWriter(logfilePath, true);
            System.out.println("Клиент запущен!");
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

    private class Resender extends Thread {
        private boolean stoped;

        public void setStop() {
            stoped = true;
        }

        @Override
        public void run() {
            try {
                while (!stoped) {
                    String message = in.readLine();
                    writeToLogFile(message);
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

