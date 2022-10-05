package com.tacer.tic_tac_toe;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.*;
import javafx.scene.paint.*;

import java.util.regex.Pattern;

public class Cell extends Label {

    public static DoubleProperty windowHeight = new SimpleDoubleProperty();
    public static DoubleProperty windowWidth = new SimpleDoubleProperty();

    public static boolean turn = true;
    private String value;

    public static Cell[][] cells = new Cell[3][3];

    public Cell(int row, int col) {

        super("");

        cells[row][col] = this;
        this.prefHeightProperty().bind(windowHeight);
        this.prefWidthProperty().bind(windowWidth);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("cell");


        this.setOnMouseClicked(e -> {

            if (turn && this.getValue() == null) {
                this.setValue("X");
                this.setFont(Font.font(Font.getFamilies().get(17), FontWeight.BOLD, this.getPrefHeight() / 6));
                this.setText(this.getValue());
                this.setTextFill(Color.RED);
                Cell.turn = false;
                checkCells();

            } else if (this.getValue() == null) {
                this.setValue("O");

                this.setFont(Font.font(Font.getFamilies().get(17), FontWeight.BOLD, this.getPrefHeight() / 6));
                this.setText(this.getValue());
                this.setTextFill(Color.BLUE);
                Cell.turn = true;
                checkCells();
            }

        });
    }

    public static boolean checkCells() {

        Pattern xWon = Pattern.compile("(XXX......)|(X...X...X)|(..X.X.X..)|(.X..X..X.)|(X..X..X..)|(...XXX...)|(......XXX)|(..X..X..X)");
        Pattern oWon = Pattern.compile("(OOO......)|(O...O...O)|(..O.O.O..)|(.O..O..O.)|(O..O..O..)|(...OOO...)|(......OOO)|(..O..O..O)");
        String won = "";
        for (int i = 0; i < 9; i++) {
            if (cells[i / 3][i % 3].getValue() == null)
                won = won + "n";
            else
                won = won + cells[i / 3][i % 3].getValue();

        }
        System.out.println(won);
        if (won.matches(xWon.pattern())) {

            endScreen("X");

            return true;
        } else if (won.matches(oWon.pattern())) {

            endScreen("O");

            return true;
        }
        return false;
    }

    private static void endScreen(String won) {

        Label endTitle = new Label(won + " won in Tic Tac Toe!");

        StackPane endScreen = new StackPane(endTitle);

        endTitle.setFont(Font.font(Font.getFamilies().get(17), FontWeight.BOLD, 20));
        endTitle.setTextFill(Color.GREEN);

        Scene scene = new Scene(endScreen, 300, 300);
        Main.window.setScene(scene);


    }

    public String getValue() {

        return this.value;
    }

    public void setValue(String value) {

        this.value = value;
    }
}
