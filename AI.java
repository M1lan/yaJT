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
import java.io.Serializable;

/**
 * A CPU player
 */
public class AI implements Serializable{
    /**
     * Inner class representing one possible way to place a stone
     */
    private class Placement{
        public int lowestLine;
        public int nrFilled;
        public int nrGaps;
        public int nrLines;
        public int rotation;
        public int x;
    }
    
    private static final byte GRID_TRACE_VALUE=127;
    
    private static final long serialVersionUID=-6830341963144442684L;
    private Game game;
    
    /**
     * Initializes the game AI
     * 
     * @param game The game for which this AI will calculate positions.
     */
    public AI(Game game) {
        this.game=game;
    }
    
    /**
     * Counts bad gaps on the grid. A gap is defined as any number of empty
     * squares in a column with a non-empty square above them.
     * 
     * @param g The 2D array representation of the grid
     * @return the number of gaps
     */
    private int countBadGaps(byte[][] g) {
        int nrOfGaps=0;
        for (int x=0; x<g.length; x++)
            nrOfGaps+=gapsInColumn(x,g);
        return nrOfGaps;
    }
    
    /**
     * Checks for gaps in one column. A gap is defined as any number of empty
     * squares in a column with a non-empty square above them.
     * 
     * @param x The column in which to loop
     * @param g The 2D representation of the grid
     * @return the amount of gaps in a column
     */
    private int gapsInColumn(int x,byte[][] g) {
        int ret=0;
        int y=0;
        boolean placedAbove=false;
        while (y<g[x].length){
            if (g[x][y]!=0&&g[x][y]!=GRID_TRACE_VALUE)
                placedAbove=true;
            if (g[x][y]!=0)
                break;
            y++;
        }
        while (y<g[x].length){
            if (g[x][y]==0){
                if (y==0||g[x][y-1]!=0)
                    ret++;
            }
            y++;
        }
        if (placedAbove)
            return ret==0 ? 0: ret+1;
        return ret;
    }
    
    /**
     * Gets the best possible combination of the placement of the current piece
     * and the next piece
     * 
     * @param grid The grid on which the piece must be placed
     * @param p The piece to place
     * @param previewP The preview piece
     * @return All information about the best placement found
     */
    /* Why can't JAVA just have normal structures like any other language? */
    private Placement getBestPos(Grid grid,Polyomino p,Polyomino previewP) {
        int x, rot, y;
        int bestX=-1, bestRot=-1, bestFilled=-1;
        int bestLowest=-1, bestLines=0, bestGaps=10000;
        int i, j;
        int rotcnt=p.getRotCount();
        Placement ret=new Placement();
        for (x=0; x<grid.getLength(); x++){
            for (rot=0; rot<rotcnt; rot++, p.rotate()){
                Grid cGrid=grid.clone();
                byte[][] g=cGrid.getGrid();
                int nrFilled=0, nrGaps=0, nrLines=0;
                int lowestLine=-1;
                if (!cGrid.canPlacePiece(p,x,2)){
                    continue;
                }
                for (y=2; y<1000; y++){
                    if (!cGrid.canPlacePiece(p,x,y))
                        break;
                }
                for (i=0; i<g.length; i++){
                    for (j=0; j<g[0].length; j++){
                        if (g[i][j]!=0)
                            g[i][j]=GRID_TRACE_VALUE;
                    }
                }
                cGrid.place(p,x,y-1);
                for (i=0; i<g.length; i++){
                    j=lowestLine==-1 ? 0: lowestLine;
                    for (; j<g[0].length; j++){
                        if (g[i][j]!=0&&g[i][j]!=GRID_TRACE_VALUE){
                            if (j>lowestLine){
                                lowestLine=j;
                                nrFilled=0;
                            }
                            if (j==lowestLine)
                                nrFilled++;
                        }
                    }
                }
                do{
                    i=cGrid.hasCompleteLine();
                    if (i>0){
                        cGrid.deleteLine(i);
                        nrLines++;
                    }
                }while (i>0);
                if (previewP!=null){
                    Grid tmpGrid=cGrid.clone();
                    byte[][] tmpArray=tmpGrid.getGrid();
                    for (i=0; i<tmpArray.length; i++){
                        for (j=0; j<tmpArray[0].length; j++){
                            if (tmpArray[i][j]!=0)
                                tmpArray[i][j]=GRID_TRACE_VALUE;
                        }
                    }
                    Placement previewPlacement=getBestPos(cGrid,previewP,null);
                    if (previewPlacement.x>=0){
                        if (lowestLine==previewPlacement.lowestLine)
                            nrFilled+=previewPlacement.nrFilled;
                        nrLines+=previewPlacement.nrLines;
                    }else{
                        nrGaps=g.length*g[0].length;
                    }
                }else{
                    nrGaps=countBadGaps(g);
                }
                if (bestGaps>=nrGaps){
                    if (nrLines>bestLines||lowestLine>bestLowest
                            ||(lowestLine==bestLowest&&nrFilled>bestFilled)
                            ||bestGaps>nrGaps){
                        bestX=x;
                        bestRot=rot;
                        bestFilled=nrFilled;
                        bestLines=nrLines;
                        bestGaps=nrGaps;
                        bestLowest=lowestLine;
                    }
                }
            }
        }
        ret.x=bestX;
        ret.rotation=bestRot;
        ret.nrFilled=bestFilled;
        ret.nrGaps=bestGaps;
        ret.lowestLine=bestLowest;
        ret.nrLines=bestLines;
        return ret;
    }
    
    /**
     * Compares different placements and calculates which of them is the best
     * one so far, then places the active pentomino at that point. Also takes
     * into account combinations with the current preview piece
     * 
     * @param p The active pentomino
     * @param previewP The preview pentomino
     */
    public void place(Polyomino p,Polyomino previewP) {
        int x, rotation, i;
        final Grid tmpGrid=game.getGrid().clone();
        final byte[][] gridValues=tmpGrid.getGrid();
        for (i=0; i<gridValues.length; i++){
            for (int j=0; j<gridValues[0].length; j++){
                if (gridValues[i][j]!=0)
                    gridValues[i][j]=GRID_TRACE_VALUE;
            }
        }
        Placement bestPlacement=getBestPos(tmpGrid,p,previewP);
        x=bestPlacement.x;
        rotation=bestPlacement.rotation;
        for (i=0; i<rotation; i++){
            while (!game.doRotate()){
                if (!game.move(0,1))
                    return;
            }
        }
        if (x<tmpGrid.getLength()/2){
            int nrMoved=tmpGrid.getLength()/2-x;
            for (i=0; i<nrMoved; i++)
                game.moveLeft();
        }else{
            int nrMoved=x-tmpGrid.getLength()/2;
            for (i=0; i<nrMoved; i++)
                game.moveRight();
        }
        while (game.move(0,1)){/* fall down if possible */}
    }
}
