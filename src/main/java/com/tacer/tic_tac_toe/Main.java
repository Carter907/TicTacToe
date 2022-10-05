package com.tacer.tic_tac_toe;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import java.util.concurrent.*;
import java.util.*;
public class Main extends Application {

    public static Stage window;
    public static ExecutorService threadPool = Executors.newCachedThreadPool();


    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(30,30,30,30));

        GridPane board = new GridPane();
        board.setStyle("-fx-background-color: darkgray;");
        board.setHgap(4);
        board.setVgap(4);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {

                board.add(new Cell(row, col), col, row);
            }
        }

        Label title = new Label("Tic Tac Toe!");
        title.translateXProperty().bind(root.widthProperty().divide(2).subtract(80));
        title.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 30));

        threadPool.execute(() -> {
            try {
                var obj = new Object() {
                    int i = 0;
                };
                while (true) {
                    obj.i++;
                    Thread.sleep(10);
                    Platform.runLater(() -> {
                        try {
                            title.setTextFill(getColor(obj.i, obj.i % 512 <= 256));
                        } catch (IllegalArgumentException e) {

                        }

                    });
                }
            }
            catch (InterruptedException e) {

            }
        });
        Button reset = new Button("reset board");
        reset.setId("reset-btn");
        reset.setOnAction(e -> {

            for (Cell[] cellRow: Cell.cells) {
                for (Cell c: cellRow) {
                    c.setValue(null);
                    c.setText(null);

                }
            }

        });

        Pane top = new Pane();
        top.getChildren().addAll(title, reset);

        root.setTop(top);

        root.setCenter(board);

        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        Cell.windowHeight.bind(scene.heightProperty());
        Cell.windowWidth.bind(scene.widthProperty());

        window = stage;
        window.setScene(scene);
        window.setTitle("Tic Tac Toe");
        window.show();

    }
    public static Color getColor(int i, boolean rev) {

        int r = 0;
        int g = 0;
        int b = 0;

        if (rev) {
            g = 256-(i % 256);
            b = 256-(i % 256);
        }
        else {
            b = i % 256;
            g = i % 256;
        }

        return Color.rgb(r,g,b);
    }

    

    public static void main(String[] args) {
        launch();
    }
}