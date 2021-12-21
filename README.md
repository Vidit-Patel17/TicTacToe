# **Tic Tac Toe App**
This is a simple Tic Tac Toe app , which uses firebase as backend server to host multiplayer games and user data, no data is stored locally on the user device other than login cache. There are 2 modes for playing - Two Player and One Player. In one player game , the user plays against computer.For two player version , a user has 2 options , one is to create a new game and wait for an other user to join , or join another player who has already created the game and this list of user who are waiting for other player to join is displayed on dashboard. 

For 2 player Game there is status textView , which shows if the opponent is connected/disconnected or yet to connect, this is also factored in the functionality of the app , i.e. in 2 player mode ,the players can only make a move when both of them are connected, and so on. For one player , it just shows the game type.

This app uses firebase for authentication and storing other user data.

# TODO
Add firebase console credentials and links to realtime database marked as todo 
