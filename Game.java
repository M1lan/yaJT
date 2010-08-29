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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JOptionPane;

/**
 * Class containing methods that belong to the game of Tetris-with-pentominoes
 * (Pentris) A two dimensional piece falls down stepwise until it reaches the
 * bottom. On its way down, the player can rotate the piece, drop it, make it
 * fall faster and, if he wants to, pause the game. If a line is full, it is
 * deleted. The game ends if no more pieces can be put on the board without
 * hitting the upper edge
 */
public class Game implements ActionListener, Runnable, Opponent, Serializable{
    private static final int BASE_SCORE_PER_LINE=100, INITIAL_RATE=500,
            MINIMUM_RATE=100, REWARD_SCORE_PER_LINE=50, SCORE_PER_MS_RATE=20;
    
    public static final int LARGE_GRID_HEIGHT=33, LARGE_GRID_WIDTH=16,
            MEDIUM_GRID_HEIGHT=22, MEDIUM_GRID_WIDTH=11, SMALL_GRID_HEIGHT=11,
            SMALL_GRID_WIDTH=6;
    
    private static final long serialVersionUID=-7465802064680301359L;
    
    private Polyomino activePentomino, previewPentomino;
    private int activeX, activeY;
    private boolean allowPentoes, allowTetroes, allowOtherPieces, cpuPlayer,
            gameIsOver, veryFunMode, frameIsRight, running=true;
    private Integer bottomLinesToAdd; /* For multiplayer mode */
    private long completedLines, score;
    private JFrame frame;
    private AI gameAI;
    private Grid grid;
    private MenuSystem menu;
    private Timer moveDownTimer; /* Use synchronized access only */
    private Opponent otherGame; /* For multiplayer mode */
    private GamePanel panel;
    private PolyominoFactory pFactory;
    private Random randomGenerator;
    private int rate; /* Depends on the difficulty */
    private final String SAVEGAME_FILE="savegame"; //$NON-NLS-1$
    private boolean mp_from_menu, noAutoMove;
    private NetIO relayObject;
    private String frameTitle;
    
    /**
     * Initializes a game of pentris
     */
    public Game() {
        allowPentoes=allowTetroes=allowOtherPieces=true;
        grid=new Grid(MEDIUM_GRID_WIDTH,MEDIUM_GRID_HEIGHT);
        cpuPlayer=false;
        if (cpuPlayer)
            gameAI=new AI(this);
        veryFunMode=false;
        menu=new MenuSystem(this);
        pFactory=new PolyominoFactory();
        moveDownTimer=new Timer(rate,this);
        gameInit();
    }
    
    /**
     * @return the gameover status
     */
    public boolean isGameover() {
        return gameIsOver;
    }
    
    /**
     * Initializes a game of pentris with a specified randomgenerator seed
     * 
     * @param seed The RNG seed using which polyominoes are selected
     */
    public Game(long seed) {
        allowPentoes=allowTetroes=allowOtherPieces=true;
        grid=new Grid(MEDIUM_GRID_WIDTH,MEDIUM_GRID_HEIGHT);
        cpuPlayer=false;
        if (cpuPlayer)
            gameAI=new AI(this);
        veryFunMode=false;
        menu=new MenuSystem(this);
        pFactory=new PolyominoFactory(seed);
        moveDownTimer=new Timer(rate,this);
        gameInit();
    }

    public Game(long seed, String name)
    {
	    this(seed);
	    frameTitle = name;
    }
    
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
     * ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!isPaused() && !noAutoMove && !gameIsOver
                &&(!cpuPlayer||(otherGame==null||otherGame.isCpuPlayer())))
            doMoveDown(true);
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#addBottomLines(int)
     */
    public void addBottomLines(int count) {
	if (relayObject != null)
		relayObject.relayAddRubble((byte)count);
        synchronized (bottomLinesToAdd){
            bottomLinesToAdd+=count;
        }
    }
    
    /**
     * Calculates the score, rewarding multiple lines
     * 
     * @param nr Number of lines deleted
     * @return The score
     */
    private long calculateScore(int nr) {
        int nrLines=nr;
        long ret=nrLines*BASE_SCORE_PER_LINE;
        int step=REWARD_SCORE_PER_LINE;
        while (nrLines>1){
            ret+=step;
            step+=REWARD_SCORE_PER_LINE;
            nrLines--;
        }
        return ret;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#canPause()
     */
    @Override
    public boolean canPause() {
        return true;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#changeGridSize(int, int,
     * boolean)
     */
    @Override
    public void changeGridSize(int width,int height,boolean changeOpponent) {
        frame.setSize(width*20+200,height*20+100);
        grid=new Grid(width,height);
        if (cpuPlayer)
            gameAI=new AI(this);
        if (frameIsRight)
            frame.setLocation(grid.getLength()*20+200,0);
        pauseGame();
        if (otherGame!=null&&changeOpponent){
            otherGame.changeGridSize(width,height,false);
            restart(true);
        }else{
            restart(false);
        }
    }
    
    /**
     * Let the bot calculate placements
     */
    private void doAI() {
        if (cpuPlayer&&!isPaused()&&(otherGame==null||otherGame.isCpuPlayer()))
            gameAI.place(activePentomino,previewPentomino);
    }
    
    /**
     * Moves a piece down if possible
     * 
     * @return false if piece can't be moved down any further
     */
    public boolean doMoveDown(boolean mayCallAI) {
        if (gameIsOver)
            return false;
	if (relayObject != null)
		relayObject.relayMoveDown();
        if (move(0,1)==false){
            removeCompleteLines();
            if (makeNextPolyomino()==false)
                return false; /* game over */
            if (veryFunMode)
                panel.rotate();
            if (otherGame!=null)
                otherGame.opponentDropped();
            if (mayCallAI)
                doAI();
            return false;
        }
        if (mayCallAI)
            doAI();
        frame.repaint();
        return true;
    }
    
    /**
     * Rotates the current piece. If it is at the border, it will move away.
     * 
     * @return false if the piece can't be rotated
     */
    public boolean doRotate() {
        boolean ret=false;
	if (relayObject != null)
		relayObject.relayRotate();
        if (!isPaused()&&!gameIsOver){
            Polyomino tmp=(Polyomino) activePentomino.clone();
            grid.remove(activePentomino,activeX,activeY);
            tmp.rotate();
            if (grid.canPlacePiece(tmp,activeX,activeY)){
                activePentomino=tmp;
                ret=true;
            }else if (grid.canPlacePiece(tmp,activeX-1,activeY)){
                activePentomino=tmp;
                activeX--;
                ret=true;
            }else if (grid.canPlacePiece(tmp,activeX+1,activeY)){
                activePentomino=tmp;
                activeX++;
                ret=true;
            }else if (grid.canPlacePiece(tmp,activeX-2,activeY)){
                activePentomino=tmp;
                activeX--;
                activeX--;
                ret=true;
            }else if (grid.canPlacePiece(tmp,activeX+2,activeY)){
                activePentomino=tmp;
                activeX++;
                activeX++;
                ret=true;
            }
            grid.place(activePentomino,activeX,activeY);
            frame.repaint();
        }
        return ret;
    }
    
    /**
     * Drops the active piece at the current position as far as it can go
     */
    public void dropDown() {
        if (!isPaused()){
            while (doMoveDown(false)&&!gameIsOver){/* drop it! */}
        }
    }
    
    /**
     * Game Initialization
     */
    @SuppressWarnings("boxing")
    private void gameInit() {
        gameIsOver=mp_from_menu=false;
        rate=INITIAL_RATE;
        setGameRate(rate);
        pauseGame();
        randomGenerator=new Random();
        activePentomino=
                pFactory.next(allowPentoes,allowTetroes,allowOtherPieces);
        previewPentomino=
                pFactory.next(allowPentoes,allowTetroes,allowOtherPieces);
        activeX=grid.getLength()/2;
        activeY=2;
        while (grid.canPlacePiece(activePentomino,activeX,activeY-1)){
            activeY-=1;
        }
        bottomLinesToAdd=0;
        score=0;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#gameOver(boolean)
     */
    @SuppressWarnings("nls")
    @Override
    public void gameOver(boolean showHighScores, boolean hideNoScore) {
        frame.setTitle("G A M E   O V E R");
        grid.place(activePentomino,activeX,activeY);
        if (gameIsOver)
            return;
	if (score != 0 || !hideNoScore) {
		if (otherGame==null){
		    if (showHighScores&&score!=0){
			HighScores.readHighScores();
			HighScores.addScore(score);
			HighScores.showHighScores();
			HighScores.writeHighScores();
		    }else if (score==0&&!mp_from_menu){
			JOptionPane.showMessageDialog(null,"Game over","Pentris!",
				JOptionPane.INFORMATION_MESSAGE);
		    }
		}else if (showHighScores){
		    gameIsOver=true;
		    otherGame.gameOver(false, true);
		    if (!mp_from_menu)
			JOptionPane.showMessageDialog(null,"Game over","Pentris!",
				JOptionPane.INFORMATION_MESSAGE);
		}else{
		    gameIsOver=true;
		    otherGame.gameOver(false, true);
		}
	}
        restart(true);
    }
    
    /**
     * Get the JFrame in which this game is displayed
     * 
     * @return the current JFrame
     */
    public JFrame getFrame() {
        return frame;
    }
    
    public Game getGame() {
        return this;
        
    }
    
    /**
     * Gets the grid which represents this game
     * 
     * @return the grid of this game, also containing the active pentomino at
     *         it's current position.
     */
    public Grid getGrid() {
        return grid;
    }
    
    /**
     * Get the total number of lines this player has deleted so far
     * 
     * @return number of deleted lines
     */
    public long getLines() {
        return completedLines;
    }
    
    /**
     * Gets the score
     * 
     * @return the score
     */
    public long getScore() {
        return score;
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#isCpuPlayer()
     */
    @Override
    public boolean isCpuPlayer() {
        return cpuPlayer;
    }
    
    /**
     * Is this game paused?
     * 
     * @return true if the game is paused
     */
    public boolean isPaused() {
        synchronized (moveDownTimer){
            return !moveDownTimer.isRunning()&&!gameIsOver;
        }
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#isReady()
     */
    @Override
    public boolean isReady() {
        return true;
    }
    
    public void loadGame() throws IOException, ClassNotFoundException {
        FileInputStream reader=null;
        reader=new FileInputStream(SAVEGAME_FILE);
        ObjectInputStream instream=null;
        instream=new ObjectInputStream(reader);
        Game g=(Game) instream.readObject();
        instream.close();
        changeGridSize(g.grid.getLength(),g.grid.getHeight(),false);
        setGame(g);
        run();
        
    }
    
    /**
     * Make the preview pentomino the active pentomino and generate a new
     * preview pentomino.
     * 
     * @return False if the pentomino cannot be placed at the top of the board
     *         (i.e. game over), true otherwise
     */
    private boolean makeNextPolyomino() {
        activePentomino=previewPentomino;
        
        previewPentomino=
                pFactory.next(allowPentoes,allowTetroes,allowOtherPieces);
        panel.setPreviewPiece(previewPentomino);
        
        activeX=grid.getLength()/2;
        activeY=2;
        
        if (!grid.canPlacePiece(activePentomino,activeX,activeY)){
            gameOver(true, false);
            return false;
        }
        
        while (grid.canPlacePiece(activePentomino,activeX,activeY-1)){
            activeY-=1;
        }
        return true;
    }
    
    /**
     * Moves the current piece horizontally and/or vertically, if possible
     * 
     * @param deltaX left/right
     * @param deltaY up/down
     * @return true if the pentomino move is not blocked
     */
    public boolean move(int deltaX,int deltaY) {
        if (gameIsOver)
            return false;
        grid.remove(activePentomino,activeX,activeY);
        if (grid.canPlacePiece(activePentomino,activeX+deltaX,activeY+deltaY)){
            activeY+=deltaY;
            activeX+=deltaX;
            grid.place(activePentomino,activeX,activeY);
            return true;
        }
        grid.place(activePentomino,activeX,activeY);
        return false;
    }
    
    /**
     * Make the frame of this game appear to the right of some other frame. Used
     * for split-screen multiplayer.
     */
    public void moveFrameRight() {
        frameIsRight=true;
    }
    
    /**
     * Moves the active pentomino left
     */
    public void moveLeft() {
	if (relayObject != null)
		relayObject.relayMoveLeft();
        if (!isPaused()&&!gameIsOver){
            move(-1,0);
            frame.repaint();
        }
    }
    
    /**
     * Moves the active pentomino right
     */
    public void moveRight() {
	if (relayObject != null)
		relayObject.relayMoveRight();
        if (!isPaused()&&!gameIsOver){
            move(1,0);
            frame.repaint();
        }
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#opponentDropped()
     */
    @Override
    public void opponentDropped() {
        if (cpuPlayer&&!isPaused()&&!otherGame.isCpuPlayer()&&!gameIsOver){
            gameAI.place(activePentomino,previewPentomino);
            doMoveDown(false);
        }
    }
    
    /**
     * Pauses the game
     */
    public void pauseGame() {
        if (otherGame!=null&&!otherGame.isReady())
            return;
        if (otherGame!=null&&!otherGame.canPause())
            return;
        moveDownTimer.stop();
        if (otherGame!=null&&!otherGame.isPaused()&&otherGame instanceof Game)
            ((Game) otherGame).pauseGame();
        if (frame!=null)
            frame.repaint();
    }
    
    /**
     * Remove filled lines, update score and game speed, send rubble to the
     * opponent if nessecary and add rubble to the bottom of this game if
     * nessecary.
     */
    @SuppressWarnings("boxing")
    private void removeCompleteLines() {
        int lineNr;
        int nrCompletedLines=0;
        do{
            lineNr=grid.hasCompleteLine();
            if (lineNr>=0){
                nrCompletedLines++;
                grid.deleteLine(lineNr);
            }
        }while (lineNr>=0);
        completedLines+=nrCompletedLines;
        score+=calculateScore(nrCompletedLines);
        rate=(int) (INITIAL_RATE-score/SCORE_PER_MS_RATE);
        if (rate<MINIMUM_RATE)
            rate=MINIMUM_RATE;
        setGameRate(rate);
        if (otherGame instanceof Game && nrCompletedLines > 1) {
		Game opponent = (Game) otherGame;
		opponent.addBottomLines(nrCompletedLines);
	}
        synchronized (bottomLinesToAdd){
            while (bottomLinesToAdd>0){
                int emptyColumn=randomGenerator.nextInt(grid.getLength());
                grid.addBottomLine(emptyColumn);
                bottomLinesToAdd--;
            }
        }
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#restart(boolean)
     */
    @SuppressWarnings("nls")
    @Override
    public void restart(boolean restartOpponent) {
	if (frameTitle == null)
		frame.setTitle("Pentris!");
	else
		frame.setTitle(frameTitle);
        if (otherGame!=null&&restartOpponent){
            otherGame.restart(false);
        }
        grid=new Grid(grid.getLength(),grid.getHeight());
        if (cpuPlayer)
            gameAI=new AI(this);
        gameInit();
        run();
    }
    
    /*
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings("nls")
    @Override
    public synchronized void run() {
        if (running){
            if (frame==null){
                frame=new JFrame();
		if (frameTitle == null)
			frame.setTitle("Pentris!");
		else
			frame.setTitle(frameTitle);
                frame.setSize(grid.getLength()*20+200,grid.getHeight()*20+100);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setJMenuBar(menu);
                if (frameIsRight)
                    frame.setLocation(grid.getLength()*20+200,0);
            }
            rate=INITIAL_RATE;
            completedLines=0;
            panel=new GamePanel(this,grid);
            panel.setPreviewPiece(previewPentomino);
            frame.setContentPane(panel);
            frame.setVisible(true);
            grid.place(activePentomino,activeX,activeY);
            if (cpuPlayer&&!isPaused()){
                gameAI.place(activePentomino,previewPentomino);
            }
            this.notifyAll();
        }
        return;
    }
    
    public void saveGame() throws IOException {
        FileOutputStream writer=null;
        writer=new FileOutputStream(SAVEGAME_FILE);
        ObjectOutputStream stream=null;
        stream=new ObjectOutputStream(writer);
        stream.writeObject(this);
        stream.flush();
        stream.close();
    }
    
    /**
     * Set the pieces allowed
     * 
     * @param pents Are pentominoes (5-squares) allowed
     * @param tets Are tetrominoes (4-squares) allowed
     * @param other Are other pieces (2 and 3-squares) allowed
     */
    public void setAllowedPieces(boolean pents,boolean tets,boolean other) {
        allowPentoes=pents;
        allowTetroes=tets;
        allowOtherPieces=other;
    }
    
    /**
     * Set whether this game should be played by an AI or a user
     * 
     * @param cpu Whether this game should be played by the AI
     */
    public void setCpuPlayer(boolean cpu) {
        cpuPlayer=cpu;
        if (cpu){
            gameAI=new AI(this);
            frame.repaint();
            if (otherGame!=null&&otherGame.isCpuPlayer())
                setSeed(System.currentTimeMillis());
        }else
            gameAI=null;
    }
    
    /**
     * Sets fun mode (rotate the display 90 degrees)
     */
    public void setFunMode() {
        veryFunMode=false;
        frame.setSize(grid.getHeight()*20+200,grid.getHeight()*20+100);
        panel.rotate();
    }
    
    /**
     * Sets the current game to a given game, most likely read from a saved file
     * 
     * @param g
     */
    private void setGame(Game g) {
        gameIsOver=false;
        grid.setGrid(g.grid.getGrid());
        rate=g.rate;
        setGameRate(rate);
        pauseGame();
        randomGenerator=g.randomGenerator;
        activePentomino=g.activePentomino;
        previewPentomino=g.previewPentomino;
        activeX=g.activeX;
        activeY=g.activeY;
        bottomLinesToAdd=g.bottomLinesToAdd;
        score=g.score;
        completedLines=g.completedLines;
    }
    
    /**
     * Sets the pace of the game
     * 
     * @param rate The number of milliseconds between drops of the pentomino
     */
    private void setGameRate(int rate) {
        synchronized (moveDownTimer){
            moveDownTimer.setDelay(rate);
        }
    }
    
    /**
     * Turn off the fun modes
     */
    public void setNoFunMode() {
        frame.setSize(grid.getLength()*20+200,grid.getHeight()*20+100);
        panel.setFunRotations(0);
        veryFunMode=false;
    }
    
    /*
     * @see
     * nl.unimaas.micc.group5.pentris.Opponent#setOpponentGame(nl.unimaas.micc
     * .group5.pentris.Opponent)
     */
    @Override
    public void setOpponentGame(Opponent opponent) {
        otherGame=opponent;
        menu.disableSaveLoadMenu();
        if (opponent instanceof NetIO)
            menu.disablePlayerMenu();
        if (!otherGame.canPause())
            menu.disablePause();
    }
    
    /**
     * Sets the initial value of the internal state of the pseudorandom number
     * generator used to select the random pentominoes
     * 
     * @param seed The seed for the RNG
     */
    public void setSeed(long seed) {
        grid=new Grid(MEDIUM_GRID_WIDTH,MEDIUM_GRID_HEIGHT);
        if (cpuPlayer)
            gameAI=new AI(this);
        pFactory=new PolyominoFactory(seed);
        gameInit();
        run();
    }
    
    /**
     * Sets very fun mode (rotates the screen 90 degrees every time a pentomino
     * hits the bottom)
     */
    public void setVeryFunMode() {
        veryFunMode=true;
        frame.setSize(grid.getHeight()*20+200,grid.getHeight()*20+100);
    }
    
    /*
     * @see nl.unimaas.micc.group5.pentris.Opponent#stop()
     */
    @Override
    public void stop() {
        running=false;
        gameOver(false, true);
        frame.dispose();
    }
    
    /**
     * Unpauses the game
     */
    public void unPauseGame() {
	frame.repaint();
        moveDownTimer.start();
        if (otherGame!=null&&otherGame.isPaused()&&otherGame instanceof Game)
            ((Game) otherGame).unPauseGame();
    }
    
    /**
     * checks if player goes to mp via the menu, needed to avoid game over
     * message
     * 
     * @param b the mp_from_menu to set
     */
    public void setMp_from_menu(boolean b) {
        this.mp_from_menu=b;
    }
    
    public boolean isMp_from_menu() {
        return mp_from_menu;
    }

    public void setNoAutoMove(boolean val)
    {
	    noAutoMove = true;
	    if (val)
		    synchronized(moveDownTimer) {
			    moveDownTimer.stop();
		    }
	    else
		    synchronized(moveDownTimer) {
			    moveDownTimer.start();
		    }
    }

    public void relayMovements(NetIO relayTarget)
    {
	    relayObject = relayTarget;
    }
}
