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
     * Boolean constants for writing the matrix (user x days) or the .csv with the queries or both
     */
    
    private static final boolean BUILD_CSV_FILE = true;
    private static final boolean BUILD_DAILY_COUNT_MATRIX_FILE = false;
    private static final boolean BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE = false;
    
    /*
     * Boolean to read the file again or to read the created result.txt
     */
    
    private static final boolean readResults = true;
    
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
    private static int queriesPerSession = 1;
    private static int queriesLastSession = 1;
    private static String query;
    //private static String rawDate;
    //private static Date generatedJavaDate;
    private static long timeSinceLastInteraction;
    private static long sessionLengthTime;
    private static long lastSessionLengthTime;
    private static long epoc;
    
    /*
     * Variables for matrix
     */
    private static int userQueries = 0;
    private static long lastEpoc;
    private static long userLastQueryDate;
    private static ArrayList<int[]> dailyCountMatrix;
    private static ArrayList<Integer> userIdRegistry;
    private static ArrayList<Integer> characterLengthArray;
    private static int[] sampleUser;
    private static CompressedMatrix cMatrix;
    private static int NNZ = 0;
    private static Writer writerSampleMatrix;
    /*
     * Debug variables
     */
    private static int maxDays = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        dailyCountMatrix = new ArrayList<>();
        characterLengthArray = new ArrayList<>();
        userIdRegistry = new ArrayList<>();
        
       // test.testArray();
        
        resetVariables();
        readBigData();
        checkForBots();
        if(BUILD_DAILY_COUNT_MATRIX_FILE){
            if(BUILD_SAMPLE_DAILY_COUNT_MATRIX_FILE){
                sampleUser = getRandomUserIDs();
                writerSampleMatrix = registerWriter(Util.getDailySampleCountMatrixLocation());
            }
            Writer writerMatrix = registerWriter(Util.getDailyCountMatrixLocation());
            writeMatrixFile(writerMatrix);
        }
        
        Writer writerCharacterCountArray = registerWriter(Util.getCharacterCountMatrixLocation());
        writeCharacterCountArray(writerCharacterCountArray);
        
        System.out.println("done");
    }

    private static Writer registerWriter(String resultFileName) {
        Writer writer = null;
        
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFileName), Util.getEncoding()));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return writer;
    }

    /**
     * Resets Variables which change from query to query
     */
    private static void resetVariables() {
        query = "";
        //rawDate = "";
        //generatedJavaDate = null;
        timeSinceLastInteraction = 0;
        lastSessionLengthTime = 0;
        queriesLastSession = 1;
        currentRank = 0;
    }
    
    /**
     * A scanner is used to read the file. 
     */
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

    private static void writeHeader(Scanner scanner, Writer writer) {
        writeResults(writer, Util.getHeader());
    }

    /**
     * The read lines get splittet, variables get set and with them a new String (new line) gets created and written in the results.txt.
     * @param scanner
     */
    private static void createResultFile(Scanner scanner) {
        Writer writerCSV;
        if(BUILD_CSV_FILE){
            writerCSV = registerWriter(Util.getCSVLocation());
            writeHeader(scanner, writerCSV);
        }
        
        int[] thisUser = null;
        thisUser = reinitializeIntArray(thisUser);
        userIdRegistry.add(0);
        while(scanner.hasNextLine()){
            String[] currentTokens = getNextTokens(scanner);
            String newEntry;
            
            setVariables(currentTokens);
            fillCharacterLengthArrayList();
            if(changeSessionIfNecessary()){
                sessionLengthTime = 0;
                queriesPerSession = 1;
            } else {
                sessionLengthTime += timeSinceLastInteraction;
                lastSessionLengthTime = sessionLengthTime;
                queriesPerSession++;
                queriesLastSession = queriesPerSession;
            }
            thisUser = buildDailyCountMatrix(thisUser);
            if(BUILD_CSV_FILE){
                newEntry = buildNewEntry();
                writeResults(writerCSV, newEntry);     
            }
            resetVariables();
        }
        if(BUILD_CSV_FILE){
            closeWriter(writerCSV);
        }
        dailyCountMatrix.remove(0); //off by one correction
    }

    /**
     * The read lines get splittet at a Tabulator.
     * @param scanner
     * @return 
     */
    private static String[] getNextTokens(Scanner scanner) {
            String currentString = scanner.nextLine();
            String[] currentTokens = currentString.split("\t");
            return currentTokens;
    }

    /**
     * The read properties of the query are being written in the variables.
     * @param currentTokens 
     */
    private static void setVariables(String[] currentTokens) {
            lastUserId = userId;
            userId = Integer.parseInt(currentTokens[0]);
            query = currentTokens[1];
            String[] arr = query.split(",|\\s|-");
            wordCount = arr.length;
            //rawDate = currentTokens[2];
            //generatedJavaDate = convertToDate(rawDate);
            lastEpoc = epoc;
            epoc = convertToDate(currentTokens[2]).getTime();
            timeSinceLastInteraction = epoc - lastEpoc;
            if(currentTokens.length>4){
                totalClicks++;
                currentRank = Integer.parseInt(currentTokens[3]);
            }
    }
    
    /**
     * Converts Date from the read Format ("yyyy-MM-dd HH:mm:ss") to a Java-Date Object. Epoch can be retreived via the Date.getTime() method.
     * @param simpleDate
     * @return 
     */
    private static Date convertToDate(String simpleDate){
        Date generatedDate = null;
        try {
            generatedDate = new java.text.SimpleDateFormat(Util.getDateFormat()).parse(simpleDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return generatedDate;
    }

    /**
     * If the User-ID of the current query is different to the one before the sessionID ticks up (Also timeSinceLastInteraction = 0).
     * If the User-ID is the same but the query is more than 30 minutes old the sessionID ticks up.
     */
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

    private static int[] buildDailyCountMatrix(int[] thisUser) {
        if(userId != lastUserId){
            userIdRegistry.add(userId);
            int[] copy = thisUser.clone();
            dailyCountMatrix.add(copy);
            thisUser = reinitializeIntArray(thisUser);
        }
        int queryDate = (int)((epoc-Util.getStartEpoc())/Util.minutesToEpoc(60*24));
        if(thisUser[queryDate] != 0){
            NNZ += 1;
        }
        thisUser[queryDate]++;
        return thisUser;
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

    /**
     * Writes in the results.txt
     * @param writer
     * @param newEntry 
     */
    private static void writeResults(Writer writer, String newEntry) {
        try {
            writer.write(newEntry + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeWriter(Writer writer) {
        try {
            writer.close();
        }
        catch (Exception e) {
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
    
    private static int[] reinitializeIntArray(int[] intArray){
        intArray = new int[Util.getTotalDays()+1];
        return intArray;
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

    private static int[] getRandomUserIDs() {
        Random r = new Random();
        int[] randomArray = new int[Util.getSampleSize()];
        for(int i = 0; i<randomArray.length; i++){
            randomArray[i] = r.nextInt(totalUsers+1);
        }
        return randomArray;
    }
    
    private static void writeCharacterCountArray (Writer writerCharacterCountArray) {
        for (int i = 0; i < characterLengthArray.size()-1; i++) {
            writeResults(writerCharacterCountArray, Integer.toString(characterLengthArray.get(i)));
        }
        writeResults(writerCharacterCountArray, Integer.toString(characterLengthArray.get(characterLengthArray.size()-1)));
    }

    private static void fillCharacterLengthArrayList() {
        characterLengthArray.add(query.length());
    }
}
