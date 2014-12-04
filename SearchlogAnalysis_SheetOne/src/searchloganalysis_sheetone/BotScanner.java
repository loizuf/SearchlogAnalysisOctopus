/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchloganalysis_sheetone;

import java.util.EnumSet;

/**
 *
 * @author Soeren
 */
public class BotScanner {
    
    
    private static int sessionId = 0;
    private static int userId = 0;
    private static int lastUserId;
    private static int wordCount;
    private static int currentRank;
    private static int totalUsers = 0;
    private static int totalClicks = 0;
    private static int queriesPerSession = 1;
    private static int queriesLastSession = 1;
    private static long timeSinceLastInteraction;
    private static long sessionLengthTime;
    private static long lastSessionLengthTime;
    private static long epoc;
    private static long lastEpoc;
    private static String query;
    private static String[] queryTokens;
    
    
    /*
     * Flags
     */    
    private enum Flag {HUMAN, QUERY_PER_SESSION, QUERY_DISTANCE, QUERY_LENGTH, SESSION_LENGTH, VARIETY};
    
    public static EnumSet<Flag> checkQuery(String query, String[] queryTokens){
        EnumSet<Flag> flags = null;
        flags.add(Flag.HUMAN);
        
        return flags;
    }
    
    public static EnumSet<Flag> testFunction(){
        EnumSet<Flag> flags = null;
        flags.add(Flag.HUMAN);
        
        return flags;
    }
}
