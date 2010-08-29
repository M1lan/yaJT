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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;

/**
 * Display providing a graphical representation of the game to the user. Major
 * GUI Component displaying the Tetripanel
 */
public class GamePanel extends JPanel{
    
    private static final int PREVIEW_Y=60;
    private static final long serialVersionUID=-7238671190592329074L;
    private static final int SQUARE_DISTANCE=20;
    private static final int PREVIEW_SIZE=5*SQUARE_DISTANCE;
    private static final int SQUARE_SIZE=19;
    private static final int TEXT_HEIGHT=10;
    private static final int TEXT_X=40;
    private static final int TEXT_Y=30;
    private final Color colors[]=
            {Color.BLACK, new Color(255,77,0), new Color(3,200,3),
                    new Color(139,0,0), new Color(255,179,102),
                    new Color(179,255,255), new Color(0,0,139),
                    new Color(255,20,147), new Color(0,100,0),
                    new Color(3,3,200), new Color(200,200,200),
                    new Color(139,26,26), new Color(203,102,52),
                    new Color(175,175,202), new Color(200,200,0),
                    new Color(46,138,138), new Color(128,64,191),
                    new Color(158,207,110), new Color(0,204,255),
                    new Color(255,255,194), new Color(255,179,179),
                    new Color(255,102,255), new Color(51,255,153),
                    new Color(51,204,102), new Color(204,102,51),
                    new Color(51,153,204), new Color(51,77,204),
                    new Color(41,184,255), new Color(204,255,102),
                    new Color(177,61,255)};
    private int funRotations;
    private Game game;
    private Grid grid;
    private Polyomino previewPiece;
    
    /**
     * A visual representation
     * 
     * @param ga the game to be shown
     * @param gr the grid of the game logic
     */
    public GamePanel(Game ga,Grid gr) {
        funRotations=0;
        game=ga;
        grid=gr;
    }
    
    /**
     * Displays the playing field and the preview field
     */
    @SuppressWarnings("nls")
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D) g;
        int i;
        long nrLines=game.getLines();
        long score=game.getScore();
        Grid oldGrid=grid;
        if (funRotations!=0){
            grid=grid.clone();
            for (i=0; i<funRotations; i++)
                grid.rotate();
        }
        g2.drawString("Lines: "+nrLines,
                grid.getLength()*SQUARE_DISTANCE+TEXT_X,PREVIEW_SIZE
                        +TEXT_HEIGHT+TEXT_Y);
        g2.drawString("Score: "+score,grid.getLength()*SQUARE_DISTANCE+TEXT_X,
                PREVIEW_SIZE+TEXT_HEIGHT*2+TEXT_Y);
        byte[][] squares=grid.getGrid();
        Rectangle box=
                new Rectangle(SQUARE_DISTANCE,SQUARE_DISTANCE,grid.getLength()
                        *SQUARE_DISTANCE,grid.getHeight()*SQUARE_DISTANCE);
        g2.setColor(Color.BLACK);
        g2.fill(box);
        for (i=0; i<grid.getLength(); i++){
            for (int j=0; j<grid.getHeight(); j++){
                Rectangle pB=
                        new Rectangle(SQUARE_DISTANCE+i*SQUARE_DISTANCE,
                                SQUARE_DISTANCE+j*SQUARE_DISTANCE,SQUARE_SIZE,
                                SQUARE_SIZE);
                g2.setColor(colors[squares[i][j]]);
                g2.fill(pB);
                Color shadowColor=colors[squares[i][j]];
                if (squares[i][j]!=0){
                    for (int k=6; k>0; k--){
                        Ellipse2D.Double shadow=
                                new Ellipse2D.Double(SQUARE_DISTANCE+i
                                        *SQUARE_DISTANCE,SQUARE_DISTANCE+j
                                        *SQUARE_DISTANCE,k,k);
                        shadowColor=shadowColor.brighter();
                        g2.setColor(shadowColor);
                        g2.fill(shadow);
                    }
                }
            }
        }
        int leftDistance=
                grid.getLength()*SQUARE_DISTANCE+PREVIEW_SIZE-SQUARE_DISTANCE;
        Rectangle preV=
                new Rectangle(grid.getLength()*SQUARE_DISTANCE+SQUARE_DISTANCE
                        *2,SQUARE_DISTANCE,PREVIEW_SIZE,PREVIEW_SIZE);
        g2.setColor(Color.BLACK);
        g2.fill(preV);
        if (previewPiece!=null){
            byte[][] positions=previewPiece.getPositions();
            g2.setColor(colors[previewPiece.getIndex()]);
            for (i=0; i<positions.length; i++){
                Rectangle pBlock=
                        new Rectangle(leftDistance+positions[i][0]
                                *SQUARE_DISTANCE,PREVIEW_Y+positions[i][1]
                                *SQUARE_DISTANCE,SQUARE_SIZE,SQUARE_SIZE);
                g2.fill(pBlock);
            }
        }
        if (game.isPaused()){
            g2.setFont(new Font("Serif",Font.BOLD,15));
            g2.setColor(Color.WHITE);
            if (game.getGrid().getLength()>Game.SMALL_GRID_WIDTH)
                g2.drawString("Press P to unpause",grid.getLength()*6,grid
                        .getHeight()*10);
            else
                g2.drawString("PAUSED",grid.getLength()*8,grid.getHeight()*12);
            
        }
        grid=oldGrid;
    }
    
    /**
     * Rotates the whole panel
     */
    public void rotate() {
        funRotations=(funRotations+1)%4;
    }
    
    /**
     * Set the amount of fun rotations
     * 
     * @param r
     */
    public void setFunRotations(int r) {
        funRotations=r;
    }
    
    /**
     * Sets the preview piece
     * 
     * @param p
     */
    public void setPreviewPiece(Polyomino p) {
        previewPiece=p;
    }
    
}
