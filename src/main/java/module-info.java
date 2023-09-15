module com.example.filetreecomparer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.filetreecomparer to javafx.fxml;
    exports com.example.filetreecomparer;
}