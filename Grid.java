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
 * Grid class: A 2D representation of the 'pentris' playing field
 */
public class Grid implements Cloneable, Serializable{
    
    private static final long serialVersionUID=-6819543666007073886L;
    private byte[][] grid;
    
    /**
     * Default constructor
     */
    public Grid() {
        grid=new byte[12][30];
    }
    
    /**
     * Constructor for a grid dimension x by y
     * 
     * @param x width
     * @param y height
     */
    public Grid(int x,int y) {
        grid=new byte[x][y];
    }
    
    /**
     * Adds a line of rubble to the bottom, pushing up the rest of the field.
     * 
     * @param openSquare The square to leave open
     */
    public void addBottomLine(int openSquare) {
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid[i].length-1; j++){
                grid[i][j]=grid[i][j+1];
            }
        }
        for (int i=0; i<grid.length; i++){
            if (i!=openSquare)
                grid[i][grid[i].length-1]=1;
            else
                grid[i][grid[i].length-1]=0;
        }
        performGravity();
    }
    
    /**
     * Check if a block can move down a line.
     * 
     * @param mark The mark value of the block
     * @param g
     * @return true if the current block can move down a line
     */
    private boolean blockCanMoveDown(byte mark,byte[][] g) {
        for (int i=0; i<g.length; i++){
            for (int j=0; j<g[0].length; j++){
                if (g[i][j]==mark){
                    if (j+1>=g[i].length)
                        return false;
                    if (g[i][j+1]!=0&&g[i][j+1]!=mark)
                        return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Check whether a certain pentomino can be placed on the playing field
     * 
     * @param p the pentris block
     * @param x x-coordinate of the square on the board
     * @param y y-coordinate of the square on the board
     * @return true if the current piece can be placed at position x/y
     */
    public boolean canPlacePiece(Polyomino p,int x,int y) {
        byte[][] positions=p.getPositions();
        try{
            for (int i=0; i<positions.length; i++){
                if (positions[i][1]+y>=grid[0].length
                        ||positions[i][0]+x>=grid.length||positions[i][0]+x<0
                        ||grid[positions[i][0]+x][positions[i][1]+y]!=0)
                    return false;
            }
        }catch (ArrayIndexOutOfBoundsException e){
            return false;
        }
        return true;
    }
    
    /*
     * Creates a copy of the grid.
     * 
     * @return a copy of the grid.
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public Grid clone() {
        int i;
        Grid g=new Grid();
        g.grid=grid.clone();
        for (i=0; i<g.grid.length; i++)
            g.grid[i]=grid[i].clone();
        return g;
    }
    
    /**
     * Remove a full line from the playing field
     * 
     * @param l the line that has to be deleted
     */
    public void deleteLine(int l) {
        for (int i=0; i<grid.length; i++){
            grid[i][l]=0;
        }
        for (int j=l; j>0; j--){
            for (int k=0; k<grid.length; k++){
                grid[k][j]=grid[k][j-1];
            }
        }
        performGravity();
    }
    
    /**
     * Returns the grid as 2d array of byte
     * 
     * @return the current state of the playing field
     */
    public byte[][] getGrid() {
        return grid;
    }
    
    /**
     * Get the height of the playing field
     * 
     * @return the height of the playing field
     */
    public int getHeight() {
        return grid[0].length;
    }
    
    /**
     * Get the length of the playing field
     * 
     * @return the length of the playing field
     */
    public int getLength() {
        return grid.length;
    }
    
    /**
     * Checks the whole playing field for full rows
     * 
     * @return the line that is complete, or -1 when no line is complete
     */
    public int hasCompleteLine() {
        int line=-2;
        for (int i=0; i<grid[0].length; i++){
            line=-2;
            for (int j=0; j<grid.length; j++){
                if (grid[j][i]==0){
                    line=-1;
                }else if (line!=-1){
                    line=i;
                }
            }
            if (line>=0)
                return line;
        }
        return -1;
    }
    
    /**
     * Attaches marks to connected squares
     * 
     * @param mark The mark value of the block
     * @param g
     * @param x
     * @param y
     */
    private void markConnectedSquares(byte mark,byte[][] g,int x,int y) {
        if (g[x][y]>0){
            g[x][y]=mark;
            if (x+1<g.length)
                markConnectedSquares(mark,g,x+1,y);
            if (x>0)
                markConnectedSquares(mark,g,x-1,y);
            if (y+1<g[0].length)
                markConnectedSquares(mark,g,x,y+1);
            if (y>0)
                markConnectedSquares(mark,g,x,y-1);
        }
    }
    
    /**
     * Drop a marked block a line
     * 
     * @param mark The mark value of the block to move
     * @param markedGrid The grid with the block marked
     */
    private void moveDownBlockMarked(byte mark,byte[][] markedGrid) {
        for (int i=0; i<grid.length; i++){
            for (int j=grid[0].length-1; j>=0; j--){
                if (markedGrid[i][j]==mark){
                    grid[i][j+1]=grid[i][j];
                    grid[i][j]=0;
                    markedGrid[i][j+1]=mark;
                    markedGrid[i][j]=0;
                }
            }
        }
    }
    
    /**
     * Let unsupported squares fall down
     */
    private void performGravity() {
        byte[][] tmpGrid=grid.clone();
        byte mark=-1;
        for (int i=0; i<grid.length; i++)
            tmpGrid[i]=grid[i].clone();
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid[i].length; j++){
                if (tmpGrid[i][j]>0){
                    markConnectedSquares(mark,tmpGrid,i,j);
                    while (blockCanMoveDown(mark,tmpGrid))
                        moveDownBlockMarked(mark,tmpGrid);
                    mark--;
                }
            }
        }
        removeMarks();
    }
    
    /**
     * Procedure that places a pentomino p at the position x/y in the grid.
     * 
     * @param p The Pentomino we want to place
     * @param x x-coordinate in the Grid
     * @param y y-coordinate in the Grid
     */
    public void place(Polyomino p,int x,int y) {
        byte index=(byte) p.getIndex();
        byte[][] positions=p.getPositions();
        for (int i=0; i<positions.length; i++)
            grid[positions[i][0]+x][positions[i][1]+y]=index;
    }
    
    /**
     * Procedure that removes a pentomino p at a position x/y in the grid
     * 
     * @param p The Pentomino we want to place
     * @param x x-coordinate in the Grid
     * @param y y-coordinate in the Grid
     */
    public void remove(Polyomino p,int x,int y) {
        byte[][] positions=p.getPositions();
        for (int i=0; i<positions.length; i++)
            grid[positions[i][0]+x][positions[i][1]+y]=0;
    }
    
    /**
     * Remove marks from all blocks
     */
    private void removeMarks() {
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid[i].length; j++){
                if (grid[i][j]<0)
                    grid[i][j]=(byte) -grid[i][j];
            }
        }
    }
    
    /**
     * Rotates the entire grid
     */
    public void rotate() {
        byte[][] newGrid=new byte[grid[0].length][grid.length];
        int i, j;
        for (i=0; i<newGrid.length; i++){
            for (j=0; j<newGrid[i].length; j++){
                newGrid[i][j]=grid[grid.length-j-1][i];
            }
        }
        grid=newGrid;
    }
    
    /**
     * Returns the grid as 2d array of byte
     * 
     * @return the current state of the playing field
     */
    public void setGrid(byte[][] g) {
        grid=g;
    }
}
