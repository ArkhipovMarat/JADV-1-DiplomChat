package server;

public class MainServer {
    public static void main(String[] args) {
       Server server = new Server("./src/settings/settings");
       server.listen();
    }
}