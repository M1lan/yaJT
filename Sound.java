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
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;

/**
 * Plays back MIDI files for background music.
 */

public class Sound{
    private static String currentFile;
    
    private static Sequencer sequencer;
    
    /**
     * Toggles Tetris midi. Note that this method will fail silently on any
     * errors.
     * 
     * @param fileStr The file to play, or "off" to stop playing
     */
    @SuppressWarnings("nls")
    public static void toggleMusic(String fileStr) {
        if (sequencer==null){
            try{
                sequencer=MidiSystem.getSequencer();
                currentFile=null;
            }catch (Exception e){
                return;
                /* No sound, so what? */
            }
        }
        if (currentFile!=null&&fileStr.equals(currentFile))
            return; /* We're already playing this file */
        currentFile=fileStr;
        
        if (sequencer.isOpen()){
            sequencer.stop();
            sequencer.close();
        }
        if (fileStr.equals("off"))
            return;
        
        File sound=new File(fileStr);
        
        try{
            sequencer.setSequence(MidiSystem.getSequence(sound));
            sequencer.open();
        }catch (Exception e){
            /* No sound, who really cares? */
        }
        sequencer.setLoopCount(Integer.MAX_VALUE);
        sequencer.start();
        currentFile=fileStr;
    }
}
