package com.example.productivitybuddy_janamircic_kids.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class WatcherService implements Runnable {

    private final Path filePath;
    private final Runnable onFileChange;
    private volatile boolean running = true;

    public WatcherService(String filePath, Runnable onFileChange) {
        this.filePath = Paths.get(filePath);
        this.onFileChange = onFileChange;
    }

    @Override
    public void run() {
        try{
            Path folder = filePath.getParent();
            if (folder == null) {
                folder = Paths.get(".");
            }
            String fileName = filePath.getFileName().toString();

            WatchService watchService = FileSystems.getDefault().newWatchService();

            folder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (running) {
                WatchKey watchKey = watchService.poll(1, TimeUnit.SECONDS);
                if(watchKey == null) {
                    continue;
                }

                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    Path filePath = (Path) watchEvent.context();
                    if(filePath.toString().equals(fileName)) {
                        onFileChange.run();
                    }
                }

                watchKey.reset();
            }
            watchService.close();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running = false;
    }
}
