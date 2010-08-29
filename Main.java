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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Pentris starts
 */
public class Main{
    private static Game game;
    private static Opponent game2;
    private static Thread gameThread;
    private static Thread opponentThread;
    
    /**
     * Starts a regular singleplayer game
     * 
     * @param notUsed Command line parameters
     */
    @SuppressWarnings("nls")
    public static void main(String[] notUsed) {
        game=new Game();
        gameThread=new Thread(game,"Game");
        gameThread.start();
        synchronized (game){
            try{
                if (game.getFrame()==null)
                    game.wait();
                /* Wait for game initialization to finish */
            }catch (InterruptedException e){
                System.exit(1);
            }
        }
        UserIO.addInput(game.getFrame());
        UserIO.setLetterGame(game);
        UserIO.setArrowGame(game);
    }
    
    /**
     * Starts a multiplayer game
     */
    @SuppressWarnings("nls")
    private static void MultiMain() {
        long seed=System.currentTimeMillis();
        game=new Game(seed);
        gameThread=new Thread(game,"Game 1");
        Game tmp=new Game(seed);
        game2=tmp;
        tmp.moveFrameRight();
        opponentThread=new Thread(tmp,"Game 2");
        game.setOpponentGame(game2);
        game2.setOpponentGame(game);
        gameThread.start();
        synchronized (game){
            try{
                if (game.getFrame()==null)
                    game.wait();
                /* Wait for game initialization to finish */
            }catch (InterruptedException e){
                System.exit(1);
            }
        }
        UserIO.addInput(game.getFrame());
        UserIO.setLetterGame(game);
        opponentThread.start();
        synchronized (game2){
            try{
                if (tmp.getFrame()==null)
                    game2.wait();
                /* Wait for game initialization to finish */
            }catch (InterruptedException e){
                System.exit(1);
            }
        }
        UserIO.addInput(tmp.getFrame());
        UserIO.setArrowGame(tmp);
    }
    
    /**
     * Starts the network multiplayer settings dialogue, and starts a network
     * multiplayer game if the user clicks OK.
     */
    @SuppressWarnings("nls")
    public static void startMultiNet() {
        final JFrame frame=new JFrame();
        final JPanel panel=new JPanel();
        final JPanel radioPanel=new JPanel();
        final JRadioButton serverButton=new JRadioButton("Server");
        final JRadioButton clientButton=new JRadioButton("Client");
        ButtonGroup group=new ButtonGroup();
        clientButton.doClick();
        group.add(serverButton);
        group.add(clientButton);
        radioPanel.add(serverButton);
        radioPanel.add(clientButton);
        panel.add(radioPanel);
        final JPanel portPanel=new JPanel();
        final JLabel portLabel=new JLabel("Port: ");
        portPanel.add(portLabel);
        final JTextField portField=new JTextField(5);
        portPanel.add(portField);
        portField.setText("30011");
        panel.add(portPanel);
        final JPanel serverPanel=new JPanel();
        final JLabel serverLabel=new JLabel("Server: ");
        serverPanel.add(serverLabel);
        final JTextField serverField=new JTextField(10);
        serverPanel.add(serverField);
        panel.add(serverPanel);
        final JPanel buttonPanel=new JPanel();
        final JButton OKButton=new JButton("OK");
        buttonPanel.add(OKButton);
        final JButton cancelButton=new JButton("Cancel");
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);
        
        /**
         * Responsible for the button handling in the network multiplayer's
         * settings dialogue
         */
        class netDialogListener implements ActionListener {
            public static final int TYPE_CANCEL=2;
            public static final int TYPE_OKBUTTON=1;
            public static final int TYPE_OTHER=3;
            private int source;
            
            public netDialogListener(int type) {
                source=type;
            }
            
            /*
             * @see
             * java.awt.event.ActionListener#actionPerformed(java.awt.event.
             * ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (source){
                case TYPE_OKBUTTON:
                    startNet(serverButton.isSelected(),Integer
                            .parseInt(portField.getText()),serverField
                            .getText());
                    frame.setVisible(false);
                    frame.dispose();
                    break;
                case TYPE_CANCEL:
                    frame.setVisible(false);
                    frame.dispose();
                    return;
                case TYPE_OTHER:
                    if (serverButton.isSelected())
                        serverField.setEnabled(false);
                    else
                        serverField.setEnabled(true);
                }
            }
        }
        serverButton.addActionListener(new netDialogListener(
                netDialogListener.TYPE_OTHER));
        clientButton.addActionListener(new netDialogListener(
                netDialogListener.TYPE_OTHER));
        OKButton.addActionListener(new netDialogListener(
                netDialogListener.TYPE_OKBUTTON));
        cancelButton.addActionListener(new netDialogListener(
                netDialogListener.TYPE_CANCEL));
        frame.setSize(200,240);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    
    /**
     * Starts a multiplayer game, ending any other games in progress
     */
    public static void startMultiSplit() {
        game.setMp_from_menu(true);
        game.gameOver(true, true);
        game.stop();
        if (game2!=null)
            game2.stop();
        MultiMain();
    }
    
    /**
     * Starts a new network multiplayer game, ending any other games running.
     * 
     * @param server Is this the server
     * @param port The port number on which to listen/connect
     * @param serverName The hostname/address of the server on which to connect
     *        (only useful for clients)
     */
    @SuppressWarnings("nls")
    static void startNet(boolean server,int port,String serverName) {
        long seed=System.currentTimeMillis();
        game.gameOver(true, true);
        game.stop();
        if (game2!=null)
            game2.stop();
        game=new Game(seed);
        gameThread=new Thread(game,"Game");
        NetIO net;
        if (server)
            net=new NetIO(game,port,seed);
        else
            net=new NetIO(game,port,serverName);
        game.setOpponentGame(net);
        opponentThread=new Thread(net,"Net");
        game2=net;
        gameThread.start();
        synchronized (game){
            try{
                if (game.getFrame()==null)
                    game.wait();
                /* Wait for game initialization to finish */
            } catch (InterruptedException e) {
                System.exit(1);
            }
        }
        UserIO.addInput(game.getFrame());
        UserIO.setLetterGame(game);
        UserIO.setArrowGame(game);
        opponentThread.start();
    }
}
