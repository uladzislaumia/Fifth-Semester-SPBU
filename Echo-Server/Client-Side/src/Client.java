public class Client {

    private int port;
    private Thread[] threads;

    public Client(int port, int clientNumber) {
        threads = new Thread[clientNumber];
        for (int i = 0; i < clientNumber; i++) {
            threads[i] = new Thread(new ServerHandler(port));
        }
    }

    public void start() {
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
    }

    public static void main(String[] args) {
        new Client(1777, 1).start();
    }
}
