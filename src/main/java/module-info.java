module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    opens org.example to javafx.fxml, javafx.graphics;
    opens org.example.controller to javafx.fxml;
    exports org.example;
    exports org.example.controller;
}