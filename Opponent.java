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
/**
 * Interface for a Multiplayer game of Pentris
 */
public interface Opponent{
    
    /**
     * Can this game be paused
     * 
     * @return true if the game can be paused (for example because it is a
     *         network game)
     */
    public boolean canPause();
    
    /**
     * Is this game paused
     * 
     * @return true if the game is paused
     */
    public boolean isPaused();
    
    /**
     * Changes the size of the playing field
     * 
     * @param width The new width of the field in squares
     * @param height The new height of the field in squares
     * @param changeOpponent Should the size of the field of the opponent also
     *        be changed?
     */
    public void changeGridSize(int width,int height,boolean changeOpponent);
    
    /**
     * Ends the game
     * 
     * @param showHighScores
     * 	Should the high scores/game-over dialog be shown
     * @param hideNoScore
     *  Should the high scores/game-over dialog be hidden if the score is zero
     */
    public void gameOver(boolean showHighScores, boolean hideNoScore);
    
    /**
     * Is this a game controlled by an AI
     * 
     * @return true if player is cpu
     */
    public boolean isCpuPlayer();
    
    /**
     * Is the controller of this game ready
     * 
     * @return whether the controller is ready (i.e. network connections
     *         established, that kind of things)
     */
    public boolean isReady();
    
    /**
     * Indicate that the opponent of this game has dropped a stone. Used for
     * example to let the AI wait for a user to drop a stone in multiplayer
     * mode.
     */
    public void opponentDropped();
    
    /**
     * Restarts the game
     * 
     * @param restartOpponent Should the opponent game also be restarted
     */
    public void restart(boolean restartOpponent);
    
    /**
     * Set the opponent game
     * 
     * @param o the game of the other player
     */
    public void setOpponentGame(Opponent o);
    
    /**
     * Perform whatever cleanup needs to be done to stop this game. Closes
     * network connections, disposes of frames, etc...
     */
    public void stop();
}
