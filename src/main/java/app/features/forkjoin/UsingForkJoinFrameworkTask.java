package app.features.forkjoin;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * | Method            | Purpose                                 |
 * <p>
 * | `fork()`          | Submit task to pool from current thread |
 * <p>
 * | `join()`          | Wait for task and get result            |
 * <p>
 * | `invoke(task)`    | Submit + wait in same thread (blocking) |
 * <p>
 * | `submit(task)`    | Async submit, returns future-like task  |
 * <p>
 * | `execute(task)`   | Fire-and-forget async submit            |
 * <p>
 * | `invokeAll(...)`  | Submit multiple and join them           |
 * <p>
 * | `complete(value)` | Force-set result manually               |
 * <p>
 * | `compute()`       | Override – core of your logic           |
 * <p>
 * Task 1 – Recursive Sum
 * Modify RecSumTask to count how many times it splits
 * <p>
 * Print total splits at the end
 * (Use a shared AtomicInteger or a thread-safe counter)
 * <p>
 * Task 2 – Find Max Integer
 * Create a RecursiveTask<Integer> called FindMaxTask
 * <p>
 * Input: List<Integer>
 * <p>
 * Goal: Find the maximum value in the list using fork/join
 * <p>
 * Task 3 – Custom ForkJoinPool
 * Create a ForkJoinPool with:
 * <p>
 * Parallelism = 2
 * <p>
 * Custom UncaughtExceptionHandler
 * <p>
 * Submit a task that randomly throws exception (simulate crash)
 * <p>
 * Catch and log exceptions via handler
 * <p>
 * Task 4 – Use RecursiveAction
 * Create PrintListAction that prints list values in parallel
 * <p>
 * If list > N elements, split recursively
 * <p>
 * Otherwise, print directly
 * (forces action-based parallelization, no result)
 * <p>
 * Task 5 – Pool Debugging
 * Submit 2 parallel tasks (sum + max)
 * <p>
 * While they're running, call debugPool() every 200ms in a loop
 * <p>
 * Observe steal count, active threads, etc
 * <p>
 * Task 6 – Fibonacci (Bad Example)
 * Implement naive RecursiveTask<Long> for fib(n)
 * <p>
 * Show how this doesn’t scale (due to overlapping recomputation)
 * <p>
 * Use for demonstrating when not to use Fork/Join
 * <p>
 * [Optional Advanced] Task 7 – Parallel Merge Sort
 * Write RecursiveTask<List<Integer>> that:
 * <p>
 * Splits list
 * <p>
 * Sorts both halves in parallel
 * <p>
 * Merges results
 * <p>
 * Benchmark vs Collections.sort(...) or sequential sort
 */
public class UsingForkJoinFrameworkTask {


    public static void main(String[] args) throws InterruptedException {
        //task 1
        AtomicInteger counter = new AtomicInteger(0);
        var numbers = new LinkedList<Integer>();
        for (int i = 0; i < 500_000; i++) {
            numbers.add(i);
        }
        var commonPool = ForkJoinPool.commonPool();
        var task = new RecSumTask(numbers, counter);
        BigInteger result = commonPool.invoke(task);
        System.out.println("Result is: " + result);
        System.out.println("Splits counter is " + task.getCounterValue());
        System.out.println("\n\n");
        //task 2
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < 100_000; i++) {
            list.add(i);
        }
        var task2 = new RecMaxNumberTask(list, 0, list.size() - 1);
        Integer result2 = commonPool.invoke(task2);
        System.out.println("Result max number is: " + result2);
        System.out.println("\n\n");

        //task 3
        var handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Custom handler caught exception:");
                System.out.println("Thread: " + t.getName());
                System.out.println("Exception: " + e);
            }
        };
        var customPool = new ForkJoinPool(2, ForkJoinPool.defaultForkJoinWorkerThreadFactory, handler, true);
        List<ForkJoinTask<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(customPool.submit(new ThrowingTask()));
        }
        tasks.forEach(task3 -> {
            try {
                task3.join();  // This will throw if the task failed
            } catch (Exception e) {
                System.out.println("Caught exception from task: " + e);
            }
        });
        customPool.shutdown();

        //task4
        System.out.println("\n");
        var stringsList = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            stringsList.add("Item-" + i);
        }
        commonPool.invoke(new RecPrintAction(stringsList));

        //task5
        System.out.println("\n\n");
        var recSumTask = new RecSumTask(numbers, counter);
        var maxNumberTask = new RecMaxNumberTask(list, 0, list.size() - 1);
        commonPool.submit(recSumTask);
        commonPool.submit(maxNumberTask);
        while (!recSumTask.isDone() && !maxNumberTask.isDone()) {
            debugPool(commonPool);
            Thread.sleep(25);
        }

        //task6
        System.out.println("\n\n");
        var fibRecTask = new RecFibTask(5);
        ForkJoinTask<Long> submitted = commonPool.submit(fibRecTask);
        System.out.println("Result fib:" + submitted.join());

        //task7
        List<Integer> numbers7 = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            numbers7.add(random.nextInt());
        }
        // Parallel version
        System.out.println("\n\n");
        long start = System.nanoTime();
        List<Integer> parallelSorted = commonPool.invoke(new ParallelMergeSort(numbers));
        long parallelTime = System.nanoTime() - start;

        start = System.nanoTime();
        List<Integer> sequentialSorted = new ArrayList<>(numbers);
        Collections.sort(sequentialSorted);
        long sequentialTime = System.nanoTime() - start;
        System.out.println("Parallel sort:" + parallelTime);
        System.out.println("Sequential sort:" + sequentialTime);

    }

    static class RecSumTask extends RecursiveTask<BigInteger> {

        private static final long serialVersionUID = 1L;
        public static final int DIVIDE_AT = 500;

        private final List<Integer> numbers;
        private final AtomicInteger counter;

        public RecSumTask(List<Integer> numbers, AtomicInteger counter) {
            this.counter = counter;
            this.numbers = numbers;
        }

        @Override
        protected BigInteger compute() {
            var subTasks = new LinkedList<RecSumTask>();
            if (numbers.size() < DIVIDE_AT) {
                var subSum = BigInteger.ZERO;
                for (Integer number : numbers) {
                    subSum = subSum.add(BigInteger.valueOf(number));
                }
                return subSum;
            } else {
                var size = numbers.size();
                var numbersLeft = numbers.subList(0, size / 2);
                var numbersRight = numbers.subList(size / 2, size);
                counter.incrementAndGet();
                var recSumLeft = new RecSumTask(numbersLeft, counter);
                var recSumRight = new RecSumTask(numbersRight, counter);
                subTasks.add(recSumRight);
                subTasks.add(recSumLeft);
                recSumLeft.fork();
                recSumRight.fork();
            }

            var sum = BigInteger.ZERO;
            for (var recSum : subTasks) {
                sum = sum.add(recSum.join());
            }
            return sum;
        }

        public int getCounterValue() {
            return counter.getAcquire();
        }

    }

    static class RecMaxNumberTask extends RecursiveTask<Integer> {

        private final List<Integer> numbers;
        private final Integer min;
        private final Integer max;

        RecMaxNumberTask(List<Integer> numbers, Integer min, Integer max) {
            this.numbers = numbers;
            this.min = min;
            this.max = max;
        }

        @Override
        protected Integer compute() {
            // Add boundary checks
            if (min < 0 || max >= numbers.size()) {
                throw new IndexOutOfBoundsException("Invalid range [" + min + "," + max + "]");
            }
            if (min == max) {
                return numbers.get(min);
            }
            if (max - min <= 1) {
                return Math.max(numbers.get(min), numbers.get(max));
            }

            int mid = (min + max) / 2;
            var left = new RecMaxNumberTask(numbers, min, mid);
            var right = new RecMaxNumberTask(numbers, mid + 1, max);

            left.fork();
            int rightResult = right.compute();
            int leftResult = left.join();

            return Math.max(leftResult, rightResult);
        }
    }

    static class ThrowingTask extends RecursiveAction {

        @Override
        protected void compute() {
            if (Math.random() > 0.5) {
                throw new RuntimeException("Simulated crash!");
            }
            System.out.println(Thread.currentThread().getName() + " completed successfully");
        }
    }

    static class RecPrintAction extends RecursiveAction {
        private static final int THRESHOLD = 100;
        private final List<String> strings;

        RecPrintAction(List<String> strings) {
            this.strings = strings;
        }

        @Override
        protected void compute() {
            if (strings.isEmpty()) {
                System.out.println("\n");
                return;
            }
            if (strings.size() > THRESHOLD) {
                int midIndex = strings.size() / 2;
                var leftTask = new RecPrintAction(strings.subList(0, midIndex));
                var rightTask = new RecPrintAction(strings.subList(midIndex, strings.size()));
                invokeAll(leftTask, rightTask); // Better than fork()+join()
            } else {
                strings.forEach(System.out::println);
            }
        }
    }

    static class RecFibTask extends RecursiveTask<Long> {
        private final int n;

        RecFibTask(int n) {
            this.n = n;
        }

        @Override
        protected Long compute() {
            if (n <= 1) {
                return (long) n;
            }
            RecFibTask fib1 = new RecFibTask(n - 1);
            RecFibTask fib2 = new RecFibTask(n - 2);
            fib1.fork();
            long result2 = fib2.compute();
            long result1 = fib1.join();
            return result1 + result2;
        }
    }

    static class ParallelMergeSort extends RecursiveTask<List<Integer>> {
        private static final int THRESHOLD = 100;
        private final List<Integer> numbers;

        ParallelMergeSort(List<Integer> numbers) {
            this.numbers = numbers;
        }

        protected List<Integer> compute() {
            if (numbers.size() <= THRESHOLD) {
                List<Integer> sorted = new ArrayList<>(numbers);
                Collections.sort(sorted);
                return sorted;
            }
            int mid = numbers.size() / 2;
            ParallelMergeSort leftTask = new ParallelMergeSort(numbers.subList(0, mid));
            ParallelMergeSort rightTask = new ParallelMergeSort(numbers.subList(mid, numbers.size()));

            leftTask.fork();
            List<Integer> right = rightTask.compute();
            List<Integer> left = leftTask.join();

            return merge(left, right);
        }

        private List<Integer> merge(List<Integer> left, List<Integer> right) {
            List<Integer> merged = new ArrayList<>();
            int i = 0, j = 0;
            while (i < left.size() && j < right.size()) {
                if (left.get(i) <= right.get(j)) {
                    merged.add(left.get(i++));
                } else {
                    merged.add(right.get(j++));
                }
            }
            // Add remaining elements
            while (i < left.size()) merged.add(left.get(i++));
            while (j < right.size()) merged.add(right.get(j++));
            return merged;
        }
    }

    public static void debugPool(ForkJoinPool commonPool) {
        System.out.println("Debugging ForkJoinPool");
        System.out.println("Active Thread Count: " + commonPool.getActiveThreadCount());
        System.out.println("Pool Size: " + commonPool.getPoolSize());
        System.out.println("Parallelism level: " + commonPool.getParallelism());
        System.out.println("Queue submitted tasks: " + commonPool.getQueuedSubmissionCount());
        System.out.println("Steal count: " + commonPool.getStealCount());
        System.out.println("\n");
    }

}
