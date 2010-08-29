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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 * Creates a highscore list when the game is over and displays it to the player
 */
@SuppressWarnings("nls")
public class HighScores{
    
    private static String[] names;
    private static final String scoreFileName="scores.txt";
    private static long[] scores;
    
    /**
     * Ask the user for his name if he deserves to be in the highscore list
     * 
     * @param score
     */
    public static void addScore(long score) {
        for (int i=0; i<10; i++){
            if (score>scores[i]){
                for (int j=9; j>i; j--){
                    names[j]=names[j-1];
                    scores[j]=scores[j-1];
                }
                scores[i]=score;
                names[i]=
                        JOptionPane.showInputDialog(null,"High Score",
                                "You have a high score. What is your name?",
                                JOptionPane.QUESTION_MESSAGE);
                break;
            }
        }
    }
    
    /**
     * Reads the high scores from the text file.
     */
    public static void readHighScores() {
        FileReader reader;
        File f=new File(scoreFileName);
        try{
            f.createNewFile();
        }catch (IOException e1){
            System.exit(1);
        }
        try{
            reader=new FileReader(scoreFileName);
        }catch (FileNotFoundException e){
            return;
            
        }
        names=new String[10];
        scores=new long[10];
        Scanner in=new Scanner(reader);
        for (int i=0; i<10; i++){
            try{
                scores[i]=in.nextInt();
                names[i]=in.nextLine();
                while (names[i].length()>0&&names[i].charAt(0)==' ')
                    names[i]=names[i].substring(1,names[i].length());
            }catch (NoSuchElementException e){
                break;
            }
        }
        in.close();
    }
    
    /**
     * Displays the score list
     */
    public static void showHighScores() {
        String message="";
        for (int i=0; i<10; i++){
            if (names[i]!=null)
                message+=names[i]+": "+scores[i]+"\n";
        }
        JOptionPane.showMessageDialog(null,message,"High scores",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * outputs the scores to .txt
     */
    @SuppressWarnings("null")
    public static void writeHighScores() {
        PrintWriter writer=null;
        try{
            writer=new PrintWriter(scoreFileName);
        }catch (FileNotFoundException e){
            System.out.println("Your system is horribly broken");
            System.exit(1); // nice way to avoid null pointer exceptions ;-)
        }
        for (int i=0; i<10; i++){
            if (names[i]!=null)
                writer.println(scores[i]+" "+names[i]);
        }
        writer.close();
    }
}
