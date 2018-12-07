package com.company;

import java.util.Scanner;

public class GameSupport {

    public static void PrintGuide() {
        int mone = 0;
        String[][] game_board = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mone++;
                game_board[i][j] = "" + mone;
                System.out.print("[" + game_board[i][j] + "]");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static char[][] createNewBoard() {
        char[][] board = new char[3][3];
        int mone = 48;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mone++;
                board[i][j] = (char)mone;
            }
        }
        return board;
    }

    public static char[][] buildBoardFromBytes(byte[] bytesBoard) {
        char[][] gameBoard = new char[3][3];
        int mone = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gameBoard[i][j] =(char)bytesBoard[mone];
                mone++;
            }
        }
        return gameBoard;
    }

    public static void printGameBoard(char[][] gameBoard) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((gameBoard[i][j]<'1' || gameBoard[i][j]>'9') && gameBoard[i][j]!='x' && gameBoard[i][j]!='y'){
                    System.out.println("sry,opponrnt left.. pls try again another server");
                    System.exit(0);
                }
                System.out.print("[" + gameBoard[i][j] + "]");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static boolean checkIfLocationApproved(char[][] gameBoard, String location) {
        int locationInt;
        try {
            locationInt=Integer.valueOf(location);
        }catch (NumberFormatException e){
            System.out.println("pls enter number 1-9");
            return false;
        }

        if (locationInt < 1 || locationInt > 9) {
            System.out.println("pls enter number 1-9 (Except for what has already been chosen)");
            return false;
        }
        char[] tempArr = new char[9];
        int mone = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tempArr[mone] = gameBoard[i][j];
                mone++;
            }
        }
        if (tempArr[locationInt - 1]!=(locationInt+48)) {
            System.out.println("location has already been used, pls play again");
            return false;
        }
        return true;
    }


    public static boolean gameOver(char[][] game_board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (game_board[i][j]!='x' && game_board[i][j]!='y')
                    return false;
            }
        }
        return true;
    }

    public static boolean playAgainDialog(Scanner scanner) {
        System.out.println("Play again? Yes/No");
        String answer;
        do {
            answer = scanner.next();
            switch (answer) {
                case "yes":
                case "YES":
                case "Yes":
                    return true;
                case "no":
                case "NO":
                case "No":
                    return false;

                default:
                    System.out.println("pls answer just Yes/No");
            }
        } while (true);
    }

    public static String rankString(char rank){
        String stringRank;
        switch (rank){
            case 'A':
                stringRank="Amateur";
                break;
            case 'B':
                stringRank="Beginner";
                break;
            case 'P':
                stringRank="Professional";
                break;
            case 'L':
                stringRank="Legendary";
                break;

                default:
                    stringRank="Amateur";
        }
        return stringRank;
    }
}