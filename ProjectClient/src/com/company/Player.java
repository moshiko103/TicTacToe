package com.company;

import java.nio.ByteBuffer;

public class Player {
    private String username;
    private int wins;
    private int loses;
    private char rank;

    public Player(String username) {
        this.username = username;
        this.wins=0;
        this.loses=0;
        this.rank='A';
    }

    public Player(String username,int wins,int loses,char rank) {
        this.username = username;
        this.wins=wins;
        this.loses=loses;
        this.rank=rank;
    }

    public Player(Player player) {
        this.username =player.username;
        this.wins=player.wins;
        this.loses=player.loses;
        this.rank=player.rank;
    }

    public Player(byte[] playerInBytes) {
        int usernameLength=playerInBytes.length-10;
        byte[] temp=new byte[usernameLength];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = playerInBytes[i];
        }
        this.username=new String(temp);
        this.wins=ByteBuffer.wrap(playerInBytes).getInt(usernameLength);
        this.loses=ByteBuffer.wrap(playerInBytes).getInt(usernameLength+4);
        this.rank=ByteBuffer.wrap(playerInBytes).getChar(usernameLength+8);
    }

    public int getWins() {
        return this.wins;
    }

    public void reachWin() {
        this.wins += 1;
        setRank();
    }

    public int getLoses() {
        return this.loses;
    }

    public void gotDefeat() {
        this.loses += 1;
        setRank();
    }

    public char getRank() {
        return rank;
    }

    public void setRank() {
        int result=this.wins-this.loses;
        if (result<=0)
            this.rank='A';
        else if (result<=3)
            this.rank='N';
        else if (result<=6)
            this.rank='P';
        else
            this.rank='L';
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public byte[] getBytes(){
        byte[] temp=this.username.getBytes();
        int length=temp.length+1+4+4+1;
        byte[] bytes=new byte[length];
        for (int i = 0; i < temp.length; i++) {
            bytes[i] = temp[i];
        }
        ByteBuffer.wrap(bytes).putInt(temp.length,this.wins);
        ByteBuffer.wrap(bytes).putInt(temp.length+4,this.loses);
        ByteBuffer.wrap(bytes).putChar(temp.length+8,this.rank);
        return bytes;
    }
}
