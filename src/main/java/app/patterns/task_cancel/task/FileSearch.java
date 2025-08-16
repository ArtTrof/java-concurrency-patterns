package app.patterns.task_cancel.task;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileSearch {
    private Thread searchThread;
    private final String searchText;
    private final Path rootDir;
    private final Consumer<Path> resultCallback;

    public FileSearch(String searchText, Path rootDir, Consumer<Path> resultCallback) {
        this.searchText = searchText.toLowerCase();
        this.rootDir = rootDir;
        this.resultCallback = resultCallback;
    }

    public void start() {
        searchThread = new Thread(() -> {
            try {
                search(rootDir);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.out.println("Search was cancelled");
            } catch (IOException e) {
                System.err.println("IO error during search: " + e.getMessage());
            }
        });
        searchThread.start();
    }

    public void cancel() {
        if (searchThread != null) {
            searchThread.interrupt();
        }
    }

    private void search(Path dir) throws IOException, InterruptedException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                // Check for interruption frequently
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Search cancelled");
                }

                if (Files.isDirectory(entry)) {
                    search(entry); // Recurse into subdirectories
                } else if (Files.isRegularFile(entry)) {
                    try {
                        String content = new String(Files.readAllBytes(entry)).toLowerCase();
                        if (content.contains(searchText)) {
                            resultCallback.accept(entry);
                        }
                    } catch (IOException e) {
                        System.err.println("Skipping unreadable file: " + entry);
                    }
                }
            }
        }
    }
}