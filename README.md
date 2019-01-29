# TicTacToe
Built tic tac toe game to showcase Peer to peer communication using socket programming 
Aparajita Sahay

Assignment 1 
Note: Mention same port number to both the machines eg. “12345” while running the program for acquiring connection.
To come out of game where no one wins the game come, manually exit through putty using putty console or by closing the game console.
When any user wins the game, game console will be closed by manually closing the game console or by using putty.
“O” always makes the first move. And if user tries to make a move when they don’t have their turn, application ignores such moves
1.	Implementation/Features: 
a.	Built peer to peer communication where two users A and B can play tic tac toe game. Built socket between two machines by establishing a TCP connection between them.
b.	One machine acts as a server and other as client. Machine has a socket that is bound to a port number.
c.	Other machine makes a connection request to other machine with other’s machine ip address and port number. Assumption is port number in both the machine is same.
d.	Once both the machine sent there port number they are listening, socket invokes accept method and wait for a request. Accept method listen for incoming connection request from other machine.

e.	After both machine accepts each other connection request, then each machine creates thread for listening to counterpart moves and updating the moves at their own ends. Thread keeps reading established TCP connection.

f.	getInputStream and getoutputStream method in socket create datainput and datoutput stream at both the machines. Each user writes to its outputstream and reads other users inputstream to be aware of other player’s move.
g.	In peer to peer communication shared resources are present between two clients. 
 My program has two shared variables gameStatus and myTurn, that needs to be updated only by one user at one time, therefore I have used Synchronization method to set these variables.
h.	In synchronization method: When one thread is executing a synchronized method for an object, all other threads that invoke synchronized methods for the same object block (suspend execution) until the first thread is done with the object.
i.	Synchronization method also makes sure that changes to the state of the object are visible to all threads.
j.	Another approach to handle the same problem is by using synchronization statement.
Synchronized statements are also useful for improving concurrency with fine-grained synchronization. However, I used synchronization method approach.
k.	In my game application, one client establishes connection by listening and other by sending connection request. Machine that creates connection b sending request sets variable IsO to true and makes the first turn.
l.	Each machine has its input stream and output stream. To exchange information between two machines, each machine writes data to its output stream in actionPerformed method and thread created at each machine listens to the inputStream of other machine in run method.
m.	To make sure that at a time only one user can play its turn, a shared variable is created and makes sure that only one user can access it at one point of time. Synchronization method is built and makes sure that user shoes myTurn variable is true only that can make a move.
n.	Socket connection (transport layer) is faster channel for communication than communication through services in application layer. 
o.	After the game is won by any user, close the output stream and input stream of each machine. Application runs but once socket is closed exception EOF is thrown too. But the output stream and input stream is not closed then no exception is thrown but machine will exit its state by manually exiting from the putty.

2.	Limitation:
a.	Security: In tic-tac toe game if User A sends some malicious data, it is important to authenticate the data first. 
In socket programming, after connection is established between client and server client can send any malicious data to server that can corrupt the server machine. Therefore, validation at server side is essential before accepting the request from client.
b.	Tic-tac toe peer to peer communication is less stable that client server communication.
In this game, each machine shares some information with each other such as gameStatus and myTurn variable. If one machine crashes then the information associated about these shared variables are also lost and this will affect the peer to peer communication. However, in client server network, shared resource resides on server and if client erases some shared data at client’s end or server’s end then there is a possibility of backup data.

3.	Improvement 
a.	Validation of input data. Counterpart thread of a machine keep reading the input buffer of other client. However, there is no data validation of what each user write/read in a buffer. It is important to do data validation before read/write data to input/output stream to make the communication more safe and reliable.
b.	Instead of peer to peer architecture where both client have access to shared resources, it will be better to have client server communication. In client server architecture, server will have access to shared resource and sends response to each client. And these shared resources can be replicated to other servers if in case data is lost from parent server. 
This way data loss can be prevented and system can be more reliable.
c.	In peer to peer communication, state of past complete or incomplete games cannot be maintained. Once client machine is closed data is lost. However, with client server architecture server can maintain the state of past complete/incomplete games.

