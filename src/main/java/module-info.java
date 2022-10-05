module com.tacer.tic_tac_toe {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.tacer.tic_tac_toe to javafx.fxml;
    exports com.tacer.tic_tac_toe;
}