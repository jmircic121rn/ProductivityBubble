package com.example.productivitybuddy_janamircic_kids;

import com.example.productivitybuddy_janamircic_kids.analytics.AnalyticsService;
import com.example.productivitybuddy_janamircic_kids.service.*;
import com.example.productivitybuddy_janamircic_kids.ui.MainChartController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static ProcessRegistry registry;
    public static FileService fileService;
    public static AnalyticsService analyticsService;
    public static ProcessManagement processManagement;
    public static WatcherService watcherService;
    public static SnapshotScheduler snapshotScheduler;
    public static ConfigLoader config;

    private static Thread scannerThread;
    private static Thread watcherThread;
    private static Thread analyticsThread;

    public static MainChartController mainController;

    @Override
    public void start(Stage stage) throws Exception {

        config = new ConfigLoader("config.properties");

        registry = new ProcessRegistry();
        fileService = new FileService(registry);
        analyticsService = new AnalyticsService(
                registry, config.getMonitorInterval());
        processManagement = new ProcessManagement(registry);
        watcherService = new WatcherService(
                config.getMappingFile(),
                () -> fileService.loadProcessesFromFileToApp(config.getMappingFile()));
        snapshotScheduler = new SnapshotScheduler(
                fileService,
                config.getSnapshotInterval(),
                "snapshots",
                config.getFixedSnapshotTimes());

        fileService.loadProcessesFromFileToApp(config.getMappingFile());

        scannerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    processManagement.getAllProcessesFromOS();
                    Thread.sleep(config.getMonitorInterval());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        scannerThread.setDaemon(true);
        scannerThread.start();

        watcherThread = new Thread(watcherService);
        watcherThread.setDaemon(true);
        watcherThread.start();

        analyticsThread = new Thread(analyticsService);
        analyticsThread.setDaemon(true);
        analyticsThread.start();

        Thread refreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (mainController != null) {
                        mainController.refresh();
                    }
                    Thread.sleep(config.getMonitorInterval());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load());
        mainController = loader.getController();

        stage.setTitle("Productivity Buddy");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            e.consume();
            shutdown();
        });
        stage.show();
    }

    public static void shutdown() {
        new Thread(() -> {
            watcherService.stop();
            analyticsService.stop();
            processManagement.shutdown();
            snapshotScheduler.shutdown();

            registry.updateTotalTimeForAllProcesses();

            fileService.saveProcessesToJsonFile(config.getMappingFile());
            fileService.shutdown(); // ceka da se save zavrsi pre nego sto nastavi

            Platform.runLater(Platform::exit);
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}