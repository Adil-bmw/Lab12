package task3;

import java.util.Arrays;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class MergeSortTask extends RecursiveTask<int[]> {
    private final int[] array;
    private static final int THRESHOLD = 10_000;

    public MergeSortTask(int[] array) {
        this.array = array;
    }

    @Override
    protected int[] compute() {
        if (array.length <= THRESHOLD) {
            int[] sorted = array.clone();
            Arrays.sort(sorted);
            return sorted;
        }

        int mid = array.length / 2;

        int[] left = Arrays.copyOfRange(array, 0, mid);
        int[] right = Arrays.copyOfRange(array, mid, array.length);

        MergeSortTask leftTask = new MergeSortTask(left);
        MergeSortTask rightTask = new MergeSortTask(right);

        leftTask.fork();
        int[] rightRes = rightTask.compute();
        int[] leftRes = leftTask.join();

        return merge(leftRes, rightRes);
    }

    private int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];

        int i = 0, j = 0, k = 0;

        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) result[k++] = left[i++];
            else result[k++] = right[j++];
        }

        while (i < left.length) result[k++] = left[i++];
        while (j < right.length) result[k++] = right[j++];

        return result;
    }

    public static void main(String[] args) {
        final int N = 1_000_000;
        int[] array = new int[N];

        java.util.Random r = new java.util.Random(42);
        for (int i = 0; i < N; i++) array[i] = r.nextInt();

        int[] copy = array.clone();

        long stdStart = System.nanoTime();
        Arrays.sort(copy);
        long stdTime = (System.nanoTime() - stdStart) / 1_000_000;

        ForkJoinPool pool = ForkJoinPool.commonPool();

        long parStart = System.nanoTime();
        int[] sorted = pool.invoke(new MergeSortTask(array));
        long parTime = (System.nanoTime() - parStart) / 1_000_000;

        System.out.println("Arrays.sort время: " + stdTime + " мс");
        System.out.println("ForkJoin время: " + parTime + " мс");

        System.out.println("Корректность: " + Arrays.equals(copy, sorted));
    }
}
