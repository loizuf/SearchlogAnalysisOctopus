package searchloganalysis_sheetone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This program reads a TextFile, converts it to an R-friendly format and writes the result in a new .txt
 */
public class SearchlogAnalysis_SheetOne {
    
    /*
     * Different Constants for different PC's
     * 1. Laptop
     * 2. Desktop at Home
     * 3. CipPool (typical Location)
     */
    
    private static final int PC_TYPE = 2;
    
    /*
     * Boolean constants for writing things
     */
    
    private static final boolean BUILD_CSV_FILE = true;
    private static final boolean BUILD_DAILY_COUNT_MATRIX_FILE = true;
    private static final boolean BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE = false;
    private static final boolean BUILD_QUERY_TOKEN_FILE = false;
    private static final boolean BUILD_QUERY_PAIR_FILE = true;
    
    /*
     * Variables for Output
     */
    private static String lastEntry = "";
    
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
    private static boolean changedSession = false;
    
    /*
     * Variables for matrix
     */
    private static DailyCountMatrixBuilder matrixBuilder;
    
    /*
     * Variables for Query Pairs
     */
    private static HashMap<String, Long> queryPairs = new HashMap<>();
    private static boolean newUser = true;
    

    public static void main(String[] args) {
        
        // This part is only necessary for the first sheet. Everything final should be done here and all information should be written in the result.txt
        matrixBuilder = new DailyCountMatrixBuilder(Util.getTotalDays(), BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE);
        resetVariables();
        
        readBigData();
        
        if(BUILD_DAILY_COUNT_MATRIX_FILE){
            System.out.println("before writing starts");
            matrixBuilder.writeFile(BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE);
        }
        
        System.out.println("done");
    }
    
    /* Help functions */
    private static Writer registerWriter(String resultFileName) {
        Writer writer = null;
        
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFileName), Util.getEncoding()));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return writer;
    }

    private static void closeWriter(Writer writer) {
        try {
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static int[] getRandomUserIDs() {
        Random r = new Random();
        int[] randomArray = new int[Util.getSampleSize()];
        for(int i = 0; i<randomArray.length; i++){
            randomArray[i] = r.nextInt(totalUsers+1);
        }
        return randomArray;
    }

    private static void resetVariables() {
        query = "";
        //rawDate = "";
        //generatedJavaDate = null;
        timeSinceLastInteraction = 0;
        lastSessionLengthTime = 0;
        queriesLastSession = 1;
        currentRank = 0;
        changedSession = false;
        newUser = false;
    }
    
    private static void writeCSVHeader(Scanner scanner, Writer writer) {
        writeResults(writer, Util.getHeader());
    }

    private static void escapeQueryCharacters(String query) {
        query = query.replaceAll("\"", "\"\"");
        query = query.replaceAll("\'", "\\\\\'");
        query = query.replaceAll(",", "\\\\,");
    }
    /* End help-functions */
    
    /* Data is read, analyzed and written into a result file */ 
    private static void readBigData() {
        Scanner scanner;
        try { 
            scanner = new Scanner(new File(Util.getDataLocation(PC_TYPE)), Util.getEncoding());
            scanner.nextLine();
            createResultFile(scanner);
            scanner.close();
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * The read lines get splittet, variables get set and with them a new String (new line) gets created and written in the results.txt
     */
    private static void createResultFile(Scanner scanner) {
        Writer writerCSV;
        Writer writerTokens;
        Writer writerPairs;
        // wirter gets initialized only when the file gets written to prevent deleting the old file in the same location
        
        if(BUILD_CSV_FILE){
            writerCSV = registerWriter(Util.getCSVLocation());
            writeCSVHeader(scanner, writerCSV);
        }
        if(BUILD_QUERY_TOKEN_FILE){
            writerTokens = registerWriter(Util.getQueryTokenFileLocation());
        }
        if(BUILD_QUERY_PAIR_FILE){
            writerPairs = registerWriter(Util.getQueryPairFileLocation());
        }
        
        while(scanner.hasNextLine()){
            String[] currentTokens = getNextTokens(scanner);
            String newEntry;
            
            /* Start getting the data of one user and writing it in a file*/
            
            // This sets local variables which are used later on
            setVariables(currentTokens);
            
            // advances session and counts some variables for measuring the session length
            changedSession = changeSessionIfNecessary();
            if(changedSession){
                sessionLengthTime = 0;
                queriesPerSession = 1;
            } else {
                sessionLengthTime += timeSinceLastInteraction;
                lastSessionLengthTime = sessionLengthTime;
                queriesPerSession++;
                queriesLastSession = queriesPerSession;
            }
            
            // writes new line in file
            if(BUILD_CSV_FILE){
                newEntry = buildNewEntry();
                if(!changedSession){
                    addNonValuesToString();
                }
                if(lastEntry != null){
                    writeResults(writerCSV, lastEntry);
                }
                lastEntry = newEntry;
            }
            if(BUILD_QUERY_TOKEN_FILE){
                for (String queryToken : queryTokens) {
                    try {
                        writerTokens.write(queryToken + ",");
                    } catch (IOException ex) {
                        Logger.getLogger(SearchlogAnalysis_SheetOne.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(BUILD_QUERY_PAIR_FILE){
                if(userId != lastUserId){
                    queryPairs.clear();
                    queryPairs.put(query, epoc);
                } else {
                    if(queryPairs.containsKey(query)){
                        long difference = epoc - queryPairs.get(query);
                        if(difference != 0){
                            String newString = userId+"\t"+query+"\t"+difference;
                            writeResults(writerPairs, newString);
                            queryPairs.put(query, epoc);
                        }
                    } else {
                        queryPairs.put(query, epoc);
                    }         
                }
            }
            
            /* end data of one user */
            
            buildDailyCountMatrix();
            resetVariables();
        }
        if(BUILD_CSV_FILE){
            closeWriter(writerCSV);
        }
        if(BUILD_QUERY_TOKEN_FILE){
            closeWriter(writerTokens);
        }
        if(BUILD_QUERY_PAIR_FILE){
            closeWriter(writerPairs);
        }
        matrixBuilder.correctOffByOne();
    }

    // The read lines get splittet at a Tabulator.
    private static String[] getNextTokens(Scanner scanner) {
            String currentString = scanner.nextLine();
            String[] currentTokens = currentString.split("\t");
            return currentTokens;
    }

    // The read properties of the query are being written in the variables.
    private static void setVariables(String[] currentTokens) {
            lastUserId = userId;
            userId = Integer.parseInt(currentTokens[0]);
            query = currentTokens[1];
            queryTokens = query.split(",|\\s|-");
            /* clears query from / and " */
            escapeQueryCharacters(query);
            wordCount = queryTokens.length;
            lastEpoc = epoc;
            epoc = convertToDate(currentTokens[2]).getTime();
            timeSinceLastInteraction = epoc - lastEpoc;
            if(currentTokens.length>4){
                totalClicks++;
                currentRank = Integer.parseInt(currentTokens[3]);
            }
    }
    
    // Converts Date from the read Format ("yyyy-MM-dd HH:mm:ss") to a Java-Date Object. Epoch can be retreived via the Date.getTime() method.
    private static Date convertToDate(String simpleDate){
        try {
            return new SimpleDateFormat(Util.getDateFormat()).parse(simpleDate);
        } catch (ParseException ex) {
            System.out.println("Something went wrong while parsing the Date");
            System.exit(1);
        }
        return null;
    }

     // If the User-ID of the current query is different to the one before the sessionID ticks up (Also timeSinceLastInteraction = 0).
     // If the User-ID is the same but the query is more than 30 minutes old the sessionID ticks up.
    private static boolean changeSessionIfNecessary() {
        if(userId != lastUserId){
            sessionId++;
            timeSinceLastInteraction = 0;
            totalUsers++;
            newUser = true;
            return true;
        } else if(timeSinceLastInteraction > Util.minutesToEpoc(30)){
            sessionId++;
            return true;
        }
        return false;
    }

    private static void buildDailyCountMatrix() {
        if(userId != lastUserId){
            matrixBuilder.addNewRow(userId);
        }
        int queryDate = (int)((epoc-Util.getStartEpoc())/Util.minutesToEpoc(60*24));
        matrixBuilder.countUserQueryDayUp(queryDate);
    }

    /**
     * builds and returns a String in the right Format for R.
     * @return 
     */
    private static String buildNewEntry() {
        String returnString =
                        "\""+sessionId+
                        "\"\t\""+userId+
                        "\"\t\""+query+
                        "\"\t\""+timeSinceLastInteraction+
                        "\"\t\""+epoc+
                        "\"\t\""+query.length()+
                        "\"\t\""+wordCount+
                        "\"\t\""+lastSessionLengthTime+
                        "\"\t\""+queriesLastSession+
                        "\"\t\""+currentRank+"\"";
        return returnString;
    }

    // Writes in the results.txt
    private static void writeResults(Writer writer, String newEntry) {
        try {
            writer.write(newEntry + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    private static void checkForBots() {
        int check = 0;
        for (int[] get : dailyCountMatrix) {
            for (int j = 0; j < get.length; j++) {
                if(get[j] >= Util.getQueryAmountBorder()){
                    check++;
                }
            }
        }
        System.out.println(check);
    }
    */

    private static void addNonValuesToString() {
        String[] split = lastEntry.split("\t");
        split[7] = "\"NA\"";
        String returnString =
                        split[0]+
                        "\t"+split[1]+
                        "\t"+split[2]+
                        "\t"+split[3]+
                        "\t"+split[4]+
                        "\t"+split[5]+
                        "\t"+split[6]+
                        "\t"+split[7]+
                        "\t"+split[8]+
                        "\t"+split[9];
        lastEntry = returnString;
    }
}
