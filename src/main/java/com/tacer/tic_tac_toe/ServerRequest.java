package com.tacer.tic_tac_toe;

import javafx.event.ActionEvent;

import java.io.Serializable;

public class ServerRequest implements Serializable {

    public enum Request {
        RESET_BOARD,
        GET_INFO,
        ADD_X,
        ADD_O;
    }

    private String request;
    private ActionEvent event;

    public ServerRequest(ActionEvent event, Request request) {
        this.request = request.toString();
        this.event = event;
    }

    public ServerRequest(Request request) {
        this.request = request.toString();

    }

    public ServerRequest(String request) {

        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public ActionEvent getEvent() {
        return event;
    }

    public Object fulfillRequest(Server serverToFulfill) {


        switch (request) {

            case "RESET_BOARD" -> {
                return resetBoard(serverToFulfill);

            }

        }
        System.out.println(request + " didn't match");
        return new Object();
    }

    private Cell[][] resetBoard(Server server) {

        for (Cell[] cArr : Cell.cells) {
            for (Cell c : cArr) {

                c.setValue(null);
                c.setText(null);
            }
        }

        server.getBoard().getChildren().forEach(c -> {
            Cell cell = (Cell) c;
            cell.setValue(null);
            cell.setText(null);
        });


        System.out.println(Cell.cells);


        return Cell.cells;

    }
}
