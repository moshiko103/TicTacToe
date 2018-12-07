package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 3000;

    private static char[][] gameBoard;
    private static Player player;

    public static void main(String[] args) {

        Scanner scanner=new Scanner(System.in);
        Socket socket=null;
        InputStream inputStream=null;
        OutputStream outputStream=null;
        byte[] buffer=new byte[4];

        try {
            socket = new Socket(HOST, PORT);
            inputStream=socket.getInputStream();
            outputStream=socket.getOutputStream();

            inputStream.read(buffer);  // get from server if accepted(200) or rejected(201) ( Server writing Main Lines:32,43 )
            int spotRef=ByteBuffer.wrap(buffer).getInt();
            if (spotRef==201) {
                System.out.println("sorry, server deals with many requests.. pls try again in a few seconds");
                System.exit(0);
            }

            welcomeDialog(outputStream,inputStream,scanner); // method contains the login and signup process
            Sleep(1000);
            System.out.println("Hello "+ player.getUsername()+", we're looking for opponent... \n");
            Sleep(1500);
            System.out.println("Just to remind you: \n" +
                               "Your rank: "+ GameSupport.rankString(player.getRank()) + "\n" +
                               "Won: " + player.getWins() + " times \n" +
                               "Defeated: " + player.getLoses() + " times \n");

            byte[] opponentConeccted = new byte[1];
            inputStream.read(opponentConeccted);  // get response when server find opponent (Server writing GameThread Lines:36,37)

            if(opponentConeccted[0]==1) {
                Sleep(3000);
                System.out.println("opponent found, game is about to begin");
            }

            /////////////////////////////////  game begins ////////////////////////////////////

            String nickname=player.getUsername();

            buffer=new byte[4];
            inputStream.read(buffer); // get the length of opponent details (Server writing GameThread Line:115)
            byte[] playerInBytes= new byte[ByteBuffer.wrap(buffer).getInt()]; //build array with the this length
            inputStream.read(playerInBytes); // get opponent details (Server writing GameThread Line:116)
            Player opponent=new Player(playerInBytes);

            Sleep(1500);
            System.out.println(nickname+ ", your opponent is: "+opponent.getUsername() + "(rank: " + GameSupport.rankString(opponent.getRank()) + ") \n");

            Sleep(1500);
            System.out.println("a little guide before starting,here the locations of the gameboard:");
            GameSupport.PrintGuide();
            Sleep(1500);
            System.out.println("good luck " + nickname +"! game begins \n");
            Sleep(1500);

            boolean itsMyTurn;
            gameBoard=GameSupport.createNewBoard();
            String myMove;

            ///////////////////////////////////////// game loop ///////////////////////////////////////////
            while (!socket.isClosed() && socket.isConnected()){
                inputStream.read(buffer);  // get if it my turn (100) or opponent turn (101) (Server writing GameThread Lines:51,53,69,71)
                if (ByteBuffer.wrap(buffer).getInt()!=100 && ByteBuffer.wrap(buffer).getInt()!=101){
                    System.out.println("sry,"+ opponent.getUsername() +" left.. pls try again another server");  // if get int != 100||101, socket probably closed or stream corrupted
                    System.exit(0);
                }
                itsMyTurn= ByteBuffer.wrap(buffer).getInt() == 100;

                if (itsMyTurn){ //  playing my turn
                    System.out.println(nickname+" it's your turn, choose your move");
                    do {
                        myMove=scanner.next();
                    }while (!GameSupport.checkIfLocationApproved(gameBoard,myMove));

                    ByteBuffer.wrap(buffer).putInt(Integer.valueOf(myMove));
                    outputStream.write(buffer); // after my move approved send it to server (Server reading GameThread Line:135)

                    inputStream.read(buffer);  // waiting for "code 900" from server after move executed (Server writing GameThread Lines:140,141)
                    if (ByteBuffer.wrap(buffer).getInt()==900) {
                        byte[] bytesGameBoard = new byte[9];
                        inputStream.read(bytesGameBoard);  // get the updated board in bytes (Server writing GameThread Lines:145,146)
                        gameBoard = GameSupport.buildBoardFromBytes(bytesGameBoard);
                        GameSupport.printGameBoard(gameBoard);

                        inputStream.read(buffer); // get 500 || 501 if there is a winner, else 502 (Server writing GameThread Lines:154,156,164,166,174)
                        if (ByteBuffer.wrap(buffer).getInt() == 500) {
                            System.out.println("Congratulations "+nickname+ " you win!");
                            checkRematch(outputStream,inputStream,opponent.getUsername(),scanner);

                        } else if (GameSupport.gameOver(gameBoard)) {
                            System.out.println("its a tied! maybe next time");
                            checkRematch(outputStream,inputStream,opponent.getUsername(),scanner);
                        }
                    }

                }
                else {   // do in opponent side
                    System.out.println("It's " + opponent.getUsername() + "'s turn, wait for his move..");
                    inputStream.read(buffer);  // waiting for "code 900" from server after move executed (Server writing GameThread Lines:140,141)
                    if (ByteBuffer.wrap(buffer).getInt()==900){
                        byte[] bytesGameBoard=new byte[9];
                        inputStream.read(bytesGameBoard);  // get the updated board in bytes (Server writing GameThread Lines:145,146)
                        gameBoard=GameSupport.buildBoardFromBytes(bytesGameBoard);
                        GameSupport.printGameBoard(gameBoard);

                        inputStream.read(buffer);  // get 500 || 501 if there is a winner, else 502 (Server writing GameThread Lines:154,156,164,166,174)
                        if (ByteBuffer.wrap(buffer).getInt()==501){
                            System.out.println("Ohhh you lose!");
                            checkRematch(outputStream,inputStream,opponent.getUsername(),scanner);
                        }
                        else if (GameSupport.gameOver(gameBoard)){
                            System.out.println("its a tied! maybe next time");
                            checkRematch(outputStream,inputStream,opponent.getUsername(),scanner);
                        }
                    }
                    else {
                        System.out.println("sry,"+ opponent.getUsername() +" left.. pls try again another server");
                        System.exit(0);
                    }
                }
            }
            ///////////////////////////////////////// game loop ends ///////////////////////////////////////////

            } catch (ConnectException e) {
            //e.printStackTrace();
            System.out.println("server is down.. come back later");
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("sry,opponrnt left.. pls try again another server");
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    ////////////////////////////////////////// Welcome method //////////////////////////////////////////////
    private static void welcomeDialog(OutputStream outputStream,InputStream inputStream,Scanner scanner) throws IOException{
        byte[] buffer=new byte[4];
        System.out.println("Welcome to Tic Tac Toe! \n" +
                "1. Login \n" +
                "2. Sign-up");

        boolean tempFlag=false;
        String answer;
        do {
            if (tempFlag)
                System.out.println("pls enter 1 or 2");
            answer=scanner.next();
            tempFlag=true;
        }while (!answer.equals("1") && !answer.equals("2"));

        tempFlag=false;
        do {
            if (tempFlag) {
                if (answer.equals("1"))
                    System.out.println("login faild! you entered wrong details, pls try again");
                else
                    System.out.println("sign up faild! username already exist");
            }
            System.out.println("Enter username:");
            String username = scanner.next();
            System.out.println("Enter password:");
            String password = scanner.next();

            String playerDetails = answer + "," + username + "," + password;
            ByteBuffer.wrap(buffer).putInt(playerDetails.length());
            outputStream.write(buffer); // write length of the string (Server reading Main Line:71)
            outputStream.write(playerDetails.getBytes());  // write string player details (Server reading Main Line:73)

            tempFlag=true;
            inputStream.read(buffer); // get response from server if login/signup process succeed(300) or faild(301) (Server writing Main Lines:124,139,155,164)
        }while (ByteBuffer.wrap(buffer).getInt()!=300);

        System.out.println("you have successfully " + (answer.equals("1") ? "logged in!" : "singed up!"));

        inputStream.read(buffer); // get length of the player object (Server writing Main Lines:171,131)
        byte[] playerInBytes=new byte[ByteBuffer.wrap(buffer).getInt()];
        inputStream.read(playerInBytes); // get the player object in bytes (Server writing Main Lines:172,132)
        player=new Player(playerInBytes);
    }

    ////////////////////////////////////////// Rematch method //////////////////////////////////////////////
    private static void checkRematch(OutputStream outputStream,InputStream inputStream,String opponentNick, Scanner scanner) throws IOException{
        byte[] buffer=new byte[4];
        int answer=GameSupport.playAgainDialog(scanner) ? 200 : 201; // 200- want rematch  201- dont
        ByteBuffer.wrap(buffer).putInt(answer);
        outputStream.write(buffer); // send if want rematch or not (Server reading GameThread Line:182,184)
        if (answer==200) {
            System.out.println("waiting for " + opponentNick + "'s decision...");
            Sleep(3000);
            inputStream.read(buffer); // waiting to get opponent rematch response (Server writing GameThread Line:189,190)
            if (ByteBuffer.wrap(buffer).getInt() == 700) {    // if opponent wants rematch too..
                gameBoard=GameSupport.createNewBoard();
                System.out.println(opponentNick + " accepted the challenge, new game begins! good luck");
                Sleep(1000);
            }
            else {
                System.out.println(opponentNick+" left.. try to find opponent in another server, see ya!");
                System.exit(0);
            }
        }
        else {
            System.out.println("Thank you for playing, see ya!");
            System.exit(0);
        }
    }

    private static void Sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
