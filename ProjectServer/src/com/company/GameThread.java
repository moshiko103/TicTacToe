package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

import static com.company.Main.usersFile;

public class GameThread extends Thread {

    private Socket[] sockets;
    private Player[] players;
    private String gameId;
    private TicTacToe game;
    private String currentTurn,firstTurn;

    public GameThread(Socket[] sockets,Player[] players,String gameId) {  // constructor
        this.sockets = sockets;
        this.players=players;
        this.gameId=gameId;
        game=new TicTacToe();
    }

    @Override
    public void run() {
        try{
            OutputStream p1OutputStream= sockets[0].getOutputStream();
            OutputStream p2OutputStream= sockets[1].getOutputStream();
            InputStream p1InputStream= sockets[0].getInputStream();
            InputStream p2InputStream= sockets[1].getInputStream();

            game.setPlayer1(players[0]);
            game.setPlayer2(players[1]);

            p1OutputStream.write(new byte[]{1}); // tells player1 opponent found and game begins (Client reading Line:49)
            p2OutputStream.write(new byte[]{1}); // tells player1 opponent found and game begins  (Client reading Line:49)

            ///////////////////////////////////  game begins  /////////////////////////////////////////

            sendNamesBetweenPlayers(p2OutputStream,game.getPlayer1()); // players get details about their opponent
            sendNamesBetweenPlayers(p1OutputStream,game.getPlayer2());

            byte[] buffer=new byte[4];
            currentTurn=randomFirstTurn();
            boolean playersInGame=true;

            ////////////////////////////////////  game loop  /////////////////////////////////////////
            while (playersInGame){
                if (currentTurn.equals("p1")){ //  player1 turn
                    ByteBuffer.wrap(buffer).putInt(100);
                    p1OutputStream.write(buffer);
                    ByteBuffer.wrap(buffer).putInt(101);
                    p2OutputStream.write(buffer);
                    //System.out.println("It's " + game.getPlayer1().getUsername() + "'s turn");
                    executePlayerMove(p1InputStream,p1OutputStream,game.getPlayer1().getUsername(),p2OutputStream);
                    //game.PrintBoard();
                    sendBoardToPlayers(p1OutputStream,p2OutputStream);
                    boolean win=checkWinner(p1OutputStream,p2OutputStream);
                    if (win || game.GameOver()){
                        checkRematch(p1InputStream,p2InputStream,p1OutputStream,p2OutputStream);
                    }
                    else {
                        currentTurn = "p2";
                    }
                }

                else {   // player2 turn
                    ByteBuffer.wrap(buffer).putInt(100);
                    p2OutputStream.write(buffer);
                    ByteBuffer.wrap(buffer).putInt(101);
                    p1OutputStream.write(buffer);
                    //System.out.println("It's " + game.getPlayer2().getUsername() + "'s turn");
                    executePlayerMove(p2InputStream,p2OutputStream,game.getPlayer2().getUsername(),p1OutputStream);
                    //game.PrintBoard();
                    sendBoardToPlayers(p1OutputStream,p2OutputStream);
                    boolean win=checkWinner(p1OutputStream,p2OutputStream);
                    if (win || game.GameOver()){
                        checkRematch(p1InputStream,p2InputStream,p1OutputStream,p2OutputStream);
                    }
                    else {
                        currentTurn = "p1";
                    }
                }
            }
            ////////////////////////////////////  game loop ends  /////////////////////////////////////////


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("player disconnected,game interrupted!! waiting for new players... (game ID: "+gameId+")");
            interrupt();
        }finally {

            if (sockets[0] != null) {
                try {
                    sockets[0].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sockets[1] != null) {
                try {
                    sockets[1].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendNamesBetweenPlayers(OutputStream opponentOutputstream,Player player) throws IOException {
        byte[] buffer=new byte[4];
        ByteBuffer.wrap(buffer).putInt(player.getBytes().length);
        opponentOutputstream.write(buffer); // send the length of current player in bytes (Client reading Line:61)
        opponentOutputstream.write(player.getBytes()); // send object of player details (Client reading Line:63)
    }

    private String randomFirstTurn() {
        int x=(Math.random()<0.5) ? 0 : 1;
        if (x==0)
        {
            firstTurn="p1";
            return "p1";
        }
        else {
            firstTurn="p2";
            return "p2";
        }
    }

    private void executePlayerMove(InputStream playerInputStream, OutputStream playerOutputstream,String playerName,OutputStream opponentOutputstream)throws IOException{
        byte[] buffer=new byte[4];
        int playerMove;
        playerInputStream.read(buffer); // get the player's move (Client writing Line:96)
        playerMove=ByteBuffer.wrap(buffer).getInt();
        game.MyMove(playerMove,playerName);

        ByteBuffer.wrap(buffer).putInt(900);
        playerOutputstream.write(buffer);  // send "code 900" to players after move executed (Client reading Lines:98,120)
        opponentOutputstream.write(buffer);  // send "code 900" to players after move executed (Client reading Lines:98,120)
    }

    private void sendBoardToPlayers(OutputStream p1Outputstream,OutputStream p2Outputstream) throws IOException{
        p1Outputstream.write(game.getBoardInBytes()); // send updated game board in bytes to players (Client reading Lines:101,122)
        p2Outputstream.write(game.getBoardInBytes()); // send updated game board in bytes to players (Client reading Lines:101,122)
    }

    private boolean checkWinner (OutputStream p1Outputstream,OutputStream p2Outputstream) throws IOException{
        byte[] buffer=new byte[4];
        if (game.CheckWin()){
            if (currentTurn.equals("p1")){
                ByteBuffer.wrap(buffer).putInt(500); // 500 indicate win (Client reading Lines:105,126)
                p1Outputstream.write(buffer);
                ByteBuffer.wrap(buffer).putInt(501); // 501 indicate lose (Client reading Lines:105,126)
                p2Outputstream.write(buffer);
                firstTurn="p1";
                game.getPlayer1().reachWin();
                game.getPlayer2().gotDefeat();
                //System.out.println(game.getPlayer1().getUsername()+ " is the winner!");
            }
            else {
                ByteBuffer.wrap(buffer).putInt(500); // 500 indicate win (Client reading Lines:105,126)
                p2Outputstream.write(buffer);
                ByteBuffer.wrap(buffer).putInt(501); // 501 indicate lose (Client reading Lines:105,126)
                p1Outputstream.write(buffer);
                firstTurn="p2";
                game.getPlayer2().reachWin();
                game.getPlayer1().gotDefeat();
                //System.out.println(game.getPlayer2().getUsername()+ " is the winner!");
            }
            return true;
        }
        ByteBuffer.wrap(buffer).putInt(502);  // 502 indicate no one wins yet (Client reading Lines:105,126)
        p1Outputstream.write(buffer);
        p2Outputstream.write(buffer);
        return false;
    }

    private void checkRematch(InputStream p1Inputstream, InputStream p2Inputstream,OutputStream p1Outputstream,OutputStream p2Outputstream) throws IOException{
        byte[] buffer=new byte[4];
        p1Inputstream.read(buffer);  // get player1 answer about rematch (Client writing Line:226)
        int p1Answer=ByteBuffer.wrap(buffer).getInt();
        p2Inputstream.read(buffer);  // get player2 answer about rematch (Client writing Line:226)
        int p2Answer=ByteBuffer.wrap(buffer).getInt();

        if (p1Answer==200 && p2Answer==200){
            ByteBuffer.wrap(buffer).putInt(700);  // 700 - rematch code
            p1Outputstream.write(buffer); // send rematch code to players (Client reading Line:230)
            p2Outputstream.write(buffer); // send rematch code to players (Client reading Line:230)

            if (game.GameOver()){
                firstTurn= firstTurn.equals("p1")? "p2" : "p1";
                currentTurn=firstTurn;
                System.out.println("Its a tied!");
            }
            //System.out.println("Players want rematch!");
            game.resetBoard();
            //System.out.println("new game begin");
        }
        else {
            if (p1Answer == 200) {
                //System.out.println(game.getPlayer1().getUsername() + " disconnect");
                ByteBuffer.wrap(buffer).putInt(701);
                p1Outputstream.write(buffer);  // send to player his opponrnt left(code 701) (Client reading Line:230)
            } else if (p2Answer == 200) {
                //System.out.println(game.getPlayer2().getUsername() + " disconnect");
                ByteBuffer.wrap(buffer).putInt(701);
                p2Outputstream.write(buffer);  // send to player his opponrnt left(code 701) (Client reading Line:230)
            }
            savePlayersUpdatedDetails();  // save all updated details of the players before game interrupt
        }
    }

    private void savePlayersUpdatedDetails() {
        try {
            File copyTemp = new File("temp.txt");
            PrintWriter copyOutput = new PrintWriter(new FileOutputStream(copyTemp, true));

            Scanner input = new Scanner(usersFile);

            while (input.hasNextLine()) {
                String[] currentDetails = input.nextLine().split(",");
                if (game.getPlayer1().getUsername().equals(currentDetails[0])) {
                    String playerDetails = currentDetails[0] + "," + currentDetails[1] + "," + game.getPlayer1().getWins() + "," + game.getPlayer1().getLoses() + "," + game.getPlayer1().getRank();
                    copyOutput.println(playerDetails);
                } else if (game.getPlayer2().getUsername().equals(currentDetails[0])) {
                    String playerDetails = currentDetails[0] + "," + currentDetails[1] + "," + game.getPlayer2().getWins() + "," + game.getPlayer2().getLoses() + "," + game.getPlayer2().getRank();
                    copyOutput.println(playerDetails);
                } else {
                    String playerDetails = currentDetails[0] + "," + currentDetails[1] + "," + currentDetails[2] + "," + currentDetails[3] + "," + currentDetails[4];
                    copyOutput.println(playerDetails);
                }
            }
            copyOutput.close();
            input.close();
            refillUsersFile(copyTemp);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refillUsersFile(File sourceFile) throws IOException {
        Scanner input = new Scanner(sourceFile);
        PrintWriter output = new PrintWriter(new FileOutputStream(usersFile));
        output.print(""); // clean the file before put the updated details
        output.close();

        output=new PrintWriter(new FileOutputStream(usersFile,true));

        while (input.hasNextLine()) {
            output.println(input.nextLine());
        }
        output.close();
        input.close();
        sourceFile.delete();
        System.out.println("game results has been saved! (game ID: "+gameId+")");
    }

}
