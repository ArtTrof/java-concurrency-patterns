package app.features.futures;


import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * CompletableFuture is a Future that may be manually completed. It combines a
 * Future interface with the CompletionStage interface, supporting dependent
 * actions that trigger upon its completion, similarly to a callback.
 * <p>
 * Important: Specify an Executor for async methods when available. All async
 * methods without an explicit Executor argument are performed using the
 * ForkJoinPool.commonPool();
 * <p>
 * Important 2: Mostly of the CompletableFuture methods returns a new
 * CompletableFuture.
 * <p>
 * == Quick terminology guide ==
 * <p>
 * = Async =
 * <p>
 * xxxAsync(...); // Async method executed in the ForkJoinPool.commonPool();
 * <p>
 * xxxAsync(..., Executor executor); // Executed in the specified Executor, good
 * for Java EE.
 * <p>
 * <p>
 * = supply x run =
 * <p>
 * supplyAsync(Supplier<U> supplier); // will complete asynchronously by calling
 * supplier.
 * <p>
 * runAsync(Runnable runnable); // will complete after the runnable executions;
 * <p>
 * <p>
 * = thenApply x thenAccept x thenRun
 * <p>
 * thenApply: transforms a value to another type;
 * <p>
 * thenAccept: accepts a consumer to the result value;
 * <p>
 * thenRun: accepts a Runnable to be executed after the result is ready;
 * <p>
 * <p>
 * == Quick API guide ==
 * <p>
 * = Creating =
 * <p>
 * new CompletableFuture<>();
 * <p>
 * CompletableFuture.supplyAsync(Supplier<U>supplier);
 * <p>
 * CompletableFuture.supplyAsync(Supplier<U> supplier, Executor executor);
 * <p>
 * CompletableFuture.runAsync(Runnable runnable);
 * <p>
 * CompletableFuture.runAsync(Runnable runnable, Executor executor);
 * <p>
 * <p>
 * = Mapping values =
 * <p>
 * completableFuture.thenApply(Function<? super T,? extends U> fn);
 * <p>
 * completableFuture.thenApplyAsync(Function<? super T,? extends U> fn);
 * <p>
 * completableFuture.thenApplyAsync(Function<? super T,? extends U> fn, Executor
 * executor);
 * <p>
 * <p>
 * = Callback on completion =
 * <p>
 * completableFuture.thenAccept(Consumer<? super T> block);
 * <p>
 * completableFuture.thenRun(Runnable action);
 * <p>
 * <p>
 * = Error handling =
 * <p>
 * completableFuture.exceptionally(ex -> ex.getMessage());
 * <p>
 * completableFuture.handle((value, ex) -> {if value != null... else {}})
 * <p>
 * <p>
 * = Pipeline =
 * <p>
 * Chain one future dependent on the other
 * <p>
 * completableFuture.thenCompose(Function<? super T,CompletableFuture<U>> fn);
 * // flatMap
 * <p>
 * <p>
 * = Mapping values from Two Futures =
 * <p>
 * completableFuture.thenCombine(CompletableFuture<? extends U> other,
 * BiFunction<? super T,? super U,? extends V> fn) ex.:
 * <p>
 * <p>
 * = Waiting for first CompletableFuture to complete =
 * <p>
 * Two services, one fast and the other slow. Fastest always wins.
 * <p>
 * completableFuture.acceptEither(CompletableFuture<? extends T> other,
 * Consumer<? super T> block);
 * <p>
 * <p>
 * = Transforming first completed =
 * <p>
 * completableFuture.applyToEither(CompletableFuture<? extends T> other,
 * Function<? super T,U> fn)
 * <p>
 * <p>
 * = Combining multiple CompletableFuture together =
 * <p>
 * CompletableFuture.allOf(CompletableFuture<?>... cfs)
 * <p>
 * CompletableFuture.anyOf(CompletableFuture<?>... cfs)
 * <p>
 * <p>
 * = Get-Complete value =
 * <p>
 * CompletableFuture.get() // block
 * <p>
 * CompletableFuture.complete() // complete future's lifecycle
 * <p>
 * CompletableFuture.obtrudeValue() // ignores complete
 * <p>
 * CompletableFuture.join() // same as get
 * <p>
 * CompletableFuture.getNow(valueIfAbsent) // immediately return
 * <p>
 * CompletableFuture.completeExceptionally() // completes throwing a exception
 * <p>
 * CompletableFuture.completeExceptionally(ex) // completes with a exception
 * <p>
 * Important Caveat
 * Even without *Async, if the previous stage is async,
 * the continuation still runs asynchronously - just in the same thread that completed
 * it rather than a fresh thread. True serialization only happens if you
 * complete futures manually from a non-async context.
 */
public class UsingCompletableFuture {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        var random = new Random();
        var executor = Executors.newCachedThreadPool();
        // Creating
        CompletableFuture<Integer> randomNum = CompletableFuture.supplyAsync(() -> random.nextInt(140), executor);

        // Mapping
        String strNum = randomNum.thenApplyAsync(n -> Integer.toString(n), executor).get();
        System.out.println("Executed " + strNum);

        // Combining
        CompletableFuture<Integer> anotherNum = CompletableFuture.supplyAsync(() -> random.nextInt(140), executor);

        // accept both and do something
        randomNum.thenAcceptBoth(anotherNum, (num1, num2) -> {
            System.out.println("Num1 is: " + num1);
            System.out.println("Num2 is: " + num2);
        });

        // combine both into a new type/value
        CompletableFuture<Integer> mappedAndCombined = randomNum.thenCombine(anotherNum, (num1, num2) -> num1 + num2);

        // retrieving value
        Integer value = mappedAndCombined.get();
        System.out.println("Sum " + value);

        // Undefined time task
        Supplier<Double> randomDouble = () -> {
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return random.nextDouble();
        };

        // Run after executed
        CompletableFuture<Double> f1 = CompletableFuture.supplyAsync(randomDouble);
        CompletableFuture<Double> f2 = CompletableFuture.supplyAsync(randomDouble);
        CompletableFuture<Double> f3 = CompletableFuture.supplyAsync(randomDouble);
        CompletableFuture<Double> f4 = CompletableFuture.supplyAsync(randomDouble);
        CompletableFuture.anyOf(f1, f2, f3, f4).thenRun(() -> System.out.println("Completed"));

        // Fastest result will be delivered
        // Undefined time task - static value
        Supplier<String> getVal = () -> {
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "First";
        };
        Supplier<String> getVal2 = () -> {
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Second";
        };
        CompletableFuture
                .supplyAsync(getVal)
                .acceptEitherAsync(CompletableFuture.supplyAsync(getVal2, executor), (firstToBeReady) -> System.out.println(firstToBeReady), executor);
        executor.shutdown();
        executor.awaitTermination(3000, TimeUnit.SECONDS);
    }

}
