import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *Aparajita Sahay
 * @author Munehiro Fukuda
 */
public class OnlineTicTacToeFrame implements ActionListener {

    private final int INTERVAL = 1000;         // 1 second
    private final int NBUTTONS = 9;            // #bottons
    private ObjectInputStream input = null;    // input from my counterpart
    private ObjectOutputStream output = null;  // output from my counterpart
    private JFrame window = null;              // the tic-tac-toe window
    private JButton[] button = new JButton[NBUTTONS]; // button[0] - button[9]
    private boolean[] myTurn = new boolean[1]; // T: my turn, F: your turn
    private String myMark = null;              // "O" or "X"
    private String yourMark = null;            // "X" or "O"
    private ServerSocket server = null;
    private Socket client = null;
    private String mark=null;
    private int countNoOFMoves=0;
    private String[] movesPlayed = new String[NBUTTONS];
    boolean gameStatus = false;

    /**
     * Prints out the usage.
     */
    private static void usage( ) {
        System.err.
	    println( "Usage: java OnlineTicTacToe ipAddr ipPort(>=5000)" );
        System.exit( -1 );
    }

    /**
     * Prints out the track trace upon a given error and quits the application.
     * @param an exception 
     */
    private static void error( Exception e ) {
        e.printStackTrace();
        System.exit(-1);
    }

    /**
     * Starts the online tic-tac-toe game.
     * @param args[0]: my counterpart's ip address, args[0]: his/her port
     */
    public static void main( String[] args ) {
        // verify the number of arguments
        if ( args.length != 2 ) {
            usage( );
        }

        // verify the correctness of my counterpart address
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName( args[0] );
        } catch ( UnknownHostException e ) {
            error( e );
        }

        // verify the correctness of my counterpart port
        int port = 0;
        try {
            port = Integer.parseInt( args[1] );
        } catch (NumberFormatException e) {
            error( e );
        }
        if ( port < 5000 ) {
            usage( );
        }

        // now start the application
        OnlineTicTacToeFrame game = new OnlineTicTacToeFrame( addr, port );
    }

    /**
     * Is the constructor that sets up a TCP connection with my counterpart,
     * brings up a game window, and starts a slave thread for listenning to
     * my counterpart.
     * @param my counterpart's ip address
     * @param my counterpart's port
     */
    public OnlineTicTacToeFrame( InetAddress addr, int port ) {
        // set up a TCP connection with my counterpart
    	try {
    	    server = new ServerSocket( port );
            // ITEM 1: set the server non-blocking, (i.e. time out beyon 1000) 
    	    server.setSoTimeout(1000);
    	    
    	} catch ( Exception e ) {
    	    error( e );
    	}
    	
    	boolean isO;
    	
    	// While accepting a remote request, try to send my connection request
    	while ( true ) {
    	    try {
    		
    	    	// ITEM 2: Try to accept a connection as a server
    	    	//The accept method waits until a client starts up and requests a
    	    	//connection on the host and port of this server
    	    	client = server.accept();
    	    } catch ( SocketTimeoutException ste ) {
    		// Couldn't receive a connection request withtin INTERVAL
    	    } catch ( IOException ioe ) {
    		error( ioe );
    	    }
    	    // Check if a connection was established. If so, leave the loop
    	    if ( client != null ) {
    	    	System.out.println( "Successfully acquired connection by listenting" );
    	    	isO = false;
    	    	
    	    	break;
    	    }

    	    try {
    	    // ITEM 3: Try to request a connection as a client
    		 client = new Socket(addr,port); 
    		 
    	    } catch ( IOException ioe ) {
    		// Connection refused
    	    }
    	    // Check if a connection was established, If so, leave the loop
    	    if ( client != null ) {
    	    	System.out.println( "Successfully acquired connection by sending request" );
    	    	isO = true;
    	   
    	    	break;
    	    }
    	}  	
    	
    	// Exchange a message with my counter part.
    	try {
    	    System.out.println( "TCP connection established..." );
    	    /* ITEM 4: Create an ObjectOutputStream object */;
    	     output= new ObjectOutputStream(client.getOutputStream()); 
    	     /* ITEM 5: Create an InputOutputStream Object */ ;
    	     input= new ObjectInputStream( client.getInputStream( ) ); 
    	    
    	} catch ( Exception e ) {
    	    error( e );
    	}
    	
    	
       	// set up a window
        makeWindow( isO ); // or makeWindow( false );
        // start my counterpart thread
        Counterpart counterpart = new Counterpart(input,this);
        counterpart.start();
    }

    /**
     * Creates a 3x3 window for the tic-tac-toe game
     * @param true if this window is created by the 1st player, false by
     *        the 2nd player
     */
    private void makeWindow( boolean amFormer ) {
        myTurn[0] = amFormer;
        myMark = ( amFormer ) ? "O" : "X";    // 1st person uses "O"
        yourMark = ( amFormer ) ? "X" : "O";  // 2nd person uses "X"

        // create a window
        window = new JFrame("OnlineTicTacToe(" +
                ((amFormer) ? "former)" : "latter)" ) + myMark );
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(3, 3));

	// initialize all nine cells.
        for (int i = 0; i < NBUTTONS; i++) {
            button[i] = new JButton();
            window.add(button[i]);
            button[i].addActionListener(this);
        }

	// make it visible
        window.setVisible(true);
    }

    /**
     * Marks the i-th button with mark ("O" or "X")
     * @param the i-th button
     * @param a mark ( "O" or "X" )
     * @param true if it has been marked in success
     */
    private boolean markButton( int i, String mark ) {
	if ( button[i].getText( ).equals( "" ) ) {
	    button[i].setText( mark );
	    button[i].setEnabled( false );
	    return true;
	}
	return false;
    }

    /**
     * Checks which button has been clicked
     * @param an event passed from AWT 
     * @return an integer (0 through to 8) that shows which button has been 
     *         clicked. -1 upon an error. 
     */
    private int whichButtonClicked( ActionEvent event ) {
	for ( int i = 0; i < NBUTTONS; i++ ) {
	    if ( event.getSource( ) == button[i] )
		return i;
	}
	return -1;
    }

    /**
     * Checks if the i-th button has been marked with mark( "O" or "X" ).
     * @param the i-th button
     * @param a mark ( "O" or "X" )
     * @return true if the i-th button has been marked with mark.
     */
    private boolean buttonMarkedWith( int i, String mark ) {
	return button[i].getText( ).equals( mark );
    }

    /**
     * Pops out another small window indicating that mark("O" or "X") won!
     * @param a mark ( "O" or "X" )
     */
    private void showWon( String mark ) {
	JOptionPane.showMessageDialog( null, mark + " won!" );	
    }
    
    /**
     * Returns the 1D index number of tictactoe board game.
     * @param args[0]: row, args[0]: column , arg[2]: totalColumnSize
     */
    private int getIndex(int row,int column,int maxColumn)
    {
    	int rownumber = row*maxColumn+column;
    	return rownumber;
    }
    

    /**
     * checks the gaveOver status of the game by checking the entire 
     * row where user have recenlty marked value('X' or 'O')
     * @param args[0]: mark(myMArk or counterpart mark), args[0]: buttonID that user clicked on board.
     */
    private boolean checkRowForWinner(String mark , int buttonId)
    {
    	
    	int rows = 3;
    	int columns = 3;
    	int rowNumber = buttonId/rows;
    	boolean winStatus=false;
    	int columnCount= 1;
    	int index;
    	int rowIndex = rowNumber;
    	int columnIndex = 0;
    	
    	while (columnIndex < columns)
    	{
    		index = getIndex(rowIndex,columnIndex,columns);
    		if(movesPlayed[index]!=mark || movesPlayed[index]=="")
    		{
    			return winStatus;
    		}
    		else
    		{
    			columnIndex++;	
    		}
    	}
		winStatus=true;
		return winStatus;   	
    }
    
    /**
     * checks the gaveOver status of the game by checking the entire 
     * column where user have recenlty marked value('X' or 'O')
     * @param args[0]: mark(myMArk or counterpart mark), args[0]: buttonID that user clicked on board.
     */
    private boolean checkColumnForWinner(String mark,int buttonId)
    {
    	//2) check columns
    	int rows =3;
    	int columns = 3;
    	boolean winStatus=false;
    	int columnNumber =  buttonId%columns;
    	int rowIndex= 0;
    	int columnIndex = columnNumber;
    	int index; 
    	while (rowIndex<rows)
    	{
    		index = getIndex(rowIndex,columnIndex,columns);
    		if(movesPlayed[index]!=mark|| movesPlayed[index]=="")
    		{
    			return winStatus;
    		}
    		else
    		{
    			rowIndex++;
    		}
    	}
    	winStatus=true;
    	return winStatus;
    }
    
    /**
     * checks the gaveOver status of the game by checking the two
     * diagonals where user have recenlty marked value('X' or 'O')
     * @param args[0]: mark(myMArk or counterpart mark), args[0]: buttonID that user clicked on board.
     */
    private boolean checkDiagonalForWinner(String mark,int buttonId)
    {
    	int rows =3;
    	int columns = 3;
    	boolean winStatus=false;
    	int columnNumber =  buttonId%columns;
    	int rownumber = buttonId/rows;
    	int rowIndex= 0;
    	int columnIndex = 0;
    	int index;
    	if(columnNumber==rownumber)
    	{
    		//int index; 
	    	while (rowIndex<rows)
	    	{
	    		index = getIndex(rowIndex,columnIndex,columns);
	    		if(movesPlayed[index]!=mark|| movesPlayed[index]=="")
	    		{
	    			return winStatus;
	    		}
	    		else
	    		{
	    			rowIndex++;
	    			columnIndex++;
	    		}
	    	}
	    	winStatus=true;
	    	return winStatus;
    	}
    	//Detect right to left diagonal
    	int sum = columnNumber + rownumber;
    	if(sum==rows-1)
    	{
    		rowIndex = 0;
    		columnIndex = columns-1;
    		while (rowIndex<rows)
	    	{
	    		index = getIndex(rowIndex,columnIndex,columns);
	    		if(movesPlayed[index]!=mark|| movesPlayed[index]=="")
	    		{
	    			return winStatus;
	    		}
	    		else
	    		{
	    			rowIndex++;
	    			columnIndex--;
	    		}
	    	}
	    	winStatus=true;
	    	return winStatus;
    	}
    	
    	return false;
    }
    
    /**
     * checks the gaveOver status of the game by checking the 
     * 1) Row 2) column 3) two diagonals 
     * where user have recenlty marked value('X' or 'O')
     * @param args[0]: mark(myMArk or counterpart mark), args[0]: buttonID that user clicked on board.
     */
    private boolean getWinner(String mark,int buttonId)
    {
    	int rows =3;
    	int columns = 3;
    	int rowNumber = buttonId/rows;
    	boolean winStatus = false;
    	if(movesPlayed.length == 0)
    	{
    		return winStatus;
    	}
    	//1) check row
    	boolean isRowWin = checkRowForWinner(mark,buttonId);
    	if(isRowWin==true)
    	{
    		winStatus = true;
    		return winStatus;
    	}
    	boolean isColumnWin = checkColumnForWinner(mark,buttonId);
    	if(isColumnWin == true)
    	{
    		winStatus = true;
    		return winStatus;
    	}
    	
    	boolean isDiagonalWin = checkDiagonalForWinner(mark,buttonId);
    	if(isDiagonalWin == true)
    	{
    		winStatus = true;
    		return winStatus;
    	}
    	return winStatus;
    }
    
    /**
     * Not calling currently. For future implementation
     */
    private boolean checkDraw()
    {
    	boolean IsDraw = true;
    	for(int i =0;i<movesPlayed.length;i++)
    	{
    		//"O" or "X"
    		if (!"X".equals(movesPlayed[i]) || !"O".equals(movesPlayed[i]))
    		//if(movesPlayed[i] != 'X' || movesPlayed[i] != 'O')
    		{
    			//System.out.println(movesPlayed[i]);
    			//System.out.println("False condition ");
    			IsDraw = false;
    			return IsDraw;
    		}
    	}
    	//System.out.println("true condition ");
    	
    	return IsDraw;
    }
    /**
     * Syncronized method to set myTurn to true or false.
     * At one time only one user (Me or counterpart) can set the myTurn variable.
     * @param args[0]: status
     */
    public synchronized void setMyTurn(boolean status)
    {
    	myTurn[0]=status;
    }
    /**
     * Syncronized method to set status of the game over to true.
     * At one time only one user can (Me or counterpart) can win the game and set the variable.
     * If the gameStatus is true then game is Won by either of the player.
     * After user make its move and check if the game is Won then gameStatus 
     * variable is set to true and the user win's the game otherwise false and game continues.
     * @param args[0]: status. 
     */
    public synchronized void setMyGameStatus(boolean status)
    {
    	gameStatus=status;
    }
    
    /**
     * NOt working currently, throw exceptions
     */
    public synchronized void closeAllSocket()
    {
    	try {
    		output.close();
    	}
    	catch(Exception e)
    	{
    		error(e);
    	}	
    }
    
    
    /**
     * Is called by AWT whenever any button has been clicked. You have to:
     * <ol>
     * <li> check if it is my turn,
     * <li> check which button was clicked with whichButtonClicked( event ),
     * <li> mark the corresponding button with markButton( buttonId, mark ),
     * <li> send this informatioin to my counterpart,
     * <li> checks if the game was completed with 
     *      buttonMarkedWith( buttonId, mark ) 
     * <li> shows a winning message with showWon( )
     */
    public void actionPerformed( ActionEvent event ) {
    	if(myTurn[0]==false)
    	{
    		return;
    	}
    	
    	if(gameStatus==true)
    	{
    		return;
    	}
    	int buttonId = whichButtonClicked( event ); 
    	 boolean marked =markButton( buttonId, myMark );
    	 if(marked==false)
    	 {
    		 System.out.println("Already you have selected this place");
    	 }
    	 else
    	 {
    		 try 
    		 {
    	    	    output.writeObject(  buttonId + "/" + "\t" +  
    	    				InetAddress.getLocalHost( ).getHostName());
    	    	    //Enter the player's turn to movesPlayed Array, used to find winner
    	    	    movesPlayed[buttonId]=myMark;
    	    	    boolean result = getWinner(myMark,buttonId);
    	    	    if(result==true)
    	    	    {
    					//show won
    					showWon(myMark);
    					setMyGameStatus(true);
    					//closeAllSocket();
    					//input.close();
    					//output.close();
    					return;
    				}
    	    	    
    	    	    /*boolean IsDraw = checkDraw();
    	    	    if(IsDraw == true )
    	    	    {
    	    	    	JOptionPane.showMessageDialog( null , "Its a Draw!" );	
    	    	    	closeAllSocket();
    	    	    }
    	    	    */
    	    	    
    	    	    setMyTurn(false);
    	    	    	
    	    	}
    	    	catch(Exception e)
    	    	{
    	    		error(e);
    	    	}	
    	 }
    	
	     }

    /**
     * This is a reader thread that keeps reading fomr and behaving as my
     * counterpart.
     */
    private class Counterpart extends Thread {
    	/**
	 * Is the body of the Counterpart thread.
	 * 
	 */
    	private ObjectInputStream input;
    	private OnlineTicTacToeFrame game;
    	public Counterpart(ObjectInputStream inputBuffer,OnlineTicTacToeFrame game)
    	{
    		input = inputBuffer;
    		this.game = game; 
    	}
        @Override
        public void run( ) {
        	
        			String counterpart=null;
        			
        			try {
        			     //while(countNoOFMoves<=8)
        				while(gameStatus!=true)
        				{
        					
		        			//Reads data of the counterpart from input buffer
        					System.out.println("Waiting to counterpart.... " );
		        			String b =(String)input.readObject( );
		        			System.out.println("I" + "\t " +InetAddress.getLocalHost( ).getHostName() + 
		        					"Listening to counterpart : " + "Button id " + b);
		        			//parse input buffer
		        			String[] parts = b.split("/");
		        			int buttonid = Integer.parseInt(parts[0]);
		        			
		        			//Enter the player's turn to movesPlayed Array, used to find winner
		        			movesPlayed[buttonid]=yourMark;
		        	
		        			boolean counterPartMark = markButton( buttonid, yourMark );
		        			
		        			if(counterPartMark==false)
		        			{
		        				System.out.println("Already filled");
		        			}
		        			else
		        			{
		        				this.game.setMyTurn(true);
		        				boolean result=getWinner(myMark,buttonid);
		        				if(result==true)
		        				{
		        					//show won
		        					showWon(yourMark);
		        					setMyGameStatus(true);
		        					//input.close();
		        					//closeAllSocket();
		        				}
		        				/*boolean IsDraw = checkDraw();
		        	    	    if(IsDraw == true )
		        	    	    {
		        	    	    	JOptionPane.showMessageDialog( null, "Its a Draw!" );	
		        	    	    	closeAllSocket();
		        	    	    }
		        	    	    */
		        	    	    
		        			}	
		        				
        				}
        			}
        			catch(Exception e)
        			{
        				error(e);
        			}
	}
    }
}
