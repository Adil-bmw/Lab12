package task1;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class ArraySumTask extends RecursiveTask<Long> {
    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 100_000;

    public ArraySumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;

        if (length <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            int mid = start + length / 2;

            ArraySumTask leftTask = new ArraySumTask(array, start, mid);
            ArraySumTask rightTask = new ArraySumTask(array, mid, end);

            leftTask.fork();
            long rightResult = rightTask.compute();
            long leftResult = leftTask.join();

            return leftResult + rightResult;
        }
    }

    public static void main(String[] args) {
        final int N = 10_000_000;
        int[] array = new int[N];

        for (int i = 0; i < N; i++) array[i] = i + 1;

        long seqStart = System.nanoTime();
        long seqSum = 0;
        for (int v : array) seqSum += v;
        long seqTimeMs = (System.nanoTime() - seqStart) / 1_000_000;

        ForkJoinPool pool = ForkJoinPool.commonPool();

        long parStart = System.nanoTime();
        long parSum = pool.invoke(new ArraySumTask(array, 0, array.length));
        long parTimeMs = (System.nanoTime() - parStart) / 1_000_000;

        System.out.println("Последовательная сумма: " + seqSum);
        System.out.println("Время: " + seqTimeMs + " мс\n");
        System.out.println("Параллельная сумма: " + parSum);
        System.out.println("Время: " + parTimeMs + " мс");
    }
}

