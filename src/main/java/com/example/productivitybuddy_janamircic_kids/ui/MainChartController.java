package com.example.productivitybuddy_janamircic_kids.ui;

import com.example.productivitybuddy_janamircic_kids.MainApp;
import com.example.productivitybuddy_janamircic_kids.model.Category;
import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainChartController {

    @FXML private TableView<ProcessModel> tableView;
    @FXML private TableColumn<ProcessModel, String> colName;
    @FXML private TableColumn<ProcessModel, String> colAlias;
    @FXML private TableColumn<ProcessModel, String> colCategory;
    @FXML private TableColumn<ProcessModel, Double> colCpu;
    @FXML private TableColumn<ProcessModel, Long>   colRam;
    @FXML private TableColumn<ProcessModel, Long>   colDuration;
    @FXML private PieChart pieChart;

    @FXML private Label labelWork;
    @FXML private Label labelFun;
    @FXML private Label labelOther;
    @FXML private Label labelUncategorized;

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
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

    public void refresh() {
        javafx.application.Platform.runLater(() -> {

            // azuriraj tabelu
            ObservableList<ProcessModel> items = tableView.getItems();
            if (items == null || items.isEmpty()) {
                tableView.setItems(FXCollections.observableArrayList(
                        MainApp.registry.getAllProcesses()));
            } else {
                items.setAll(MainApp.registry.getAllProcesses());
            }

            // azuriraj pie chart
            var stats = MainApp.analyticsService.getProcessesTimePerCategory();

            pieChart.setAnimated(false);
            pieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Work",
                            stats.getOrDefault(Category.Work, 0L)),
                    new PieChart.Data("Fun",
                            stats.getOrDefault(Category.Fun, 0L)),
                    new PieChart.Data("Other",
                            stats.getOrDefault(Category.Other, 0L)),
                    new PieChart.Data("Uncategorized",
                            stats.getOrDefault(Category.Uncategorized, 0L))
            ));

            // azuriraj labele
            labelWork.setText("Work: " +
                    formatTime(stats.getOrDefault(Category.Work, 0L)));
            labelFun.setText("Fun: " +
                    formatTime(stats.getOrDefault(Category.Fun, 0L)));
            labelOther.setText("Other: " +
                    formatTime(stats.getOrDefault(Category.Other, 0L)));
            labelUncategorized.setText("Uncategorized: " +
                    formatTime(stats.getOrDefault(Category.Uncategorized, 0L)));
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