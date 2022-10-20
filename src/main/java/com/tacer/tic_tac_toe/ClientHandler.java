package com.tacer.tic_tac_toe;

import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {

    private Socket user;
    private Server server;


    public ClientHandler(Server server,Socket user) {

        this.user = user;
        this.server = server;

    }

    @Override
    public void run() {

        try {
            //create the from and to streams
            ObjectInputStream fromUser = new ObjectInputStream(user.getInputStream());
            ObjectOutputStream toUser = new ObjectOutputStream(user.getOutputStream());
            InetAddress userAddress = user.getInetAddress();
            while (true) {
                System.out.println("before reading");

                ServerRequest serverRequest = (ServerRequest) fromUser.readObject();

                System.out.println("after reading");

                System.out.println(serverRequest.getRequest());

                if (serverRequest.getRequest().equals("GET_INFO")) {
                    System.out.println("info request");
                    toUser.writeObject(userAddress);
                    toUser.flush();
                    continue;
                }

                System.out.println("before writing");

                toUser.writeObject(serverRequest.fulfillRequest(getServer()));
                toUser.flush();

                System.out.println("after writing");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException f) {

            f.printStackTrace();
        }
    }

    private Server getServer() {
        return this.server;
    }
    private Socket getUser() {
        return this.user;
    }


}
