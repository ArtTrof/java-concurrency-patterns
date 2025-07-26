package app.threads_and_runnables;


/**
 * Starts two threads: one prints "Ping" every 500ms, the other prints "Pong" every 500ms.
 * <p>
 * Let them run for 3 seconds.
 * <p>
 * Then gracefully stop both using interrupt().
 */
public class PingPongThreadsTask  {
    public static void main(String[] args) throws InterruptedException {

        Runnable firstTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println(String.format("Ping by thread %s", Thread.currentThread().getName()));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        Runnable secondTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println(String.format("Pong by thread %s", Thread.currentThread().getName()));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        var firstThread = new Thread(firstTask);
        var secondThread = new Thread(secondTask);

        firstThread.start();
        secondThread.start();
        Thread.sleep(3000);
        firstThread.interrupt();
        secondThread.interrupt();
    }
}
