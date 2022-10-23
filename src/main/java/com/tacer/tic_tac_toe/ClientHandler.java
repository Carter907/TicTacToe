package com.tacer.tic_tac_toe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    private Socket user;
    private Server server;

    public ClientHandler(Server server, Socket user) {
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

                server.sendMessage("Server Request from client:" + serverRequest.getRequest());

                if (serverRequest.getRequest().equals("GET_INFO")) {

                    server.sendMessage("info request");

                    toUser.writeObject(userAddress);
                    toUser.flush();
                    continue;
                }
                if (serverRequest.getRequest().equals("DISCONNECT_PLAYER")) {

                    Player player = (Player) fromUser.readObject();

                    Player disconnectPlayer = server.disconnectPlayer(player);

                    server.sendMessage("removing player: " + disconnectPlayer);

                    server.sendMessage(Arrays.toString(server.getplayersConnected()));


                    toUser.writeObject(disconnectPlayer);
                    toUser.flush();
                    continue;
                }
                if (serverRequest.getRequest().equals("ADD_PLAYER")) {

                    Player player = (Player) fromUser.readObject();

                    server.sendMessage("adding player: " + player);

                    player = server.connectPlayer(player);

                    server.sendMessage(Arrays.toString(server.getplayersConnected()));

                    toUser.writeObject(player);
                    toUser.flush();
                    continue;
                }

                System.out.println("before writing");

                toUser.writeObject(serverRequest.fulfillRequest(getServer()));
                toUser.flush();

                System.out.println("after writing");
            }
        } catch (SocketException e) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Server getServer() {
        return this.server;
    }

    private Socket getUser() {
        return this.user;
    }


}
