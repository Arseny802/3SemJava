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
public class Client
{
    private JFrame jFrame = new JFrame("Buhoder");
    private javax.swing.JButton sendButton = new javax.swing.JButton("Send");
    private TextArea writeMessageArea = new TextArea();
    private TextArea chatArea = new TextArea();
    private BufferedReader in;
    private PrintWriter out;
    private boolean isWaiting = false;

    public static void main(String[] args)
    {
        new Client();
    }

    public Client()
    {

        sendButton.addActionListener(new ListenClass());

        chatArea.setEditable(false);
        chatArea.setFocusable(false);
        jFrame.setSize(560, 720);
        jFrame.setMinimumSize(new Dimension(560, 720));
        jFrame.setMaximumSize(new Dimension(560, 720));
        jFrame.setResizable(false);
        sendButton.setSize(80, (int) writeMessageArea.getSize().getHeight());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLayout(new java.awt.BorderLayout());

        JPanel South = new JPanel();
        South.add(writeMessageArea);
        South.add(sendButton);
        writeMessageArea.addKeyListener(new KeyListener()
        {
            short iter = 0;
            @Override
            public void keyTyped(KeyEvent e)
            {
                if (e.getSource() == KeyCode.ENTER) sendMessage(null);
                switch (iter % 3)
                {
                    case 0: sendMessage("."); break;
                    case 1: sendMessage(".."); break;
                    case 2: sendMessage("..."); break;
                }
                jFrame.setVisible(true);
                if (iter == 1000) iter = 0;
                else ++iter;
            }
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        jFrame.add(chatArea, BorderLayout.CENTER);
        jFrame.add(South, BorderLayout.SOUTH);
        jFrame.setVisible(true);

        connect();
    }

    private class ListenClass implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
            if (ae.getSource() == sendButton)
            {
                sendMessage(null);
            }
        }
    }

    private void addMessage(String msg)
    {
        if (msg.startsWith("Client"))
        {
            deleteWritingPoints();
            if(!isWaiting)
            {
                WaitToDeleteWritingPoint Wait = new WaitToDeleteWritingPoint();
                Wait.start();
                isWaiting = true;
            }
        }
        chatArea.append(msg);
        jFrame.setVisible(true);
    }

    private void sendMessage(String message)
    {
        Message messageOut = new Message(null);
        messageOut.msg = message;
        if (messageOut.msg == null)
        {
            messageOut.msg = writeMessageArea.getText();
        }
        messageOut.convertToSend();

        if (!Objects.equals(messageOut.msg, null))
        {
            out.println(messageOut.msg);
        }

        writeMessageArea.setText(null);
        jFrame.setVisible(true);
    }

    private void connect()
    {
        try
        {
            InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
            Socket socket = new Socket(ipAddress, 11675);

            in =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            refresh();
            GettingMessagesClass getting_messages = new GettingMessagesClass();
            getting_messages.start();
        }
        catch (Exception x)
        {
            x.printStackTrace();
        }
    }

    private void refresh() throws IOException
    {
        Message logs = new Message(null);
        logs.msg = in.readLine();
        if (logs.msg != null)
        {
            logs.convertToGet();
            chatArea.setText(logs.msg);
            jFrame.setVisible(true);
        }
    }

    private void deleteWritingPoints()
    {
        String text = chatArea.getText();
        String[] chat = text.split("\n");
        int iter = 0; text = "";
        for (String str : chat)
        {
            if (!str.startsWith("Client"))
                text = text + chat[iter] + "\n";
            ++iter;
        }
        chatArea.setText(text);
    }

    private class GettingMessagesClass extends Thread
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    while (true)
                    {
                        Message messageIn = new Message(null);
                        messageIn.msg = in.readLine();
                        if (messageIn.msg != null)
                        {
                            messageIn.convertToGet();
                            System.out.println("Got: " + messageIn.msg);
                            addMessage(messageIn.msg);
                        }
                    }
                }
                catch (IOException e)
                {
                    System.err.println("Mistake with getting message.");
                    e.printStackTrace();
                }
            }
        }
    }

    private class WaitToDeleteWritingPoint extends Thread
    {
        synchronized public void run()
        {
            try
            {
                wait(5000);
                deleteWritingPoints();
                isWaiting = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        public void main(String[] args)
        {
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run() {}
            });
            new WaitToDeleteWritingPoint().start();
        }
    }
}
