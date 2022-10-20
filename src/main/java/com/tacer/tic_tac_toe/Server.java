package com.tacer.tic_tac_toe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Application {

    public static boolean isActive = false;
    public static Stage window;
    public ExecutorService threadPool = Executors.newCachedThreadPool();

    private ExecutorService sockets = Executors.newFixedThreadPool(2);

    private BorderPane root;
    private GridPane board;
    private Label title;
    private Button reset;
    private Pane top;
    private Scene scene;

    @Override
    public void start(Stage stage) {

        root = new BorderPane();
        root.setPadding(new Insets(30, 30, 30, 30));

        board = new GridPane();
        board.setStyle("-fx-background-color: darkgray;");
        board.setHgap(4);
        board.setVgap(4);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {

                board.add(new Cell(row, col), col, row);
            }
        }

        title = new Label("Tic Tac Toe!");
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
            } catch (InterruptedException e) {

            }
        });
        reset = new Button("reset board");
        reset.setId("reset-btn");
        reset.setOnAction(e -> {

            for (Cell[] cellRow : Cell.cells) {
                for (Cell c : cellRow) {
                    c.setValue(null);
                    c.setText(null);

                }
            }

        });

        top = new Pane();
        top.getChildren().addAll(title, reset);

        root.setTop(top);

        root.setCenter(board);

        scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        Cell.windowHeight.bind(scene.heightProperty());
        Cell.windowWidth.bind(scene.widthProperty());

        window = stage;
        window.setScene(scene);
        window.setTitle("Tic Tac Toe(Server)");
        window.show();

        threadPool.execute(() -> {

            try {
                ServerSocket serverSocket = new ServerSocket(8001);
                isActive = true;
                while (true) {

                    Socket client = serverSocket.accept();
                    System.out.println("accepted client");
                    sockets.execute(new ClientHandler(this, client));
                    System.out.println("client handled");
                }

            } catch (IOException e) {

            }
        });


        window.setOnHidden(e -> {

            this.threadPool.shutdownNow();
        });

    }
    public BorderPane getRoot() {
        return root;
    }

    public void setRoot(BorderPane root) {
        this.root = root;
    }

    public GridPane getBoard() {
        return board;
    }

    public void setBoard(GridPane board) {
        this.board = board;
    }

    public Label getTitle() {
        return title;
    }

    public void setTitle(Label title) {
        this.title = title;
    }

    public Button getReset() {
        return reset;
    }

    public void setReset(Button reset) {
        this.reset = reset;
    }

    public Pane getTop() {
        return top;
    }

    public void setTop(Pane top) {
        this.top = top;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
    public static Color getColor(int i, boolean rev) {

        int r = 0;
        int g = 0;
        int b = 0;

        if (rev) {
            g = 256 - (i % 256);
            b = 256 - (i % 256);
        } else {
            b = i % 256;
            g = i % 256;
        }

        return Color.rgb(r, g, b);
    }

    public static void main(String[] args) {
        Server.launch(args);
    }


}