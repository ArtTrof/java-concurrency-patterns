package app.patterns.divide_conquer.task;

import java.util.List;
import java.util.concurrent.RecursiveTask;

public class IntSums {
    private static final int THRESHOLD = 250;

    public static class Task extends RecursiveTask<Integer> {
        private final List<Integer> listValues;

        public Task(List<Integer> listValues) {
            this.listValues = listValues;
        }

        @Override
        protected Integer compute() {
            if (listValues.size() < THRESHOLD) {
                return listValues.stream().reduce(0, Integer::sum);
            } else {
                int mid = listValues.size() / 2;
                Task leftTask = new Task(listValues.subList(0, mid));
                Task rightTask = new Task(listValues.subList(mid, listValues.size()));

                leftTask.fork();              // start leftTask asynchronously
                int rightResult = rightTask.compute(); // compute rightTask in current thread
                int leftResult = leftTask.join();      // wait for leftTask result

                return leftResult + rightResult;
            }
        }
    }
}
