package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main {

    public static final int PORT = 3000;
    private static Socket[] sockets =new Socket[2];
    private static Player[] players =new Player[2];
    public static File usersFile=new File("users.txt");
    public static int playersCount = 0;
    public static int finishThreadCounter=0;
    private static int gamesCounter=1;
    private static String gameId=makeGameId(gamesCounter);

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            Socket socket;
            byte[] buffer=new byte[4];

            while (true) {
                if (playersCount < 2) {
                    System.out.println("waiting for player" + (playersCount+1) + " (game ID: "+gameId+")");
                    sockets[playersCount] = serverSocket.accept();
                    ByteBuffer.wrap(buffer).putInt(200);
                    sockets[playersCount].getOutputStream().write(buffer);  // 200 tells to client he accepted by server ( Client reading Line:32 )
                    System.out.println("player" + (playersCount+1) + " connected (game ID: "+gameId+")");

                    int constCount=playersCount;
                    welcomeThread(sockets[constCount].getInputStream(), sockets[constCount].getOutputStream(),constCount);  // this method start thread to this specific client

                    playersCount++;
                }
                else{
                    socket = serverSocket.accept();
                    ByteBuffer.wrap(buffer).putInt(201);
                    socket.getOutputStream().write(buffer); // 201 tells to client he rejected by server ( Client reading Line:32 )
                    socket.close();
                    System.out.println("new player blocked");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //////////////////////////// do in the backgroud all the login/signup dialog with the clients /////////////////////////////
    private static void welcomeThread(InputStream inputStream,OutputStream outputStream,int playerNum) {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag = false; // becomes true- if login/signup succeed
                byte[] buffer = new byte[4];
                try {
                    do {
                        inputStream.read(buffer); // read length of string ( Client writing Line:214 )
                        byte[] details = new byte[ByteBuffer.wrap(buffer).getInt()];
                        inputStream.read(details);  // read string player details ( Client writing Line:215)
                        String playerDetails = new String(details);
                        String[] arrDetails = playerDetails.split(",");  // arrDetails[0]-client answer login/signout, arrDetails[1]-client username, arrDetails[2]-client password
                        switch (arrDetails[0]) {
                            case "1":
                                flag = loginPlayer(arrDetails[1], arrDetails[2], outputStream,playerNum);
                                break;

                            case "2":
                                flag = signupPlayer(arrDetails[1], arrDetails[2], outputStream,playerNum);
                                break;
                        }
                    } while (!flag);
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.out.println("player disconnected in login/signup progress!!! waiting for new player... (game ID: "+gameId+")");
                    if (players[0]==null && players[1]!=null){
                        players[0]=players[1];
                        sockets[0]=sockets[1];
                    }
                    finishThreadCounter--;
                    playersCount--;
                }
                finishThreadCounter++; // count the sockets that finished login thread

                if (playersCount==2&& finishThreadCounter==2) {
                    System.out.println("players connected! starting game...(game ID: "+gameId+")");
                    Thread gameplay=new GameThread(sockets,players,gameId);  // create the game thread
                    gameplay.start();
                    gamesCounter++;
                    gameId=makeGameId(gamesCounter);

                    players=new Player[2];  // reset arrays and counters for new game
                    sockets=new Socket[2];
                    playersCount=0;
                    finishThreadCounter=0;
                }
            }
        });
        thread.start();
    }

    ////////////////////////////////////////  login method  /////////////////////////////////////////
    private static boolean loginPlayer(String username,String password,OutputStream outputStream,int playerNum) throws IOException{

        Scanner input=new Scanner(usersFile);
        byte[] buffer=new byte[4];

        while (input.hasNextLine()) {
            String[] currentDetails = input.nextLine().split(",");
            if (currentDetails[0].equals(username) && currentDetails[1].equals(password)){
                ByteBuffer.wrap(buffer).putInt(300);
                outputStream.write(buffer);    //  300- say to client login succes ( Client reading Line:218 )
                System.out.println("player"+ (playerNum+1) +" login succeed (game ID: "+gameId+")");

                Player player=new Player(currentDetails[0],Integer.valueOf(currentDetails[2]),Integer.valueOf(currentDetails[3]),currentDetails[4].charAt(0));
                byte[] bytesPlayer=player.getBytes();
                ByteBuffer.wrap(buffer).putInt(bytesPlayer.length);
                outputStream.write(buffer);  // send to client the length of "bytesPlayer" (Client reading Line:223)
                outputStream.write(bytesPlayer); // send to client his saved details (Client reading Line:225)

                players[playerNum]=new Player(player);

                return true;
            }
        }
        ByteBuffer.wrap(buffer).putInt(301);
        System.out.println("player"+ (playerNum+1) +" login faild! wrong details (game ID: "+gameId+")");
        outputStream.write(buffer);   //  301- say to client login faild ( Client reading Line:218 )
        return false;
    }

    ////////////////////////////////////////  signup method  /////////////////////////////////////////
    private static boolean signupPlayer(String username, String password, OutputStream outputStream,int playerNum) throws IOException{

        Scanner input=new Scanner(usersFile);  // create scanner to read from "usersFile"
        PrintWriter output=new PrintWriter(new FileOutputStream(usersFile, true));  //create printWriter to write into "usersFile"
        byte[] buffer=new byte[4];

        while (input.hasNextLine()) {
            String[] currentDetails = input.nextLine().split(",");
            if (currentDetails[0].equals(username)){
                ByteBuffer.wrap(buffer).putInt(301);
                outputStream.write(buffer);  //  301- say to client username already exist  ( Client reading Line:218 )
                System.out.println("player"+ (playerNum+1) +" signup faild! username already exist (game ID: "+gameId+")");
                return false;
            }
        }
        String playerDetails=username + "," + password + "," + "0" + "," + "0" + "," + "A";
        output.println(playerDetails);
        output.close();
        ByteBuffer.wrap(buffer).putInt(300);  //  300- say to client signup succes ( Client reading Line:218 )
        outputStream.write(buffer);
        System.out.println("player"+ (playerNum+1) +" signup succeed (game ID: "+gameId+")");

        Player player=new Player(username);
        byte[] bytesPlayer=player.getBytes();
        ByteBuffer.wrap(buffer).putInt(bytesPlayer.length);
        outputStream.write(buffer);  // send to client the length of "bytesPlayer" (Client reading Line:223)
        outputStream.write(bytesPlayer); // send to client his saved details (Client reading Line:225)

        players[playerNum]=new Player(player);

        return true;
    }

    private static String makeGameId(int counter){
        return "#"+counter;
    }

}
