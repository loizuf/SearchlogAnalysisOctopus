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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

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
    
    private static final int PC_TYPE = 1;
    
    /*
     * Boolean constants for writing things
     */
    
    private static final boolean BUILD_CSV_FILE = true;
    private static final boolean BUILD_DAILY_COUNT_MATRIX_FILE = false;
    private static final boolean BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE = false;
    
    /*
     * Variables for Output
     */
    private static int sessionId = 0;
    private static int userId = 0;
    private static int lastUserId;
    private static int wordCount;
    private static int currentRank;
    private static int totalUsers = 0;
    private static int totalClicks = 0;
    private static int flag = 0;
    private static int queriesPerSession = 1;
    private static int queriesLastSession = 1;
    private static long timeSinceLastInteraction;
    private static long sessionLengthTime;
    private static long lastSessionLengthTime;
    private static long epoc;
    private static long lastEpoc;
    private static String query;
    
    /*
     * Variables for matrix
     */
    private static DailyCountMatrixBuilder matrixBuilder;
    
    /*
    private static int NNZ = 0;
    private static int[] sampleUser;
    private static ArrayList<int[]> dailyCountMatrix;
    private static Writer writerSampleMatrix;
    */

    public static void main(String[] args) {
        
        // This part is only necessary for the first sheet. Everything final should be done here and all information should be written in the result.txt
        matrixBuilder = new DailyCountMatrixBuilder(Util.getTotalDays(), BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE);
        resetVariables();
        
        readBigData();
        checkForBots();
        
        if(BUILD_DAILY_COUNT_MATRIX_FILE){
            if(BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE){
                matrixBuilder.writeFile(BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE);
            }
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
    }
    
    private static void writeCSVHeader(Scanner scanner, Writer writer) {
        writeResults(writer, Util.getHeader());
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
        // wirter gets initialized only when the file gets written to prevent deleting the old file in the same location
        
        if(BUILD_CSV_FILE){
            writerCSV = registerWriter(Util.getCSVLocation());
            writeCSVHeader(scanner, writerCSV);
        }
        
        while(scanner.hasNextLine()){
            String[] currentTokens = getNextTokens(scanner);
            String newEntry;
            
            /* Start getting the data of one user and writing it in a file*/
            
            // This sets local variables which are used later on
            setVariables(currentTokens);
            
            // advances session and counts some variables for measuring the session length
            if(changeSessionIfNecessary()){
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
                writeResults(writerCSV, newEntry);     
            }
            
            /* end data of one user */
            
            buildDailyCountMatrix();
            resetVariables();
        }
        if(BUILD_CSV_FILE){
            closeWriter(writerCSV);
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
            String[] arr = query.split(",|\\s|-");
            wordCount = arr.length;
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
        String returnString =sessionId+
                        ","+userId+
                        ","+query+
                        ","+timeSinceLastInteraction+
                        ","+epoc+
                        ","+query.length()+
                        ","+wordCount+
                        ","+lastSessionLengthTime+
                        ","+queriesLastSession+
                        ","+currentRank;
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

    private static void logMatrix() {
        int test = 0;
        for (int i = 0; i < 10; i++) {
            String userResults = "";
            for (int j = 0; j < Util.getTotalDays(); j++) {
                userResults += dailyCountMatrix.get(i)[j] + ", ";
                test+=dailyCountMatrix.get(i)[j];
            }
            System.out.println("UserID: "+userIdRegistry.get(i+1)+"\n"+userResults);
            System.out.println(test);
        }
    }

    private static void writeMatrixFile(Writer writerMatrix) {
        for (int i=0; i<dailyCountMatrix.size(); i++){
            String newEntry = buildNewMatrixLine(i);
            writeResults(writerMatrix, newEntry);
            if(BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE){
                if(Arrays.asList(sampleUser).contains(i)){
                    writeResults(writerSampleMatrix, newEntry);
                }
            }
        }
        closeWriter(writerMatrix);
    }

    private static String buildNewMatrixLine(int n) {
        String returnString = "";
        int[] currentMatrixLine = dailyCountMatrix.get(n);
        for(int i=0; i<currentMatrixLine.length; i++){
            returnString += currentMatrixLine[i]+" ";
        }
        return returnString;
    }

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
}
