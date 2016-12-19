package buhoder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Арсений on 17.12.2016.
 */

class Message {

    /**
     * Класс, отвечающий за обработку сообщений
     * для их пекредачи. Предназначения функций
     * ясны из их названий. Старая периписка
     * хранится в отдельном файле с названием
     * "Server_logs.txt", для её хранения после
     * закрытия программы.
     */


    String msg;
    private final static String enter = "////n";
    private final String senderName;
    static String helloMessage;
    static String byeMessage;

    Message(String senderName) {
        this.senderName = senderName;
    }

    void convertToSend() {
        this.msg = msg.replace("\n", enter);
    }

    void convertToGet() {
        this.msg = msg.replace(enter, "\n");
    }

    void formatMessage() {
        if (this.msg.equals(".") || this.msg.equals("..") || this.msg.equals("...")) {
            this.msg = senderName + ": " + this.msg + "\n";
        } else {
            this.msg = "[" + getTime() + "] " + senderName + ": " + this.msg + "\n";
            saveLogs(this.msg);
        }
    }

    void sayHello() {
        this.msg = getLogs();
        if (this.msg != null) this.convertToSend();
        helloMessage = "[" + getTime() + "] " + senderName + " comes now" + enter;

    }

    void sayGoodbye() {
        System.out.println("[" + getTime() + "] " + senderName + " has left");
        byeMessage = "[" + getTime() + "] " + senderName + " has left";

    }

    private String getTime() {
        return new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    private void saveLogs(String newText) {
        File file = new File("Server_logs.txt");
        String text = getLogs() + newText;

        try {
            if(!file.exists()) file.createNewFile();

            try (PrintWriter out = new PrintWriter(file.getAbsoluteFile())) {
                out.print(text);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLogs() {
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
