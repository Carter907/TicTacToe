package com.tacer.tic_tac_toe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

public class ClientHandler implements Runnable {
    private Socket user;
    private Server server;

    private BlockingQueue<ServerRequest> writeRequests = new ArrayBlockingQueue<>(1, true);

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

                ServerRequest serverRequest = (ServerRequest) fromUser.readObject();



                if (serverRequest.getRequest().equals("GET_INFO")) {
                    server.sendMessage("Server Request from client:" + serverRequest.getRequest());
                    server.sendMessage("info request");

                    toUser.writeObject(userAddress);
                    toUser.flush();
                    continue;
                }
                if (serverRequest.getRequest().equals("DISCONNECT_PLAYER")) {
                    server.sendMessage("Server Request from client:" + serverRequest.getRequest());
                    Player player = (Player) fromUser.readObject();

                    Player disconnectPlayer = server.disconnectPlayer(player);

                    server.sendMessage("removing player: " + disconnectPlayer);

                    server.sendMessage(Arrays.toString(server.getPlayersConnected()));


                    toUser.writeObject(disconnectPlayer);
                    toUser.flush();
                    continue;
                }
                if (serverRequest.getRequest().equals("ADD_PLAYER")) {
                    server.sendMessage("Server Request from client:" + serverRequest.getRequest());
                    Player player = (Player) fromUser.readObject();

                    server.sendMessage("adding player: " + player);

                    player = server.connectPlayer(player);

                    server.sendMessage(Arrays.toString(server.getPlayersConnected()));

                    toUser.writeObject(player);
                    toUser.flush();
                    continue;
                }
                if (serverRequest.getRequest().equals("SEND_BOARD")) {
                    server.sendMessage("Client is requesting to send board (" + serverRequest.getRequest()+ ")");

                    String[][] cells = (String[][]) fromUser.readObject();

                    System.out.println("object Received: ");
                    Stream.of(cells).forEach(cArr -> System.out.println(Arrays.toString(cArr)));
                    cells = server.checkCells(cells);

                    StringBuilder boardToSend = new StringBuilder("");

                    Stream.of(cells).forEach(c -> boardToSend.append(" " +Arrays.toString(c)));

                    server.sendMessage("Sending board to client: " + boardToSend);

                    toUser.writeObject(cells);
                    toUser.flush();
                    continue;
                }



                Object serverData = serverRequest.fulfillRequest(server);
                toUser.writeObject(serverData);
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
