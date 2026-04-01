package com.example.productivitybuddy_janamircic_kids.ui;

import com.example.productivitybuddy_janamircic_kids.MainApp;
import com.example.productivitybuddy_janamircic_kids.model.Category;
import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryController {

    @FXML private Label labelTitle;
    @FXML private TableView<ProcessModel> tableView;
    @FXML private TableColumn<ProcessModel, String> colName;
    @FXML private TableColumn<ProcessModel, String> colAlias;
    @FXML private TableColumn<ProcessModel, Double> colCpu;
    @FXML private TableColumn<ProcessModel, Long>   colRam;
    @FXML private TableColumn<ProcessModel, Long>   colDuration;
    @FXML private ListView<String> top10List;

    private Category category;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getOriginalName()));
        colAlias.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getAliasName()));
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

        tableView.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

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
    }

    public void setCategory(Category category) {
        this.category = category;
        labelTitle.setText(category.toString());
        refresh();
    }

    public void refresh() {
        javafx.application.Platform.runLater(() -> {

            List<ProcessModel> filtered = MainApp.registry
                    .getAllProcesses().stream()
                    .filter(p -> p.getCategory() == category)
                    .collect(Collectors.toList());

            tableView.setItems(
                    FXCollections.observableArrayList(filtered));

            List<String> top10 = MainApp.registry.getAllProcesses()
                    .stream()
                    .filter(p -> p.getCategory() == category)
                    .sorted((a, b) -> Long.compare(
                            b.getTotalTimeSeconds() + b.getProcessSessionTimeSeconds(),
                            a.getTotalTimeSeconds() + a.getProcessSessionTimeSeconds()))
                    .limit(10)
                    .map(p -> p.getAliasName() + " — " +
                            formatTime(p.getTotalTimeSeconds()
                                    + p.getProcessSessionTimeSeconds()))
                    .collect(Collectors.toList());

            top10List.setItems(
                    FXCollections.observableArrayList(top10));
        });
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) labelTitle.getScene().getWindow();
        stage.close();
    }
}