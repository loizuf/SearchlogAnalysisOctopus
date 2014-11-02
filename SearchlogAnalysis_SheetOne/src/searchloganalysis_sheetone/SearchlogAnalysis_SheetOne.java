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
import java.util.Date;
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
    
    private static final int PC_TYPE = 2;
    
    /*
     * Boolean constants for writing the matrix (user x days) or the .csv with the queries or both
     */
    
    private static final boolean BUILD_CSV_FILE = true;
    private static final boolean BUILD_MATRIX_FILE = true;
    
    /*
     * Variables for Output
     */
    private static int sessionId = 0;
    private static int userId = 0;
    private static int lastUserId;
    private static int totalUsers = 0;
    private static int totalClicks = 0;
    private static String query;
    private static String rawDate;
    private static Date generatedJavaDate;
    private static long timeSinceLastInteraction;
    private static long epoc;
    
    /*
     * Variables for matrix
     */
    private static int userQueries = 0;
    private static long lastEpoc;
    private static long userLastQueryDate;
    private static ArrayList<int[]> resultMatrix;
    private static ArrayList<Integer> userIdRegistry;
    private static CompressedMatrix cMatrix;
    private static int NNZ = 0;
    /*
     * Debug variables
     */
    private static int maxDays = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Writer writerCSV = registerWriter(Util.getCSVLocation());
        Writer writerMatrix = registerWriter(Util.getMatrixLocation());

        resultMatrix = new ArrayList<>();
        userIdRegistry = new ArrayList<>();
        
        resetVariables();
        readBigData(writerCSV);
        if(BUILD_MATRIX_FILE){
            writeMatrixFile(writerMatrix);
            cMatrix = new CompressedMatrix(resultMatrix, NNZ);
        }
        
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
        rawDate = "";
        generatedJavaDate = null;
        timeSinceLastInteraction = 0;
    }
    
    /**
     * A scanner is used to read the file.
     * @param writerCSV 
     */
    private static void readBigData(Writer writerCSV) {
        Scanner scanner;
        try { 
            scanner = new Scanner(new File(Util.getDataLocation(PC_TYPE)), Util.getEncoding());
            writeHeader(scanner, writerCSV);
            createResultFile(scanner, writerCSV);
            scanner.close();
            closeWriter(writerCSV);
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        }
    }

    private static void writeHeader(Scanner scanner, Writer writer) {
        scanner.nextLine();
        writeResults(writer, Util.getHeader());
    }

    /**
     * The read lines get splittet, variables get set and with them a new String (new line) gets created and written in the results.txt.
     * @param scanner
     * @param writer 
     */
    private static void createResultFile(Scanner scanner, Writer writer) {
        int[] thisUser = null;
        thisUser = reinitializeIntArray(thisUser);
        userIdRegistry.add(0);
        while(scanner.hasNextLine()){
            String[] currentTokens = getNextTokens(scanner);
            String newEntry;
            
            setVariables(currentTokens);
            changeSessionIfNecessary();
            thisUser = buildMatrix(thisUser);
            if(BUILD_CSV_FILE){
                newEntry = buildNewEntry();
                writeResults(writer, newEntry);     
            }
            resetVariables();
        }
        resultMatrix.remove(0); //off by one correction
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
            rawDate = currentTokens[2];
            generatedJavaDate = convertToDate(rawDate);
            lastEpoc = epoc;
            epoc = generatedJavaDate.getTime();
            timeSinceLastInteraction = epoc - lastEpoc;
            if(currentTokens.length>4){
                totalClicks++;
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
    private static void changeSessionIfNecessary() {
        if(userId != lastUserId){
            sessionId++;
            timeSinceLastInteraction = 0;
            totalUsers++;
        } else if(timeSinceLastInteraction > Util.minutesToEpoc(30)){
            sessionId++;
        }
    }

    private static int[] buildMatrix(int[] thisUser) {
        if(userId != lastUserId){
            userIdRegistry.add(userId);
            int[] copy = thisUser.clone();
            resultMatrix.add(copy);
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
        return sessionId+
                    ","+userId+
                    ","+query+
                    ","+rawDate+
                    ","+generatedJavaDate+
                    ","+timeSinceLastInteraction+
                    ","+epoc;
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
                userResults += resultMatrix.get(i)[j] + ", ";
                test+=resultMatrix.get(i)[j];
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
        for (int i=0; i<resultMatrix.size(); i++){
            String newEntry = buildNewMatrixLine(i);
            writeResults(writerMatrix, newEntry);
        }
        closeWriter(writerMatrix);
    }

    private static String buildNewMatrixLine(int n) {
        String returnString = "";
        int[] currentMatrixLine = resultMatrix.get(n);
        for(int i=0; i<currentMatrixLine.length; i++){
            returnString += currentMatrixLine[i]+" ";
        }
        return returnString;
    }
}
