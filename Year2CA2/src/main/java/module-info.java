module com.example.year2ca2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.year2ca2 to javafx.fxml;
    exports com.example.year2ca2;
}