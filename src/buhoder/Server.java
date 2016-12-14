package buhoder;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Арсений on 12.12.2016.
 */

public class Server {

    public static void main(String[] args) {new Server();}

    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());
    private ServerSocket server;

    public Server() {
        try {
            server = new ServerSocket(11675, 0, InetAddress.getByName("127.0.0.1"));
            int clients = 0;
            System.out.println("Server is started");

            while (true) {
                Socket socket = server.accept();

                Connection connection = new Connection(socket, clients);
                connections.add(connection);
                ++clients;

                connection.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeAll();
        }
    }

    private void closeAll() {
        try {
            server.close();
            synchronized(connections) {
                for (Connection connection : connections)
                    connection.close();
            }
        } catch (Exception e) {
            System.err.println("Потоки не были закрыты!");
        }
    }

    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;
        private int number;

        private String name = "";

        private Connection(Socket socket, int number) {
            this.socket = socket;
            this.number = number;

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }

        @Override
        public void run() {
            String line, time;
            try {
                name = "Client" + number;
                line = GetLogs();
                if (line != null) this.out.println(line.replace("\n", "////n"));
                synchronized(connections) {
                    time = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss").format(Calendar.getInstance().getTime());
                    for (Connection connection : connections)
                        connection.out.println("[" + time + "] " + name + " comes now////n");
                }

                while (true) {
                    line = in.readLine().replace("////n", "\n");
                    time = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss").format(Calendar.getInstance().getTime());
                    if (line.equals(".") || line.equals("..") || line.equals("...")) {
                        line = "Client" + number + ": " + line + "\n";
                    } else {
                        line = "[" + time + "] " + name + ": " + line + "\n";
                        SaveLogs(line);
                    }
                    line = line.replace("\n", "////n");

                    synchronized(connections) {
                        System.out.println("Sending to clients: " + line);
                        for (Connection connection : connections)
                            connection.out.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                time = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss").format(Calendar.getInstance().getTime());
                System.out.println("[" + time + "] " + name + " has left");
                synchronized(connections) {
                    for (Connection connection : connections)
                        connection.out.println("[" + time + "] " + name + " has left");
                }
            } finally {
                close();
            }
        }

        void close() {
            try {
                in.close();
                out.close();
                socket.close();
                connections.remove(this);

                if (connections.size() == 0) {
                    Server.this.closeAll();
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println("Потоки не были закрыты!");
            }
        }

        private void SaveLogs(String newText){
            File file = new File("Server_logs.txt");
            String text = GetLogs() + newText;

            try {
                if(!file.exists()) file.createNewFile();

                try (PrintWriter out = new PrintWriter(file.getAbsoluteFile())) {
                    out.print(text);
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String GetLogs(){
            File file = new File("Server_logs.txt");
            if(!file.exists()) return null;

            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader in = new BufferedReader(new FileReader(file.getAbsoluteFile()));

                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
                in.close();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }

            return sb.toString();
        }
    }
}
