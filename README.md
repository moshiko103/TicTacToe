# TicTacToe
Final java project - TicTacToe with Server and Client.

A fun game with very friendly user interface, so come and play tic tac toe! win your opponents, raise your rank and maybe become a legend! 

### Server
* Main - Server management, receives inquiries from clients, identifies them by login/signup and send each 2 clients to new game thread. 

* GameThread - Game management-Here the whole game happens(server side)! includes:the game UI, communication between clients, backup players details and so on..

* Player - Creates a player object. contains Getters and Setters of player details(name,rank...) and essential methods like reachWin/gotDefeat.

* TicTacToe - Creates a game(TicTacToe) object, contains all the methods the game needs (execute moves,check winner,reset game and so on..)

### Client
* Main - Client management-Here the whole game happens(client side) - connects with the server, login/signup, play until one of the clients disconnects.

* GameSupport - contains methods that support the Main.class in the progress of the game (print game board, check if gameOver, Checking a player's input confirmation and so on..)

* Player - The same class as the server.

![UI image](http://www.interload.co.il/upload/6736013.png)

### @All rights reserved to Moshiko frastai.
