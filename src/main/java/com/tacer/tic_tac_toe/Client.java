package com.tacer.tic_tac_toe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;


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


        window.setOnHidden(e -> {

            Display.serverHandler.shutdownNow();
            double time = 0.0;
            if (!Display.serverHandler.isTerminated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            System.out.println(Display.serverHandler);

            System.exit(0);
        });


    }

    private class Display {
        private static ExecutorService serverHandler = Executors.newCachedThreadPool();
        private static Lock serverLock = new ReentrantLock(true);

        private static Condition serverCheck = serverLock.newCondition();
        public static int width = 500;
        public static int height = 400;

        public static Scene setTitleScreen() {


            BorderPane root = new BorderPane();


            root.setBackground(Styling.Backgrounds.SKY_BLUE.getBackground());
            VBox center = new VBox();
            center.setSpacing(40);
            center.setAlignment(Pos.CENTER);

            Label title = new Label("Welcome to Tic Tac Toe");
            title.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, 40));
            Display.serverHandler.execute(animatedTitle(title));

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

        public static Scene setServerScreen() {


            VBox root = new VBox();
            root.setAlignment(Pos.CENTER);

            root.setBackground(Styling.Backgrounds.SERVER_BG.getBackground());


            ComboBox<String> serverBox = new ComboBox<>();
            serverBox.setStyle("-fx-text-fill: white;");
            serverBox.setBackground(Styling.Backgrounds.LIGHT_GRAY.getBackground());
            serverBox.setOnAction(e -> {

                System.out.println(Thread.currentThread().getName());
                window.setScene(pickTeam());


            });

            System.out.println("code running on the main thread outside of the setOnAction");
            serverBox.setValue("please select a Server");
            serverBox.setPrefSize(400, 20);


            root.getChildren().add(serverBox);

            Scene scene = new Scene(root, Display.width, Display.height);


            serverHandler.execute(() -> {
                try {
                    Socket socket = new Socket();
                    int i = 0;
                    while (true) {

                        try {
                            Thread.sleep(1000);
                            if (window.getScene() != scene) {
                                break;
                            }

                            while (!socket.isConnected()) {
                                try {
                                    socket = new Socket("localhost", 8000);
                                    toServer = new ObjectOutputStream(socket.getOutputStream());
                                    fromServer = new ObjectInputStream(socket.getInputStream());
                                } catch (SocketException ex) {

                                }
                            }
                            toServer.writeObject(new ServerRequest(ServerRequest.RequestType.GET_INFO));
                            toServer.flush();

                            System.out.println("info executed");
                            InetAddress socketInfo = (InetAddress) fromServer.readObject();
                            Platform.runLater(() -> {
                                if (!serverBox.getItems().contains(socketInfo.toString()))
                                    serverBox.getItems().add(0, socketInfo.toString());


                            });


                        } catch (SocketException ex) {
                            socket = new Socket();
                            serverBox.getItems().clear();
                        }

                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {

                }

            });

            return scene;
        }

        public static Scene setGame(Player player) {

            BorderPane root = new BorderPane();
            root.setPadding(new Insets(30, 30, 30, 30));

            root.setBackground(Styling.Backgrounds.LIGHT_GRAY.getBackground());

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

            Platform.runLater(() -> Display.animatedTitle(title));
            Button reset = new Button("reset board");
            reset.setId("reset-btn");
            reset.setOnAction(e -> {

                new Thread(() -> {

                    try {

                        toServer.writeObject(new ServerRequest(ServerRequest.RequestType.RESET_BOARD));

                        toServer.flush();


                        System.out.println("before reading");

                        //Creating the object that will back the GridPane representing the board
                        Cell[][] cells = (Cell[][]) fromServer.readObject();
                        //Creating the styling for the Transient fields
                        Platform.runLater(() -> {
                            Cell.setCellFunctionality(cells);
                        });


                        Stream.of(cells).forEach(cArr -> System.out.println(Arrays.toString(cArr)));
                        System.out.println("after reading");
                        Platform.runLater(() -> {
                            Cell.cells = cells;
                            Stream.of(Cell.cells).forEach(cArr -> System.out.println(Arrays.toString(cArr)));
                            board.getChildren().clear();
                            for (int row = 0; row < Cell.cells.length; row++) {
                                for (int col = 0; col < Cell.cells.length; col++) {
                                    board.add(Cell.cells[row][col], col, row);
                                }


                            }
                            Stream.of(board.getChildren()).forEach(System.out::println);
                        });
//                        Platform.runLater(() -> {
//                            board.getChildren().clear();
//                            for (int row = 0; row < cells.length; row++) {
//                                for (int col = 0; col < cells[row].length; col++) {
//
//                                    board.add(cells[row][col],col,row);
//                                }
//                            }
//
//                        });

                    } catch (IOException f) {
                        f.printStackTrace();
                    } catch (ClassNotFoundException g) {
                        g.printStackTrace();
                    }

                }).start();


            });

            Pane top = new Pane();
            top.getChildren().addAll(title, reset);

            root.setTop(top);

            root.setCenter(board);

            Scene scene = new Scene(root, Display.width, Display.height);
            scene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
            Cell.windowHeight.bind(scene.heightProperty());
            Cell.windowWidth.bind(scene.widthProperty());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            }));

            window.setOnHidden(e -> {


                try {
                    System.out.println("writing player...");
                    toServer.writeObject(new ServerRequest(ServerRequest.RequestType.DISCONNECT_PLAYER));
                    toServer.flush();

                    toServer.writeObject(player);
                    toServer.flush();
                    Player removedPlayer = (Player) fromServer.readObject();
                    System.out.println("removed " + removedPlayer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                Display.serverHandler.shutdownNow();
                double time = 0.0;
                if (!Display.serverHandler.isTerminated()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                System.out.println(Display.serverHandler);

                System.exit(0);


            });

            return scene;
        }

        public static Scene pickTeam() {


            Label xTeamText = new Label("X");
            Label oTeamText = new Label("O");
            Rectangle oTeam = new Rectangle(0, 0, 250, 500);
            Rectangle xTeam = new Rectangle(250, 0, 250, 500);
            BlockingQueue<ServerRequest> serverQueue = new ArrayBlockingQueue<>(1, true);


            Pane root = new Pane();
            root.setPadding(new Insets(10, 0, 0, 0));

            root.setOnMouseClicked(e -> {

                Point2D ePoint = new Point2D(e.getX(), e.getY());

                if (oTeam.getBoundsInParent().contains(ePoint) && !oTeamText.getText().equals("FULL")) {


                    serverHandler.execute(() -> {
                        try {
                            System.out.println("writing player...");
                            toServer.writeObject(new ServerRequest(ServerRequest.RequestType.ADD_PLAYER));
                            toServer.flush();
                            Player player = new Player(Player.Team.O_TEAM, false);

                            toServer.writeObject(player);
                            toServer.flush();

                            Player playerAdded = (Player) fromServer.readObject();

                            Platform.runLater(() -> {
                                window.setScene(setGame(playerAdded));
                            });

                            System.out.println("Player added: " + playerAdded);

                            serverQueue.put(new ServerRequest(ServerRequest.RequestType.CHECK_PLAYERS));


                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                    });

                } else if (xTeam.getBoundsInParent().contains(ePoint) && !xTeamText.getText().equals("FULL")) {


                    serverHandler.execute(() -> {

                        try {
                            System.out.println("writing player...");
                            toServer.writeObject(new ServerRequest(ServerRequest.RequestType.ADD_PLAYER));
                            toServer.flush();
                            Player player = new Player(Player.Team.X_TEAM, false);
                            Platform.runLater(() -> window.setScene(setGame(player)));


                            toServer.writeObject(player);
                            toServer.flush();
                            Player playerAdded = (Player) fromServer.readObject();
                            System.out.println("player added" + playerAdded);

                            serverQueue.put(new ServerRequest(ServerRequest.RequestType.CHECK_PLAYERS));

                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            });
            Button checkCapacity = new Button("check capacity");

            checkCapacity.setOnAction(e -> {
                try {
                    serverQueue.put(new ServerRequest(ServerRequest.RequestType.CHECK_PLAYERS));

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });

            root.getChildren().addAll(xTeam, xTeamText, oTeam, oTeamText, checkCapacity);

            Scene scene = new Scene(root, 500, 500);

            serverHandler.execute(() -> {

                try {

                    Player[] serverPlayers = {new Player(Player.Team.NO_TEAM, false), new Player(Player.Team.NO_TEAM, false)};
                    serverQueue.put(new ServerRequest(ServerRequest.RequestType.CHECK_PLAYERS));
                    while (true) {
                        Thread.sleep(1000);
                        if (window.getScene() != scene)
                            break;
                        String capacity = "";
                        try {
                            System.out.println("getting players...");
                            toServer.writeObject(serverQueue.take());
                            toServer.flush();

                            serverPlayers = (Player[]) fromServer.readObject();
                            System.out.println(Arrays.toString(serverPlayers));


                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }


                        capacity = "";
                        for (int i = 0; i < serverPlayers.length; i++) {

                            if (serverPlayers[i].getTeam() != Player.Team.NO_TEAM && capacity.matches("[OX]_TAKEN")) {
                                capacity = "FULL";
                            } else if (serverPlayers[i].getTeam() == Player.Team.NO_TEAM && capacity.matches("()|(OPEN)")) {
                                capacity = "OPEN";
                            } else if (serverPlayers[i].getTeam() == Player.Team.X_TEAM) {
                                capacity = "X_TAKEN";
                            } else if (serverPlayers[i].getTeam() == Player.Team.O_TEAM) {
                                capacity = "O_TAKEN";

                            }

                        }
                        //System.out.println(capacity);

                        switch (capacity) {
                            case "OPEN" -> {
                                Platform.runLater(() -> {

                                    oTeam.setFill(Color.BLUE);

                                    xTeam.setFill(Color.RED);

                                    oTeamText.setText("O");
                                    oTeamText.setTranslateX(125 - 40);
                                    oTeamText.setTranslateY(250 - 40);
                                    oTeamText.setFont(FontPresets.REGULAR_LARGE.getFont());

                                    xTeamText.setText("X");
                                    xTeamText.setTranslateX(375 - 40);
                                    xTeamText.setTranslateY(250 - 40);
                                    xTeamText.setFont(FontPresets.REGULAR_LARGE.getFont());
                                });

                            }
                            case "FULL" -> {
                                Platform.runLater(() -> {
                                    oTeam.setFill(Color.GRAY);

                                    xTeam.setFill(Color.GRAY);

                                    oTeamText.setText("FULL");
                                    oTeamText.setTranslateX(125 - 80);
                                    oTeamText.setTranslateY(250 - 40);
                                    oTeamText.setFont(FontPresets.FULL_FONT.getFont());


                                    xTeamText.setText("FULL");
                                    xTeamText.setTranslateX(375 - 80);
                                    xTeamText.setTranslateY(250 - 40);
                                    xTeamText.setFont(FontPresets.FULL_FONT.getFont());
                                });


                            }
                            case "O_TAKEN" -> {

                                Platform.runLater(() -> {
                                    oTeam.setFill(Color.GRAY);

                                    xTeam.setFill(Color.RED);

                                    oTeamText.setText("FULL");
                                    oTeamText.setTranslateX(125 - 80);
                                    oTeamText.setTranslateY(250 - 40);
                                    oTeamText.setFont(FontPresets.FULL_FONT.getFont());


                                    xTeamText.setText("X");
                                    xTeamText.setTranslateX(375 - 40);
                                    xTeamText.setTranslateY(250 - 40);
                                    xTeamText.setFont(FontPresets.REGULAR_LARGE.getFont());
                                });

                            }
                            case "X_TAKEN" -> {

                                Platform.runLater(() -> {
                                    oTeam.setFill(Color.BLUE);

                                    xTeam.setFill(Color.GRAY);

                                    oTeamText.setText("O");
                                    oTeamText.setTranslateX(125 - 40);
                                    oTeamText.setTranslateY(250 - 40);
                                    oTeamText.setFont(FontPresets.REGULAR_LARGE.getFont());

                                    xTeamText.setText("FULL");
                                    xTeamText.setTranslateX(375 - 80);
                                    xTeamText.setTranslateY(250 - 40);
                                    xTeamText.setFont(FontPresets.FULL_FONT.getFont());
                                });


                            }
                            default -> {
                                Platform.runLater(() -> {
                                    oTeam.setFill(Color.WHITE);
                                    oTeamText.setFont(FontPresets.REGULAR_LARGE.getFont());

                                    xTeam.setFill(Color.WHITE);
                                    xTeamText.setFont(FontPresets.REGULAR_LARGE.getFont());
                                    System.out.println("incorrect String or could not get player info");
                                });

                            }
                        }
                    }

                } catch (InterruptedException ex) {
                    System.err.println("blockquote interrupted");
                }


            });


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


    }
}