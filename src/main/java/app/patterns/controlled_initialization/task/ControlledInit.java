package app.patterns.controlled_initialization.task;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ControlledInit {
    private CountDownLatch latch;
    private ServiceA serviceA;
    private ServiceB serviceB;
    private ServiceC serviceC;

    public void initServiceA() {
        try {
            Thread.sleep(1000);
            serviceA = new ServiceA();
            System.out.println("Service A created");
            latch.countDown();
        } catch (InterruptedException e) {
        }
    }

    public void initServiceB() {
        try {
            Thread.sleep(2000);
            serviceB = new ServiceB();
            System.out.println("Service B created");
            latch.countDown();
        } catch (InterruptedException e) {
        }
    }

    public void initServiceC() {
        try {
            Thread.sleep(3000);
            serviceC = new ServiceC();
            System.out.println("Service C created");
            latch.countDown();
        } catch (InterruptedException e) {
        }
    }

    public void initServicesConcurrently(List<Runnable> runnables, ExecutorService executors) throws InterruptedException {
        latch = new CountDownLatch(runnables.size());
        for (Runnable runnable : runnables) {
            executors.submit(runnable);
        }
        executors.shutdown();
        executors.awaitTermination(10, TimeUnit.SECONDS);
    }

    public void processRequest() throws InterruptedException {
        latch.await();
        if (serviceA != null)
            System.out.println("Service A created, instance: " + serviceA);
        if (serviceB != null)
            System.out.println("Service B created, instance: " + serviceB);
        if (serviceC != null)
            System.out.println("Service C created, instance: " + serviceC);
    }

    static class ServiceA {
    }

    static class ServiceB {
    }

    static class ServiceC {
    }

    public static void main(String[] args) {
        var initializer = new ControlledInit();
        var executors = Executors.newCachedThreadPool();
        Runnable initServiceA = initializer::initServiceA;
        Runnable initServiceC = initializer::initServiceC;
        List<Runnable> tasks = List.of(initServiceA, initServiceC);
        try {
            initializer.initServicesConcurrently(tasks, executors);
            initializer.processRequest();
        } catch (InterruptedException e) {
        }

    }
}
