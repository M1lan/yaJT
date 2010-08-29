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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 * The menu bar for the game panels
 */
public class MenuSystem extends JMenuBar implements Serializable{
    
    /**
     * Provide an ActionListener to the menu
     */
    class menulistener implements ActionListener{
        private int m;
        
        public menulistener(int menuItem) {
            m=menuItem;
        }
        
/*
 * (non-Javadoc)
 * 
 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
 * ActionEvent)
 */
        @SuppressWarnings({"nls", "boxing"})
        @Override
        public void actionPerformed(ActionEvent ae) {
            switch (m){
            /* menu game */
            case ITEM_TOGGLEPAUSE:
                if (!game.isPaused())
                    game.pauseGame();
                else
                    game.unPauseGame();
                break;
            case ITEM_RESTART:
                game.restart(true);
                break;
            case ITEM_EXIT:
                System.exit(0);
                break;
            /* menu fun */
            case ITEM_FUNMODE:
                game.setFunMode();
                break;
            case ITEM_VERYFUNMODE:
                game.setVeryFunMode();
                break;
            case ITEM_NOFUNMODE:
                game.setNoFunMode();
                break;
            /* menu music */
            case ITEM_MUSIC_A:
                Sound.toggleMusic("tune_a.mid");
                break;
            case ITEM_MUSIC_B:
                Sound.toggleMusic("tune_b.mid");
                break;
            case ITEM_MUSIC_C:
                Sound.toggleMusic("tune_c.mid");
                break;
            case ITEM_MUSIC_OFF:
                Sound.toggleMusic("off");
                break;
            /* menu board */
            case ITEM_FIELD_SMALL:
                game.changeGridSize(Game.SMALL_GRID_WIDTH,
                        Game.SMALL_GRID_HEIGHT,true);
                break;
            case ITEM_FIELD_MEDIUM:
                game.changeGridSize(Game.MEDIUM_GRID_WIDTH,
                        Game.MEDIUM_GRID_HEIGHT,true);
                break;
            case ITEM_FIELD_LARGE:
                game.changeGridSize(Game.LARGE_GRID_WIDTH,
                        Game.LARGE_GRID_HEIGHT,true);
                break;
            case ITEM_CPU_PLAYER:
                game.setCpuPlayer(true);
                if (game.isPaused())
                    game.unPauseGame();
                break;
            case ITEM_HUMAN_PLAYER:
                game.setCpuPlayer(false);
                break;
            case ITEM_PIECES_PENTO_ONLY:
                game.setAllowedPieces(true,false,false);
                break;
            case ITEM_PIECES_ALL:
                game.setAllowedPieces(true,true,true);
                break;
            case ITEM_PIECES_NONPENTO:
                game.setAllowedPieces(false,true,true);
                break;
            case ITEM_PIECES_TETRO:
                game.setAllowedPieces(false,true,false);
                break;
            case ITEM_MULTI_SPLIT:
                Main.startMultiSplit();
                break;
            case ITEM_MULTI_NET:
                Main.startMultiNet();
                break;
            case ITEM_SAVE:

                try{
                    game.saveGame();
                }catch (IOException e1){
                    e1.printStackTrace();
                }
                
                break;
            case ITEM_LOAD:

                try{
                    game.loadGame();
                }catch (IOException e){
                    e.printStackTrace();
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }
                
                break;
            default:
                System.out.format("Menu %d pressed\n",m);
                break;
            }
        }
    }
    
    private static final int ITEM_CPU_PLAYER=13;
    private static final int ITEM_EXIT=2;
    private static final int ITEM_FIELD_LARGE=12;
    private static final int ITEM_FIELD_MEDIUM=11;
    private static final int ITEM_FIELD_SMALL=10;
    private static final int ITEM_FUNMODE=3;
    private static final int ITEM_HUMAN_PLAYER=14;
    private static final int ITEM_LOAD=22;
    private static final int ITEM_MULTI_NET=20;
    private static final int ITEM_MULTI_SPLIT=19;
    private static final int ITEM_MUSIC_A=6;
    private static final int ITEM_MUSIC_B=7;
    private static final int ITEM_MUSIC_C=8;
    private static final int ITEM_MUSIC_OFF=9;
    private static final int ITEM_NOFUNMODE=5;
    private static final int ITEM_PIECES_ALL=16;
    private static final int ITEM_PIECES_NONPENTO=17;
    private static final int ITEM_PIECES_PENTO_ONLY=15;
    private static final int ITEM_PIECES_TETRO=18;
    private static final int ITEM_RESTART=1;
    private static final int ITEM_SAVE=21;
    private static final int ITEM_TOGGLEPAUSE=0;
    
    private static final int ITEM_VERYFUNMODE=4;
    
    private static final long serialVersionUID=928942017036194159L;
    Game game;
    private JMenuItem ldEntry;
    private JMenu playerMenu;
    private JMenuItem pauseEntry;
    
    private JMenuItem savEntry;
    
    /**
     * Creates a new menu bar for a game.
     * 
     * @param game The game to which the menu options should apply
     */
    @SuppressWarnings("nls")
    public MenuSystem(Game game) {
        super();
        this.game=game;
        JMenu gamemenu;
        JMenuItem mEntry;
        JRadioButtonMenuItem radioEntry;
        ButtonGroup playerGroup=new ButtonGroup();
        ButtonGroup piecesGroup=new ButtonGroup();
        
        gamemenu=new JMenu("Game");
        gamemenu.setMnemonic(KeyEvent.VK_G);
        add(gamemenu);
        
        savEntry=new JMenuItem("Save",KeyEvent.VK_S);
        savEntry.addActionListener(new menulistener(ITEM_SAVE));
        gamemenu.add(savEntry);
        ldEntry=new JMenuItem("Load",KeyEvent.VK_S);
        ldEntry.addActionListener(new menulistener(ITEM_LOAD));
        gamemenu.add(ldEntry);
        radioEntry=new JRadioButtonMenuItem("Use pentominoes only");
        radioEntry.setSelected(true);
        radioEntry.addActionListener(new menulistener(ITEM_PIECES_PENTO_ONLY));
        piecesGroup.add(radioEntry);
        gamemenu.add(radioEntry);
        radioEntry=new JRadioButtonMenuItem("Use non-pentominoes only");
        radioEntry.addActionListener(new menulistener(ITEM_PIECES_NONPENTO));
        piecesGroup.add(radioEntry);
        gamemenu.add(radioEntry);
        radioEntry=new JRadioButtonMenuItem("Use tetrominoes only");
        radioEntry.addActionListener(new menulistener(ITEM_PIECES_TETRO));
        piecesGroup.add(radioEntry);
        gamemenu.add(radioEntry);
        radioEntry=new JRadioButtonMenuItem("Use all pieces");
        radioEntry.addActionListener(new menulistener(ITEM_PIECES_ALL));
        piecesGroup.add(radioEntry);
        gamemenu.add(radioEntry);
        gamemenu.addSeparator();
        pauseEntry=new JMenuItem("Toggle Pause",KeyEvent.VK_R);
        pauseEntry.addActionListener(new menulistener(ITEM_TOGGLEPAUSE));
        gamemenu.add(pauseEntry);
        mEntry=new JMenuItem("Restart",KeyEvent.VK_T);
        mEntry.addActionListener(new menulistener(ITEM_RESTART));
        gamemenu.add(mEntry);
        gamemenu.addSeparator();
        mEntry=new JMenuItem("Exit",KeyEvent.VK_X);
        mEntry.addActionListener(new menulistener(ITEM_EXIT));
        gamemenu.add(mEntry);
        
        gamemenu=new JMenu("Fun");
        gamemenu.setMnemonic(KeyEvent.VK_F);
        add(gamemenu);
        mEntry=new JMenuItem("Fun mode");
        mEntry.addActionListener(new menulistener(ITEM_FUNMODE));
        gamemenu.add(mEntry);
        mEntry=new JMenuItem("Very fun mode");
        mEntry.addActionListener(new menulistener(ITEM_VERYFUNMODE));
        gamemenu.add(mEntry);
        mEntry=new JMenuItem("Normal mode");
        mEntry.addActionListener(new menulistener(ITEM_NOFUNMODE));
        gamemenu.add(mEntry);
        
        gamemenu=new JMenu("Music");
        gamemenu.setMnemonic(KeyEvent.VK_M);
        add(gamemenu);
        mEntry=new JMenuItem("Tune A",KeyEvent.VK_1);
        mEntry.addActionListener(new menulistener(ITEM_MUSIC_A));
        gamemenu.add(mEntry);
        mEntry=new JMenuItem("Tune B",KeyEvent.VK_2);
        mEntry.addActionListener(new menulistener(ITEM_MUSIC_B));
        gamemenu.add(mEntry);
        mEntry=new JMenuItem("Tune C",KeyEvent.VK_2);
        mEntry.addActionListener(new menulistener(ITEM_MUSIC_C));
        gamemenu.add(mEntry);
        gamemenu.addSeparator();
        mEntry=new JMenuItem("Off",KeyEvent.VK_2);
        mEntry.addActionListener(new menulistener(ITEM_MUSIC_OFF));
        gamemenu.add(mEntry);
        
        gamemenu=new JMenu("Board");
        gamemenu.setMnemonic(KeyEvent.VK_B);
        add(gamemenu);
        mEntry=new JMenuItem("Small",KeyEvent.VK_S);
        mEntry.addActionListener(new menulistener(ITEM_FIELD_SMALL));
        gamemenu.add(mEntry);
        mEntry=new JMenuItem("Medium",KeyEvent.VK_M);
        mEntry.addActionListener(new menulistener(ITEM_FIELD_MEDIUM));
        gamemenu.add(mEntry);
        mEntry=new JMenuItem("Large",KeyEvent.VK_L);
        mEntry.addActionListener(new menulistener(ITEM_FIELD_LARGE));
        gamemenu.add(mEntry);
        
        playerMenu=new JMenu("Player");
        playerMenu.setMnemonic(KeyEvent.VK_P);
        radioEntry=new JRadioButtonMenuItem("CPU player");
        radioEntry.addActionListener(new menulistener(ITEM_CPU_PLAYER));
        playerGroup.add(radioEntry);
        playerMenu.add(radioEntry);
        radioEntry=new JRadioButtonMenuItem("Human player");
        radioEntry.setSelected(true);
        radioEntry.addActionListener(new menulistener(ITEM_HUMAN_PLAYER));
        playerGroup.add(radioEntry);
        playerMenu.add(radioEntry);
        add(playerMenu);
        
        gamemenu=new JMenu("Multiplayer");
        gamemenu.setMnemonic(KeyEvent.VK_M);
        add(gamemenu);
        mEntry=new JMenuItem("Split screen",KeyEvent.VK_S);
        mEntry.addActionListener(new menulistener(ITEM_MULTI_SPLIT));
        gamemenu.add(mEntry);
        mEntry=new JMenuItem("Network",KeyEvent.VK_N);
        mEntry.addActionListener(new menulistener(ITEM_MULTI_NET));
        gamemenu.add(mEntry);
    }
    
    /**
     * Disables the 'player' menu. Used to prevent cheating in network games,
     * for example.
     */
    public void disablePlayerMenu() {
        playerMenu.setEnabled(false);
    }
    
    /**
     * Disables the save and load options. Those are intended for singleplayer.
     */
    public void disableSaveLoadMenu() {
        savEntry.setEnabled(false);
        ldEntry.setEnabled(false);
    }
    
    /**
     * Disables the pause option in the game menu.
     */
    public void disablePause() {
        pauseEntry.setEnabled(false);
    }
    
}
