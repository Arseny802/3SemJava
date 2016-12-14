/**
 * Created by Арсений on 12.12.2016.
 */

import buhoder.Client;
import buhoder.Server;

public class Main {
    public static void main(String[] args) {

        new t1().start();
        new t2().start();
        new t3().start();
        new t4().start();
    }
    private static class t1 extends Thread {
        public void run(){
            new Server();
        }
    }
    private static class t2 extends Thread {
        public void run(){
            new Client();
        }
    }
    private static class t3 extends Thread {
        public void run(){
            new Client();
        }
    }
    private static class t4 extends Thread {
        public void run(){
            new Client();
        }
    }
}
