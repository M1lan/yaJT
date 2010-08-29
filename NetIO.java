/*
 * Pentris: A 'Tetris'-like Puzzle Game featuring Pentominoes
 * 
 * Copyright (C) 2008 Group 5, Project 1.2 BA KECS University of Maastricht, NL
 * 
 * SVN Repository and details available at http://code.google.com/p/pentominoes/
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 * Network Multiplayer support. This implementation of the Opponent interface in
 * this class basically behaves as if it's methods are called on the connecting
 * computer's game.
 */
public class NetIO implements Runnable, Opponent{
    private static final int IPTOS_LOWDELAY=0x10; /* See RFC 1349 */
    private static final byte MSG_BITS=4;
    private static final char MSG_BYE=4;
    private static final char MSG_GAMEOVER=1;
    private static final char MSG_IAMSERVER=5;
    /* IAMSERVER is always followed by 8 bytes of the RNG seed */
    private static final char MSG_NOP=0;
    private static final char MSG_RESTART=3;
    private static final char MSG_MOVEDOWN = 6;
    private static final char MSG_MOVERIGHT = 7;
    private static final char MSG_MOVELEFT = 8;
    private static final char MSG_MOVEROTATE = 9;
    private static final char MSG_OPPONENT_MARKED_RUBBLE = 10;
    private Game game;
    private boolean isServer;
    private int port;
    private long seed; /* Only used if we are server */
    private String serverName;
    private Socket socket;
    private ServerSocket serv;
    private Game opponentState;
    private Thread opponentThread;
    
    /**
     * Prepares a network multiplayer game in server mode
     * 
     * @param localGame The game on this computer that will be played
     * @param port The port on which to listen
     * @param seed The seed for the polyomino selecting RNG to send to the
     *        client
     */
    public NetIO(Game localGame,int port,long seed) {
        game=localGame;
        isServer=true;
        this.port=port;
        socket=null;
        this.seed=seed;
    }
    
    /**
     * Prepares a network multiplayer game in client mode
     * 
     * @param localGame The game on this computer that will be played
     * @param port The port on which to connect
     * @param name The hostname/address of the server game
     */
    public NetIO(Game localGame,int port,String name) {
        game=localGame;
        isServer=false;
        serverName=name;
        this.port=port;
        socket=null;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#canPause()
     */
    @Override
    public boolean canPause() {
        return false;
    }
    
    public boolean isPaused() {
        return false;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#changeGridSize(int, int,
     * boolean)
     */
    @Override
    public void changeGridSize(int width,int height,boolean changeOpponent) {
        closeConnection();
        System.exit(1); /* We don't support remove restarts yet */
    }
    
    /**
     * Terminates network connection
     */
    private void closeConnection() {
        sendWithoutExceptions(MSG_BYE);
        try{
            socket.close();
        }catch (Exception e){
            /* Who cares? */
        }
        socket=null;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#gameOver(boolean)
     */
    @Override
    public void gameOver(boolean showHighScores, boolean hideNoScore) {
	if (socket == null && isServer) {
		final ServerSocket tmp = serv;
		serv = null;
		try {
			tmp.close();
		} catch (Exception e) {
		}
		return;
	} else {
		sendWithoutExceptions(MSG_GAMEOVER);
	}
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#isCpuPlayer()
     */
    @Override
    public boolean isCpuPlayer() {
        return false;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#isReady()
     */
    @Override
    public boolean isReady() {
        return socket!=null;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#opponentDropped()
     */
    @Override
    public void opponentDropped() {/* not needed here */}
    
    /**
     * Listens to the connected game and responds to the commands it sends.
     * Note: this function will never return, it will exit the program on errors
     * or when the connected computer exits.
     */
    @SuppressWarnings({"nls", "null"})
    public void readloop() {
        InputStream netin=null;
        try{
            netin=socket.getInputStream();
        }catch (IOException e){
            System.out.println("Congratulation, you've found a bug!");
            System.exit(1);
        }
        while (true){
            int c=-2;
            try{
                c=netin.read();
            }catch (EOFException e){
                System.out.println("Unexpected end of stream.");
                System.exit(1); /* This can probably be recovered from */
            }catch (IOException e){
                System.out.println("Connection error.");
                System.exit(1); /* Why not retry to connect? */
            }
            if (c==-1){
                JOptionPane.showMessageDialog(null,"Opponent game over",
                        "Pentris!",JOptionPane.INFORMATION_MESSAGE);
                System.exit(1);
            }
            switch (c%(1<<MSG_BITS)){
            case MSG_GAMEOVER:
                game.gameOver(true, false);
                System.exit(0);
                break;
            case MSG_RESTART:
                game.restart(false);
                break;
            case MSG_BYE:
                JOptionPane.showMessageDialog(null,"Opponent game over",
                        "Pentris!",JOptionPane.INFORMATION_MESSAGE);
                game.gameOver(true, false);
                System.exit(0);
                break;
            case MSG_NOP:
                break;
            case MSG_IAMSERVER:
                if (isServer){
                    System.out.println("Opponent claims to also be server,"
                            +" ignoring.");
                }else{
                    try{
                        long seed=0;
                        for (int i=0; i<8; i++){
                            c=netin.read();
                            if (c==-1)
                                throw new EOFException();
                            seed=seed*256+c;
                        }
                        System.out.println("Got seed "+seed);
                        game.setSeed(seed);
			opponentState = new Game(seed, "Opponent game");
			opponentThread = new Thread(opponentState, "Opponent visualisation thread");
			opponentState.moveFrameRight();
			opponentState.setOpponentGame(game);
			opponentState.setNoAutoMove(true);
			game.relayMovements(this);
			opponentThread.start();
			synchronized (opponentState) {
				try {
					if (opponentState.getFrame() == null)
						opponentState.wait();
				} catch (InterruptedException e) {
					System.exit(1);
				}
			}
			opponentState.unPauseGame();
                    }catch (EOFException e){
                        System.out.println("Unexpected end of stream.");
                        System.exit(1); // This can probably be recovered from
                    }catch (IOException e){
                        System.out.println("Connection error.");
                        System.exit(1);/* Why not retry to connect? */
                    }
                }
                game.unPauseGame();
                break;
	    case MSG_MOVEDOWN:
		opponentState.doMoveDown(false);
		break;
	    case MSG_MOVERIGHT:
		opponentState.moveRight();
		break;
	    case MSG_MOVELEFT:
		opponentState.moveLeft();
		break;
	    case MSG_MOVEROTATE:
		opponentState.doRotate();
		break;
	    case MSG_OPPONENT_MARKED_RUBBLE:
		System.out.format("Opponent marked %d lines\n", c >> MSG_BITS);
		opponentState.addBottomLines(c >> MSG_BITS);
		break;
            default:
                System.out.println("Unknown command from remote: "+c);
            }
        }
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#restart(boolean)
     */
    @Override
    public void restart(boolean restartOpponent) {
        sendWithoutExceptions(MSG_RESTART);
    }
    
    /*
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (isServer)
            startServer();
        else
            startClient();
    }
    
    /**
     * Send some character to the connected computer, exiting the program if any
     * errors occur.
     * 
     * @param c The character to send
     */
    @SuppressWarnings("nls")
    private void sendWithoutExceptions(int c) {
        try{
            socket.getOutputStream().write(c);
        }catch (Exception e){
            System.out.println("Unexpected error sending data to remote.");
	    if (c != MSG_GAMEOVER && c != MSG_BYE && c != MSG_RESTART)
		    System.exit(1);
        }
    }
    
    /*
     * @see
     * nl.unimaas.micc.group5.pentris.Opponent#setOpponentGame(nl.unimaas.micc
     * .group5.pentris.Opponent)
     */
    @Override
    public void setOpponentGame(Opponent o) {
        game=(Game) o;
    }
    
    /**
     * Starts a client connection. Note: this method will never return, it will
     * exit the program when done.
     */
    @SuppressWarnings("nls")
    public void startClient() {
        if (!(serverName==null)){
            try{
                socket=new Socket(serverName,port);
            }catch (IOException e){
                System.out.println("Could not connect to "+serverName+":"+port);
                System.exit(1);
            }
            try{
                socket.setTcpNoDelay(true);
                socket.setTrafficClass(IPTOS_LOWDELAY);
            }catch (Exception e){
                /* We really don't care */
            }
            /* No unpause, that will be done when we hear from the server */
            readloop();
        }
    }
    
    /**
     * Starts a server connection. Note: This method will never return unless
     * the user switches to a different game mode, it will exit the program
     * when done.
     */
    @SuppressWarnings("nls")
    public void startServer() {
        try{
            serv=new ServerSocket(port);
            System.out.println("Waiting for connection on port "+port+".");
            socket=serv.accept();
            serv.close();
            sendWithoutExceptions(MSG_IAMSERVER);
            sendWithoutExceptions((int) ((seed>>56)&255));
            sendWithoutExceptions((int) ((seed>>48)&255));
            sendWithoutExceptions((int) ((seed>>40)&255));
            sendWithoutExceptions((int) ((seed>>32)&255));
            sendWithoutExceptions((int) ((seed>>24)&255));
            sendWithoutExceptions((int) ((seed>>16)&255));
            sendWithoutExceptions((int) ((seed>>8)&255));
            sendWithoutExceptions((int) (seed&255));
            System.out.println("Sent seed "+seed);
        }catch (Exception e){
	    if (serv == null) { /* The user switched out of network mode */
		    return;
	    }
            System.out.println("Could not listen on port "+port+".");
            System.exit(1);
        }
        try{
            socket.setTcpNoDelay(true);
            socket.setTrafficClass(IPTOS_LOWDELAY);
        }catch (Exception e){
            /* We really don't care */
        }
        game.unPauseGame();
	opponentState = new Game(seed, "Opponent game");
	opponentThread = new Thread(opponentState, "Opponent visualisation thread");
	opponentState.moveFrameRight();
	opponentState.setOpponentGame(game);
	opponentState.setNoAutoMove(true);
	game.relayMovements(this);
	opponentThread.start();
	synchronized (opponentState) {
		try {
			if (opponentState.getFrame() == null)
				opponentState.wait();
		} catch (InterruptedException e) {
			System.exit(1);
		}
	}
	opponentState.unPauseGame();
        readloop();
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#stop()
     */
    @Override
    public void stop() {
        sendWithoutExceptions(MSG_BYE);
	if (socket != null) {
		try {
			socket.close();
		} catch (Exception e) {
		}
	}
    }

    public void relayMoveDown()
    {
	    sendWithoutExceptions(MSG_MOVEDOWN);
    }

    public void relayRotate()
    {
	    sendWithoutExceptions(MSG_MOVEROTATE);
    }

    public void relayMoveLeft()
    {
	    sendWithoutExceptions(MSG_MOVELEFT);
    }

    public void relayMoveRight()
    {
	    sendWithoutExceptions(MSG_MOVERIGHT);
    }

    public void relayAddRubble(byte count)
    {
	    sendWithoutExceptions(MSG_OPPONENT_MARKED_RUBBLE +
			          (count << MSG_BITS));
    }
}
