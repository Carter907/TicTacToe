package com.tacer.tic_tac_toe;

import java.io.Serializable;

public class Player implements Serializable {


    public void setConnection(boolean connection) {
        this.connected = true;
    }

    public enum Team {

        X_TEAM,
        NO_TEAM,
        ERR_TEAM,
        O_TEAM;
    }
    private boolean connected;
    private Team team;

    public Player(Team team, boolean connected) {
        this.team = team;
        this.connected = connected;

    }


    public boolean isConnected() {
        return this.connected;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public String toString() {
        return this.getTeam().name() + " " + this.isConnected();
    }

}
