package com.example.productivitybuddy_janamircic_kids.ui;

import com.example.productivitybuddy_janamircic_kids.MainApp;
import com.example.productivitybuddy_janamircic_kids.model.Category;
import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;
import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.property.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Map;

public class MainChartController {

    @FXML private TableView<ProcessModel> tableView;
    @FXML private TableColumn<ProcessModel, String> colName;
    @FXML private TableColumn<ProcessModel, String> colAlias;
    @FXML private TableColumn<ProcessModel, String> colCategory;
    @FXML private TableColumn<ProcessModel, Double> colCpu;
    @FXML private TableColumn<ProcessModel, Long>   colRam;
    @FXML private TableColumn<ProcessModel, Long>   colDuration;
    @FXML private PieChart pieChart;
    @FXML private ImageView kittyWalk;

    @FXML private Label labelWork;
    @FXML private Label labelFun;
    @FXML private Label labelOther;
    @FXML private Label labelUncategorized;

    private final ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Work", 1),
            new PieChart.Data("Fun", 1),
            new PieChart.Data("Other", 1),
            new PieChart.Data("Uncategorized", 1)
    );

    private Image kittyImg1;
    private Image kittyImg2;
    private int kittyFrame = 0;

    @FXML
    public void initialize() {

        colName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getOriginalName()));
        colAlias.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getAliasName()));
        colCategory.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCategory().toString()));
        colCpu.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getCpuUsage()));
        colRam.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getRamUsage()));
        colDuration.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getTotalTimeSeconds()
                                + data.getValue().getProcessSessionTimeSeconds()));

        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ProcessModel selected = tableView
                        .getSelectionModel().getSelectedItem();
                if (selected != null) {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                MainApp.class.getResource(
                                        "process-detail-view.fxml"));
                        Scene scene = new Scene(loader.load());
                        ProcessDetailController controller =
                                loader.getController();
                        controller.setProcess(selected);

                        Stage stage = new Stage();
                        stage.setTitle(selected.getOriginalName());
                        stage.setScene(scene);
                        stage.setOnHidden(e -> controller.stopAutoRefresh());
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        pieChart.setAnimated(false);
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);
        pieChart.setData(pieData);

        startKittyAnimation();
    }

    private void startKittyAnimation() {
        try {
            kittyImg1 = new Image(MainApp.class.getResourceAsStream("hello_kitty.png"));
            kittyImg2 = new Image(MainApp.class.getResourceAsStream("hello_kitty_walk.png"));
        } catch (Exception e) {
            return;
        }

        if (kittyWalk == null) return;

        TranslateTransition walk = new TranslateTransition(Duration.seconds(8), kittyWalk);
        walk.setFromX(0);
        walk.setToX(800);
        walk.setCycleCount(Animation.INDEFINITE);
        walk.setAutoReverse(true);
        walk.play();

        javafx.animation.Timeline frameSwap = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(300), e -> {
                    kittyFrame = 1 - kittyFrame;
                    kittyWalk.setImage(kittyFrame == 0 ? kittyImg1 : kittyImg2);
                    if (walk.getCurrentRate() < 0) {
                        kittyWalk.setScaleX(-1);
                    } else {
                        kittyWalk.setScaleX(1);
                    }
                })
        );
        frameSwap.setCycleCount(Animation.INDEFINITE);
        frameSwap.play();
    }

    public void refresh() {
        javafx.application.Platform.runLater(() -> {

            ObservableList<ProcessModel> items = tableView.getItems();
            if (items == null || items.isEmpty()) {
                tableView.setItems(FXCollections.observableArrayList(
                        MainApp.registry.getAllProcesses()));
            } else {
                items.setAll(MainApp.registry.getAllProcesses());
            }

            Map<Category, Long> stats = MainApp.analyticsService.getProcessesTimePerCategory();

            long workTime = stats.getOrDefault(Category.Work, 0L);
            long funTime = stats.getOrDefault(Category.Fun, 0L);
            long otherTime = stats.getOrDefault(Category.Other, 0L);
            long uncatTime = stats.getOrDefault(Category.Uncategorized, 0L);
            long totalTime = workTime + funTime + otherTime + uncatTime;

            pieData.get(0).setPieValue(Math.max(workTime, 0));
            pieData.get(1).setPieValue(Math.max(funTime, 0));
            pieData.get(2).setPieValue(Math.max(otherTime, 0));
            pieData.get(3).setPieValue(Math.max(uncatTime, 0));

            if (totalTime > 0) {
                pieData.get(0).setName("Work " + String.format("%.1f%%", workTime * 100.0 / totalTime));
                pieData.get(1).setName("Fun " + String.format("%.1f%%", funTime * 100.0 / totalTime));
                pieData.get(2).setName("Other " + String.format("%.1f%%", otherTime * 100.0 / totalTime));
                pieData.get(3).setName("Uncategorized " + String.format("%.1f%%", uncatTime * 100.0 / totalTime));
            } else {
                pieData.get(0).setName("Work");
                pieData.get(1).setName("Fun");
                pieData.get(2).setName("Other");
                pieData.get(3).setName("Uncategorized");
            }

            labelWork.setText("Work: " + formatTime(workTime));
            labelFun.setText("Fun: " + formatTime(funTime));
            labelOther.setText("Other: " + formatTime(otherTime));
            labelUncategorized.setText("Uncategorized: " + formatTime(uncatTime));
        });
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }

    @FXML private void onWorkDetails() {
        openCategoryView(Category.Work);
    }

    @FXML private void onFunDetails() {
        openCategoryView(Category.Fun);
    }

    @FXML private void onOtherDetails() {
        openCategoryView(Category.Other);
    }

    @FXML private void onUncategorizedDetails() {
        openCategoryView(Category.Uncategorized);
    }

    @FXML
    private void onSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Process Info");
        fileChooser.setInitialFileName("process_info.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());
        if (file != null) {
            MainApp.fileService.saveProcessesToJsonFile(file.getAbsolutePath());
        }
    }

    @FXML
    private void onLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Process Info");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(tableView.getScene().getWindow());
        if (file != null) {
            MainApp.fileService.loadProcessesFromFileToApp(file.getAbsolutePath());
        }
    }

    @FXML
    private void onShutdown() {
        MainApp.shutdown();
    }

    private void openCategoryView(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(
                            "category-view.fxml"));
            Scene scene = new Scene(loader.load());
            CategoryController controller = loader.getController();
            controller.setCategory(category);

            Stage stage = new Stage();
            stage.setTitle(category.toString());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
