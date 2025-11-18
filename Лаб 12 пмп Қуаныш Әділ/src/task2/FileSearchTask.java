package task2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

public class FileSearchTask extends RecursiveAction {
    private final File directory;
    private final String extension;
    private final List<String> results;

    public FileSearchTask(File directory, String extension, List<String> results) {
        this.directory = directory;
        this.extension = extension;
        this.results = results;
    }

    @Override
    protected void compute() {
        File[] files = directory.listFiles();
        if (files == null) return;

        List<FileSearchTask> subTasks = new ArrayList<>();

        for (File f : files) {
            if (f.isDirectory()) {
                FileSearchTask task = new FileSearchTask(f, extension, results);
                subTasks.add(task);
                task.fork();
            } else if (f.getName().endsWith(extension)) {
                results.add(f.getAbsolutePath());
            }
        }

        for (FileSearchTask t : subTasks) t.join();
    }

    private static void createTestDirectory(String rootPath) throws IOException {
        File root = new File(rootPath);
        root.mkdirs();

        new File(root, "file1.txt").createNewFile();
        new File(root, "file2.java").createNewFile();

        File sub1 = new File(root, "subdir1");
        sub1.mkdir();
        new File(sub1, "file3.txt").createNewFile();

        File sub2 = new File(sub1, "subdir2");
        sub2.mkdir();
        new File(sub2, "file4.txt").createNewFile();

        File sub3 = new File(root, "subdir3");
        sub3.mkdir();
        new File(sub3, "file5.txt").createNewFile();
    }

    public static void main(String[] args) throws Exception {
        String rootPath = "test_directory";
        createTestDirectory(rootPath);

        File root = new File(rootPath);
        String ext = ".txt";

        List<String> results = Collections.synchronizedList(new ArrayList<String>());

        long start = System.nanoTime();
        ForkJoinPool.commonPool().invoke(new FileSearchTask(root, ext, results));
        long timeMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Найденные файлы:");
        int i = 1;
        for (String p : results) {
            System.out.println(i++ + ". " + p);
        }

        System.out.println("\nВсего найдено: " + results.size());
        System.out.println("Время: " + timeMs + " мс");
    }
}
