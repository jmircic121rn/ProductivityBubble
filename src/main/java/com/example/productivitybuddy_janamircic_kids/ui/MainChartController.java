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
import javafx.stage.Stage;

public class MainChartController {

    @FXML private TableView<ProcessModel> tableView;
    @FXML private TableColumn<ProcessModel, String> colName;
    @FXML private TableColumn<ProcessModel, String> colAlias;
    @FXML private TableColumn<ProcessModel, String> colCategory;
    @FXML private TableColumn<ProcessModel, Double> colCpu;
    @FXML private TableColumn<ProcessModel, Long>   colRam;
    @FXML private TableColumn<ProcessModel, Long>   colDuration;
    @FXML private PieChart pieChart;

    @FXML private Label labelProductivity;
    @FXML private Label labelWork;
    @FXML private Label labelSocialAndCommunication;
    @FXML private Label labelWebBrowsers;
    @FXML private Label labelEntertainment;
    @FXML private Label labelCreativity;
    @FXML private Label labelGaming;
    @FXML private Label labelOther;
    @FXML private Label labelUncategorized;

    private boolean pieChartInitialized = false;

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

            // azuriraj pie chart — uvek postavi novu listu, bez animacije
            var stats = MainApp.analyticsService.getProcessesTimePerCategory();
            System.out.println("Productivity value: " + stats.getOrDefault(Category.Productivity, 0L));
            System.out.println("Gaming value: " + stats.getOrDefault(Category.Gaming, 0L));
            System.out.println("Uncategorized value: " + stats.getOrDefault(Category.Uncategorized, 0L));



            pieChart.setAnimated(false);
            pieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Productivity",
                            stats.getOrDefault(Category.Productivity, 0L)),
                    new PieChart.Data("Work",
                            stats.getOrDefault(Category.Work, 0L)),
                    new PieChart.Data("Social",
                            stats.getOrDefault(Category.Social_and_Communication, 0L)),
                    new PieChart.Data("Web",
                            stats.getOrDefault(Category.WebBrowsers, 0L)),
                    new PieChart.Data("Entertainment",
                            stats.getOrDefault(Category.Entertainment, 0L)),
                    new PieChart.Data("Creativity",
                            stats.getOrDefault(Category.Creativity, 0L)),
                    new PieChart.Data("Gaming",
                            stats.getOrDefault(Category.Gaming, 0L)),
                    new PieChart.Data("Other",
                            stats.getOrDefault(Category.Other, 0L)),
                    new PieChart.Data("Uncategorized",
                            stats.getOrDefault(Category.Uncategorized, 0L))
            ));

            // azuriraj labele
            labelProductivity.setText("Productivity: " +
                    formatTime(stats.getOrDefault(Category.Productivity, 0L)));
            labelWork.setText("Work: " +
                    formatTime(stats.getOrDefault(Category.Work, 0L)));
            labelSocialAndCommunication.setText("Social: " +
                    formatTime(stats.getOrDefault(
                            Category.Social_and_Communication, 0L)));
            labelWebBrowsers.setText("Web Browsers: " +
                    formatTime(stats.getOrDefault(Category.WebBrowsers, 0L)));
            labelEntertainment.setText("Entertainment: " +
                    formatTime(stats.getOrDefault(Category.Entertainment, 0L)));
            labelCreativity.setText("Creativity: " +
                    formatTime(stats.getOrDefault(Category.Creativity, 0L)));
            labelGaming.setText("Gaming: " +
                    formatTime(stats.getOrDefault(Category.Gaming, 0L)));
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

    @FXML private void onProductivityDetails() {
        openCategoryView(Category.Productivity);
    }

    @FXML private void onWorkDetails() {
        openCategoryView(Category.Work);
    }

    @FXML private void onSocialAndCommunicationDetails() {
        openCategoryView(Category.Social_and_Communication);
    }

    @FXML private void onWebBrowsersDetails() {
        openCategoryView(Category.WebBrowsers);
    }

    @FXML private void onFunDetails() {
        openCategoryView(Category.Work);
    }

    @FXML private void onEntertainmentDetails() {
        openCategoryView(Category.Entertainment);
    }

    @FXML private void onCreativityDetails() {
        openCategoryView(Category.Creativity);
    }

    @FXML private void onGamingDetails() {
        openCategoryView(Category.Gaming);
    }

    @FXML private void onOtherDetails() {
        openCategoryView(Category.Other);
    }

    @FXML private void onUncategorizedDetails() {
        openCategoryView(Category.Uncategorized);
    }

    @FXML
    private void onSave() {
        MainApp.fileService.saveProcessesToJsonFile(MainApp.config.getMappingFile());
    }

    @FXML
    private void onLoad() {
        MainApp.fileService.loadProcessesFromFileToApp(MainApp.config.getMappingFile());
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