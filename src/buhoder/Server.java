package buhoder;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by Арсений on 12.12.2016.
 */

public class Server {

    /**
     * Серверная часть программы, "соединяет" пользователей
     */

    public static void main(String[] args) {
        new Server();
    }

    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());
    private ServerSocket server;

    public Server() {

        /**
         * Создаёт список клиентов, связывается с каждым
         */

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

        /**
         * Закрывает как и потоки связи с пользователями, так и сам сервер
         */

        try {
            server.close();
            synchronized(connections) {
                for (Connection connection : connections) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Threads worn't closed!");
        }
    }

    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        private String name = "";

        private Connection(Socket socket, int number) {

            /**
             * Создаёт поток соединения с клиентом
             */

            this.socket = socket;
            name = "Client" + number;

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
            try {

                /**
                 * Объявляет всем пользователям о появлении нового клиента,
                 * отправляет ему уже существующую переписку
                 */

                Message hello = new Message(name);
                hello.sayHello();
                if (hello.msg != null) this.out.println(hello.msg);
                synchronized(connections) {
                    for (Connection connection : connections) {
                        connection.out.println(Message.helloMessage);
                    }
                }

                /**
                 * Принимает сообщения от нового клиента и отправляет
                 * их всем с обработкой (добавляеи имя и время)
                 */

                while (true) {
                    Message message = new Message(name);
                    message.msg = in.readLine();
                    message.convertToGet();
                    message.formatMessage();
                    message.convertToSend();

                    synchronized(connections) {
                        System.out.println("Sending to clients: " + message.msg);
                        for (Connection connection : connections) {
                            connection.out.println(message.msg);
                        }
                    }
                }
            } catch (IOException e) {

                /**
                 * Сообщает всем, что клиент закрыл программу и
                 * в конце закрывает поток пользователя
                 */

                e.printStackTrace();
                Message bye = new Message(name);
                bye.sayGoodbye();
                synchronized(connections) {
                    for (Connection connection : connections) {
                        connection.out.println(Message.byeMessage);
                    }
                }
            }
            finally {
                close();
            }
        }

        void close() {

            /**
             * Закрывает серверные потоки
             */

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
                System.err.println("Threads worn't closed!");
            }
        }

    }
}
