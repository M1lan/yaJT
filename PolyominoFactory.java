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
 * Creates random polyominoes
 */
public class PolyominoFactory implements Serializable{
    
    private static final long serialVersionUID=3823781589749354178L;
    private Random randomGenerator;
    
    /**
     * Creates a new factory with a random seed
     */
    public PolyominoFactory() {
        randomGenerator=new Random();
    }
    
    /**
     * Creates a new Polyomino factory with a specified seed
     * 
     * @param seed The seed for the RNG
     */
    public PolyominoFactory(long seed) {
        randomGenerator=new Random(seed);
    }
    
    /**
     * Creates a random polyomino
     * 
     * @param allowPento Allow the creation of a piece of 5 blocks
     * @param allowTetro Allow the creation of a piece of 4 blocks
     * @param allowOther Allow the creation of a piece with 2 or 3 blocks
     */
    public Polyomino next(boolean allowPento,boolean allowTetro,
            boolean allowOther) {
        return new Polyomino(allowPento,allowTetro,allowOther,randomGenerator);
    }
}
