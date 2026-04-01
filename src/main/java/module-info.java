module com.example.productivitybuddy_janamircic_kids {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.example.productivitybuddy_janamircic_kids to javafx.fxml;
    opens com.example.productivitybuddy_janamircic_kids.ui to javafx.fxml;
    opens com.example.productivitybuddy_janamircic_kids.model to javafx.fxml, com.google.gson;
    opens com.example.productivitybuddy_janamircic_kids.service to javafx.fxml, com.google.gson;
    opens com.example.productivitybuddy_janamircic_kids.analytics to javafx.fxml;

    exports com.example.productivitybuddy_janamircic_kids;
}