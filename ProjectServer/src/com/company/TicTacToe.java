package com.company;

public class TicTacToe
{
    private char[][] game_board =new char[3][3];
    private Player player1;
    private Player player2;

    public TicTacToe(){
        int mone=48;
        for (int i=0;i<3;i++)
        {
            for (int j=0;j<3;j++)
            {
                mone++;
                game_board[i][j]=(char)mone;
            }
        }
        player1=new Player("player1");
        player2=new Player("player2");
    }

    byte[] getBoardInBytes(){
        byte[] boardBytes=new byte[9];
        int mone=0;
        for (int i=0;i<3;i++)
        {
            for (int j=0;j<3;j++)
            {
                boardBytes[mone]=(byte)game_board[i][j];
                mone++;
            }
        }
        return boardBytes;
    }

    Player getPlayer1(){
        return this.player1;
    }

    Player getPlayer2(){
        return this.player2;
    }

    public void setPlayer1(Player player1) {
        this.player1 = new Player(player1);
    }

    public void setPlayer2(Player player2) {
        this.player2 = new Player(player2);
    }

    boolean MyMove(int location, String name) {
        if (location <= 3 && location > 0) {
            if (game_board[0][location - 1]!=location+48) {
                System.out.println("number has already been chosen");
                return false;
            }
            else
                game_board[0][location - 1] = name.equals(player1.getUsername()) ? 'x' : 'y';
        }
        else if (location <= 6&& location>0) {
            if (game_board[1][location - 4]!=location+48) {
                System.out.println("number has already been chosen");
                return false;
            }
            else
                game_board[1][location - 4] = name.equals(player1.getUsername()) ? 'x' : 'y';
        }
        else if (location <= 9&& location>0) {
            if (game_board[2][location - 7]!=location+48) {
                System.out.println("number has already been chosen");
                return false;
            }
            else
                game_board[2][location - 7] = name.equals(player1.getUsername()) ? 'x' : 'y';
        }
        else {
            System.out.println("pls enter number 1-9 (Except for what has already been chosen)");
            return false;
        }
        return true;
    }

    boolean CheckWin(){
        if((game_board[0][0]== game_board[0][1]&& game_board[0][1]== game_board[0][2]) || (game_board[0][0]== game_board[1][0]&& game_board[1][0]== game_board[2][0])) {
            return true;
        }

        if((game_board[2][2]== game_board[2][1]&& game_board[2][1]== game_board[2][0]) || (game_board[2][2]== game_board[1][2]&& game_board[1][2]== game_board[0][2])) {
            return true;
        }

        if((game_board[1][1]== game_board[1][0]&& game_board[1][0]== game_board[1][2]) || (game_board[1][1]== game_board[0][1]&& game_board[0][1]== game_board[2][1])
                || (game_board[1][1]== game_board[0][0]&& game_board[0][0]== game_board[2][2]) || (game_board[1][1]== game_board[2][0]&& game_board[2][0]== game_board[0][2])) {
            return true;
        }
        return false;
    }

    void PrintBoard(){
        for (int i=0;i<3;i++)
        {
            for (int j=0;j<3;j++)
            {
                System.out.print("["+ game_board[i][j]+"]");
            }
            System.out.println();
        }
        System.out.println();
    }

    boolean GameOver() {
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                if (game_board[i][j]!='x' && game_board[i][j]!='y')
                    return false;
            }
        }
        return true;
    }

    void resetBoard(){
        int mone=48;
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                mone++;
                game_board[i][j]=(char)mone;
            }
        }
    }

}