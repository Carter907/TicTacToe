package com.tacer.tic_tac_toe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client extends Application {


    public static Stage window;
    private static ObjectOutputStream toServer = null;
    private static ObjectInputStream fromServer = null;
    private String team = "";

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage stage) {


        window = stage;
        window.setScene(Display.setTitleScreen());
        window.setTitle("Tic Tac Toe (Client)");
        window.show();

        try {
            Socket socket = new Socket("localhost", 8001);


            toServer = new ObjectOutputStream(socket.getOutputStream());
            fromServer = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();

        }


        window.setOnHidden(e -> {
            Display.threadPool.shutdownNow();
            while (!Display.threadPool.isTerminated()) {
                try {
                    Thread.sleep(10);
                    System.out.println(Display.threadPool);
                } catch (InterruptedException f) {
                    f.printStackTrace();
                }
            }
            System.out.println("all threads closed");
        });


    }

    private class Display {


        public static ExecutorService threadPool = Executors.newCachedThreadPool();
        public static int width = 500;
        public static int height = 400;

        public static Scene setTitleScreen() {


            BorderPane root = new BorderPane();
            VBox center = new VBox();
            center.setSpacing(40);
            center.setAlignment(Pos.CENTER);

            Label title = new Label("Welcome to Tic Tac Toe");
            title.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, 40));
            Display.threadPool.execute(animatedTitle(title));

            Button joinServer = new Button("Join a game");
            joinServer.setStyle("-fx-background-color: #dedede;");
            joinServer.setFont(FontPresets.REGULAR.getFont());

            joinServer.setPrefHeight(50);
            joinServer.setOnAction(e -> {

                Client.window.setScene(setServerScreen());
            });

            center.getChildren().addAll(title, joinServer);

            root.setCenter(center);

            Scene scene = new Scene(root, Display.width, Display.height);
            return scene;
        }

        public static Scene setGame() {

            BorderPane root = new BorderPane();
            root.setPadding(new Insets(30, 30, 30, 30));

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

            Display.threadPool.execute(Display.animatedTitle(title));
            Button reset = new Button("reset board");
            reset.setId("reset-btn");
            reset.setOnAction(e -> {
                try {

                    toServer.writeObject(new ServerRequest(e, ServerRequest.Request.RESET_BOARD));
                    toServer.flush();
                    Cell.cells = (Cell[][]) fromServer.readObject();
                } catch (IOException f) {
                    f.printStackTrace();
                } catch (ClassNotFoundException g) {
                    g.printStackTrace();
                }


            });

            Pane top = new Pane();
            top.getChildren().addAll(title, reset);

            root.setTop(top);

            root.setCenter(board);

            Scene scene = new Scene(root, Display.width, Display.height);
            scene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
            Cell.windowHeight.bind(scene.heightProperty());
            Cell.windowWidth.bind(scene.widthProperty());

            return scene;
        }

        public static Scene setClientWindow() {
            Scene scene = new Scene(new Pane(), Display.width, Display.height);
            return scene;
        }

        public static Scene setServerScreen() {


            VBox root = new VBox();
            root.setAlignment(Pos.CENTER);

            ComboBox<String> serverBox = new ComboBox<>();
            serverBox.setOnAction(e -> {

                window.setScene(setGame());
            });
            serverBox.setValue("please select a Server");
            serverBox.setPrefSize(400, 20);

            threadPool.execute(() -> {

                try {
                    while (true) {
                        toServer.writeObject(new ServerRequest(ServerRequest.Request.GET_INFO));

                        InetAddress socketInfo = (InetAddress) fromServer.readObject();

                        serverBox.getItems().add(socketInfo.toString());

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException f) {
                    f.printStackTrace();
                }

            });


            root.getChildren().add(serverBox);

            Scene scene = new Scene(root, Display.width, Display.height);

            return scene;
        }

        public static Runnable animatedTitle(Label title) {

            return () -> {
                try {
                    var obj = new Object() {
                        int i = 0;
                    };
                    while (true) {
                        obj.i++;
                        Thread.sleep(10);
                        Platform.runLater(() -> {
                            try {
                                title.setTextFill(Display.getColor(obj.i, obj.i % 512 <= 256));
                            } catch (IllegalArgumentException e) {

                            }

                        });
                    }
                } catch (InterruptedException e) {

                }
            };
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

        private enum FontPresets {

            REGULAR(Font.font("Times new Roman", FontWeight.BOLD, FontPosture.REGULAR, 17));

            private final Font font;

            FontPresets(Font font) {
                this.font = font;
            }

            public Font getFont() {
                return this.font;
            }
        }
    }
}