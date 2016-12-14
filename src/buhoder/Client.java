package buhoder;

import javafx.scene.input.KeyCode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

/**
 * Created by Арсений on 12.12.2016.
 */
public class Client {

    private JFrame swg = new JFrame("Buhoder");
    private javax.swing.JButton SendButton = new javax.swing.JButton("Send");
    private TextArea write_message_area = new TextArea();
    private TextArea chat_area = new TextArea();
    private BufferedReader in;
    private PrintWriter out;
    private boolean isWaiting = false;

    public static void main(String[] args) {new Client();}

    public Client() {

        SendButton.addActionListener( new listen_class());

        chat_area.setEditable(false);
        chat_area.setFocusable(false);
        swg.setSize(560, 720);
        swg.setMinimumSize(new Dimension(560, 720));
        swg.setMaximumSize(new Dimension(560, 720));
        swg.setResizable(false);
        SendButton.setSize(80, (int) write_message_area.getSize().getHeight());
        swg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        swg.setLayout(new java.awt.BorderLayout());

        JPanel South = new JPanel();
        South.add(write_message_area);
        South.add(SendButton);
        write_message_area.addKeyListener(new KeyListener() {
            short iter = 0;
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getSource() == KeyCode.ENTER) send_message(null);
                switch (iter % 3) {
                    case 0: send_message("."); break;
                    case 1: send_message(".."); break;
                    case 2: send_message("..."); break;
                }
                swg.setVisible(true);
                if (iter == 1000) iter = 0;
                else ++iter;
            }
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        swg.add(chat_area, BorderLayout.CENTER);
        swg.add(South, BorderLayout.SOUTH);
        swg.setVisible(true);

        connect();
    }

    private class listen_class implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == SendButton)
                send_message(null);
        }
    }

    private void add_message(String msg) {
        if (msg.startsWith("Client")) {
            delete_writingPoints();
            if(!isWaiting) {
                Wait_to_delete_writingPoint Wait = new Wait_to_delete_writingPoint();
                Wait.start();
                isWaiting = true;
            }
        }
        chat_area.append(msg);
        swg.setVisible(true);
    }

    private void send_message(String message) {
        if (message == null) message = write_message_area.getText();
        if (!Objects.equals(message, ""))
            out.println(message.replace("\n", "////n"));

        write_message_area.setText(null);
        swg.setVisible(true);
    }

    private void connect() {
        try {
            InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
            Socket socket = new Socket(ipAddress, 11675);

            in =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Refresh();
            getting_messages_class getting_messages = new getting_messages_class();
            getting_messages.start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private void Refresh() throws IOException {
        String logs = in.readLine();
        if (logs!=null) {
            chat_area.setText(logs.replace("////n", "\n"));
            swg.setVisible(true);
        }
    }

    private void delete_writingPoints() {
        String text = chat_area.getText();
        String[] chat = text.split("\n");
        int iter = 0; text = "";
        for (String str : chat) {
            if (!str.startsWith("Client"))
                text = text + chat[iter] + "\n";
            ++iter;
        }
        chat_area.setText(text);
    }

    private class getting_messages_class extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    while (true) {
                        String messageIn = in.readLine();
                        if (messageIn != null) {
                            System.out.println("Got: " + messageIn.replace("////n", "\n"));
                            add_message(messageIn.replace("////n", "\n"));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Mistake with getting message.");
                    e.printStackTrace();
                }
            }
        }
    }

    private class Wait_to_delete_writingPoint extends Thread{
        synchronized public void run() {
            try {
                wait(5000);
                delete_writingPoints();
                isWaiting = false;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void main(String[] args) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {}
            });
            new Wait_to_delete_writingPoint().start();
        }
    }
}
