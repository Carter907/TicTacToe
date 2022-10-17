package com.tacer.tic_tac_toe;

import javafx.event.ActionEvent;

import java.net.*;
import java.util.*;
import java.io.*;

public class ClientHandler implements Runnable {

    private Socket user;


    public ClientHandler(Socket user) {

        this.user = user;

    }

    @Override
    public void run() {

        try {
            //create the from and to streams
            ObjectInputStream fromUser = new ObjectInputStream(user.getInputStream());
            ObjectOutputStream toUser = new ObjectOutputStream(user.getOutputStream());

            while (true) {

                ServerRequest serverRequest = (ServerRequest) fromUser.readObject();

                if (serverRequest.getRequest() == ServerRequest.Request.GET_INFO) {

                    toUser.writeObject(user.getInetAddress());
                }

                toUser.writeObject(serverRequest.fufillRequest());
                toUser.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException f) {

            f.printStackTrace();
        }
    }
}
