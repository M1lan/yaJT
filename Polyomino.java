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
import java.util.Random;

/**
 * The definitions of all 28 pieces used in the game
 */
public class Polyomino implements Cloneable, Serializable{
    
    private static final double pento_chance=0.4;
    private static final long serialVersionUID=-1160170678090733195L;
    private byte[][] currentP;
    private int index;
    private byte[][][] polyomino={

    // The 'Minoes':
            
            {{0, 0}, {-1, 0}}, // a domino
            
            {{0, 0}, {0, 1}, {1, 0}}, // a tromino
            {{0, 0}, {1, 0}, {-1, 0}}, // a tromino
            // Tetro-
            {{0, 0}, {-1, 0}, {1, 0}, {2, 0}}, // I
            {{0, 0}, {0, 1}, {0, -1}, {-1, -1}}, // J
            {{0, 0}, {0, 1}, {0, -1}, {1, -1}}, // L
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}}, // O
            {{0, 0}, {0, 1}, {-1, 0}, {1, 1}}, // S
            {{0, 0}, {0, 1}, {1, 0}, {-1, 1}}, // Z
            {{0, 0}, {0, 1}, {-1, 0}, {1, 0}}, // T
            // Pento-
            {{0, 0}, {0, 1}, {0, -1}, {1, 1}, {1, 0}}, // P
            {{0, 0}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}}, // X
            {{0, 0}, {-1, 0}, {0, -1}, {0, 1}, {1, -1}}, // F
            {{1, 1}, {1, 0}, {1, -1}, {0, 1}, {-1, 1}}, // V
            {{0, 0}, {-1, 0}, {-1, 1}, {0, -1}, {1, -1}}, // W
            {{0, 0}, {1, 0}, {0, 1}, {0, -1}, {0, 2}}, // Y
            {{0, 0}, {-1, 0}, {-2, 0}, {1, 0}, {2, 0}}, // I
            {{0, 0}, {0, -1}, {0, 1}, {1, 1}, {-1, 1}}, // T
            {{0, 0}, {0, 1}, {-1, 1}, {0, -1}, {1, -1}}, // Z
            {{0, 0}, {1, 0}, {1, 1}, {-1, 0}, {-1, 1}}, // U
            {{0, 0}, {0, 1}, {0, 2}, {-1, 0}, {-1, -1}}, // N
            {{0, 0}, {0, 1}, {0, 2}, {0, -1}, {1, -1}}, // L
            {{0, 0}, {0, 1}, {0, 2}, {0, -1}, {-1, -1}}, // L-flip
            {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, -1}}, // N-flip
            {{0, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}}, // F-flip
            {{0, 0}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}}, // P-flip
            {{0, 0}, {-1, 0}, {0, 1}, {0, -1}, {0, 2}}, // Y-flip
            {{0, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}}, // S
    };
    
    /**
     * Creates a random polyomino, using a provided RNG
     * 
     * @param allowPento Allow the creation of a piece of 5 blocks
     * @param allowTetro Allow the creation of a piece of 4 blocks
     * @param allowOther Allow the creation of a piece with 2 or 3 blocks
     * @param randomGenerator The RNG to use to generate the polyomino number
     */
    public Polyomino(boolean allowPento,boolean allowTetro,boolean allowOther,
            Random randomGenerator) {
        currentP=null;
        while (currentP==null){
            int nr=randomGenerator.nextInt(polyomino.length);
            currentP=polyomino[nr];
            if (currentP.length==5&&!allowPento){
                currentP=null;
                continue;
            }
            if (currentP.length==4&&!allowTetro){
                currentP=null;
                continue;
            }
            if (currentP.length!=4&&currentP.length!=5&&!allowOther){
                currentP=null;
                continue;
            }
            if (currentP.length==5&&allowTetro
                    &&randomGenerator.nextDouble()>pento_chance){
                currentP=null;
                continue;
            }
            index=nr+1;
        }
    }
    
    /**
     * Constructor
     * 
     * @param p piece number
     */
    public Polyomino(int p) {
        currentP=polyomino[p-1];
        index=p;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        Polyomino tmp=new Polyomino(index);
        tmp.currentP=currentP.clone();
        return tmp;
    }
    
    /**
     * Gets the current piece's identifier
     * 
     * @return index piece-identifier
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Gets the position of the current piece's squares
     * 
     * @return the piece definition
     */
    public byte[][] getPositions() {
        return currentP;
    }
    
    /**
     * Get the number of unique rotations of this polyomino.
     * 
     * @return the number of unique rotations
     */
    public int getRotCount() {
        switch (index){
        case 1:
        case 4:
        case 8:
        case 9:
        case 12:
        case 17:
            return 2;
        case 7:
            return 1;
        default:
            return 4;
        }
    }
    
    /**
     * Rotates the current piece by 90 degrees Rotations for piece
     * 1,4,17,8,9,7,12 are special: some only need to be flipped back and forth,
     * others do not rotate at all
     */
    public void rotate() {
        byte[][] store=new byte[currentP.length][2];
        switch (index){
        case 1:
        case 4:
        case 17:
            for (int i=1; i<currentP.length; i++){
                store[i][0]=currentP[i][1];
                store[i][1]=currentP[i][0];
            }
            currentP=store;
            break;
        case 8:
        case 9:
            for (int i=1; i<currentP.length; i++){
                store[i][0]=currentP[i][0];
                store[i][1]=currentP[i][1];
            }
            store[2][0]=(byte) (currentP[2][0]*-1);
            store[3][1]=(byte) (currentP[3][1]*-1);
            currentP=store;
            break;
        case 7:
        case 12:
            break;
        default:
            for (int i=0; i<currentP.length; i++){
                store[i][0]=currentP[i][1];
                store[i][1]=(byte) (currentP[i][0]*-1);
            }
            currentP=store;
            break;
        }
    }
}
