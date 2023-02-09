module com.example.jirawordexporter {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.example.jirawordexporter to javafx.fxml;
    exports com.example.jirawordexporter;
}