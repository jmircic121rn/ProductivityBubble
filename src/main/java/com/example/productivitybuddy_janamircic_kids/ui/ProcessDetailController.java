package com.example.productivitybuddy_janamircic_kids.ui;

import com.example.productivitybuddy_janamircic_kids.MainApp;
import com.example.productivitybuddy_janamircic_kids.model.Category;
import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProcessDetailController {

    @FXML private Label labelTitle;
    @FXML private Label labelName;
    @FXML private Label labelPid;
    @FXML private Label labelCpu;
    @FXML private Label labelRam;
    @FXML private Label labelRank;
    @FXML private Label labelTime;
    @FXML private Label labelFreeze;
    @FXML private TextField fieldAlias;
    @FXML private ComboBox<Category> comboCategory;
    @FXML private Button btnFreeze;

    private ProcessModel process;

    @FXML
    public void initialize() {
        comboCategory.setItems(FXCollections.observableArrayList(
                Category.values()));
    }

    public void setProcess(ProcessModel process) {
        this.process = process;

        labelTitle.setText(process.getOriginalName());
        labelName.setText(process.getOriginalName());
        labelPid.setText(String.valueOf(process.getProcessId()));
        fieldAlias.setText(process.getAliasName());
        comboCategory.setValue(process.getCategory());

        osvezi();
    }

    public void osvezi() {
        if (process == null) return;

        javafx.application.Platform.runLater(() -> {
            labelCpu.setText(process.getCpuUsage() + "%");
            labelRam.setText(process.getRamUsage() + " MB");

            long ukupno = process.getTotalTimeSeconds()
                    + process.getProcessSessionTimeSeconds();
            long hours = ukupno / 3600;
            long minutes = (ukupno % 3600) / 60;
            labelTime.setText(hours + "h " + minutes + "m");

            long rank = MainApp.registry.getAllProcesses().stream()
                    .filter(p -> (p.getTotalTimeSeconds()
                            + p.getProcessSessionTimeSeconds()) > ukupno)
                    .count() + 1;
            labelRank.setText(rank + " / "
                    + MainApp.registry.getAllProcesses().size());

            if (process.isTrackingFreezed()) {
                labelFreeze.setText("Frozen ❄");
                btnFreeze.setText("Unfreeze Tracking");
            } else {
                labelFreeze.setText("Active ✓");
                btnFreeze.setText("Freeze Tracking");
            }
        });
    }

    @FXML
    private void onSetAlias() {
        String alias = fieldAlias.getText().trim();
        if (!alias.isEmpty()) {
            process.setAliasName(alias);
            MainApp.mainController.refresh();

        }
    }

    @FXML
    private void onSetCategory() {
        Category selected = comboCategory.getValue();
        if (selected != null) {
            process.setCategory(selected);
            MainApp.mainController.refresh();
        }
    }

    @FXML
    private void onKillProcess() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kill Process");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will terminate "
                + process.getOriginalName());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ProcessHandle.of(process.getProcessId())
                        .ifPresent(ProcessHandle::destroy);
                MainApp.registry.removeProcess(
                        process.getOriginalName());
                onBack();
            }
        });
    }

    @FXML
    private void onFreezeTracking() {
        process.setTrackingFreezed(!process.isTrackingFreezed());
        osvezi();
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) labelName.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void onSave() {
        String alias = fieldAlias.getText().trim();
        if (!alias.isEmpty()) {
            process.setAliasName(alias);
        }
        Category selected = comboCategory.getValue();
        if (selected != null) {
            process.setCategory(selected);
        }
        MainApp.fileService.saveProcessesToJsonFile(
                MainApp.config.getMappingFile());
        MainApp.mainController.refresh();
    }
}