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
 * User Input/Output class: Handles keyboard input from the frames and
 * translates them into the appropriate game commands.
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

public class UserIO implements KeyListener{
    private static Game arrowGame;
    private static Game letterGame;
    
    /**
     * Adds the key listener to a frame
     * 
     * @param frame The frame on which to listen
     */
    public static void addInput(JFrame frame) {
        frame.addKeyListener(new UserIO());
    }
    
    /**
     * Switch input controls of game g to arrow keys
     * 
     * @param g The game which should respond to arrow keys
     */
    public static void setArrowGame(Game g) {
        arrowGame=g;
    }
    
    /**
     * Switch input controls to R D F G keys
     * 
     * @param g The game which should respond to the letter keys
     */
    public static void setLetterGame(Game g) {
        letterGame=g;
    }
    
    /*
     * Invoked when a key has been pressed: Use the arrow-keys left, right and
     * up to move and rotate the pentomino. Down-arrow makes it fall faster,
     * space drops. Alternatively, use keys 'D', 'G', 'F' and 'R'.
     */
    /*
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int kc=e.getKeyCode();
        switch (kc){
        case KeyEvent.VK_F:
            if (letterGame!=null&&!letterGame.isCpuPlayer())
                letterGame.actionPerformed(null);
            break;
        case KeyEvent.VK_DOWN:
            if (arrowGame!=null&&!arrowGame.isCpuPlayer())
                arrowGame.actionPerformed(null);
            break;
        case KeyEvent.VK_R:
            if (letterGame!=null&&!letterGame.isCpuPlayer())
                letterGame.doRotate();
            break;
        case KeyEvent.VK_UP:
            if (arrowGame!=null&&!arrowGame.isCpuPlayer())
                arrowGame.doRotate();
            break;
        case KeyEvent.VK_D:
            if (letterGame!=null&&!letterGame.isCpuPlayer())
                letterGame.moveLeft();
            break;
        case KeyEvent.VK_LEFT:
            if (arrowGame!=null&&!arrowGame.isCpuPlayer())
                arrowGame.moveLeft();
            break;
        case KeyEvent.VK_G:
            if (letterGame!=null&&!letterGame.isCpuPlayer())
                letterGame.moveRight();
            break;
        case KeyEvent.VK_RIGHT:
            if (arrowGame!=null&&!arrowGame.isCpuPlayer())
                arrowGame.moveRight();
            break;
        case KeyEvent.VK_SPACE:
            if (letterGame!=null&&!letterGame.isCpuPlayer()
                    &&!letterGame.isGameover())
                letterGame.dropDown();
            break;
        case KeyEvent.VK_END:
            if (arrowGame!=null&&!arrowGame.isCpuPlayer()
                    &&!arrowGame.isGameover())
                arrowGame.dropDown();
            break;
        }
    }
    
    /*
     * Notice: For key pressed and key released events, the getKeyCode method
     * returns the event's keyCode. For key typed events, the getKeyCode method
     * always returns VK_UNDEFINED. Therefore, we cannot use the higher-level
     * 'keyTyped' for arrow-keys. Luckily, keyPressed w/ non-handled keyReleased
     * does just the same. (non-Javadoc)
     */
    /*
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {/* left unhandled */}
    
    /*
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
        char c=e.getKeyChar();
        switch (c){
        case 'p':
        case 'P':
            boolean pause;
            if (letterGame!=null)
                pause=!letterGame.isPaused();
            else if (arrowGame!=null)
                pause=!arrowGame.isPaused();
            else
                return;
            
            if (pause){
                if (letterGame!=null)
                    letterGame.pauseGame();
                if (arrowGame!=null)
                    arrowGame.pauseGame();
            }else{
                if (arrowGame!=null)
                    arrowGame.unPauseGame();
                if (letterGame!=null)
                    letterGame.unPauseGame();
            }
            break;
        }
    }
}
