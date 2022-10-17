package com.tacer.tic_tac_toe;

import javafx.event.*;

import java.io.*;

public class ServerRequest implements Serializable {

    public enum Request implements Serializable{
        RESET_BOARD,
        GET_INFO,
        ADD_X,
        ADD_O;
    }

    public Request getRequest() {
        return request;
    }

    public ActionEvent getEvent() {
        return event;
    }

    private Request request;
    private ActionEvent event;

    public ServerRequest(ActionEvent event, Request request) {
        this.request = request;
        this.event = event;
    }
    public ServerRequest(Request request) {
        this.request = request;

    }

    public Object fufillRequest() {


        switch (request) {

            case RESET_BOARD -> {
                return resetBoard();

            }

        }
        return new Object();
    }

    private static Object resetBoard() {
        Cell[][] board = Cell.cells;


        for (Cell[] cellRow : board) {
            for (Cell c : cellRow) {
                c.setValue(null);
                c.setText(null);

            }
        }
        return board;

    }
}
