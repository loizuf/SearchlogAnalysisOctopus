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
import java.util.Date;
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
    //private final static String BIG_DATA_LOCATION = "C:\\Users\\Soean\\Documents\\Uni\\AOL-user-ct-collection\\user-ct-test-collection-01.txt";
    //private final static String BIG_DATA_LOCATION = "Y:\\Uni\\Serachlogs\\AOL-user-ct-collection\\user-ct-test-collection-01.txt";
    private final static String BIG_DATA_LOCATION = "C:\\Users\\cip\\AOL-user-ct-collection\\user-ct-test-collection-01.txt";
    
    /*
     * Header Line for Ouput
     */
    private static final String HEADER = "sessionID,UserID,query,rawDatw,javaGenDate,timeSinceLast,epoch";
    
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
    private static long epoch;
    private static long lastEpoch;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        resetVariables();
        Writer writer = registerWriter();
        readBigData(writer);
        closeWriter(writer);
        System.out.println(totalUsers);
        System.out.println(totalClicks);
        System.out.println("done");
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

    private static Writer registerWriter() {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("results.txt"), "utf-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return writer;
    }
    
    /**
     * A scanner is used to read the file.
     * @param writer 
     */
    private static void readBigData(Writer writer) {
        Scanner scanner;
        try { 
            scanner = new Scanner(new File(BIG_DATA_LOCATION), "UTF-8");
            tokenizeInput(scanner, writer);
            scanner.close();
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * The read lines get splittet, variables get set and with them a new String (new line) gets created and written in the results.txt.
     * @param scanner
     * @param writer 
     */
    private static void tokenizeInput(Scanner scanner, Writer writer) {
        scanner.nextLine();
        writeResults(writer, HEADER);
        while(scanner.hasNextLine()){
            String[] currentTokens = getNextLine(scanner);
            String newEntry;
            
            setVariables(currentTokens);
            changeSessionIfNecessary();
            newEntry = buildNewEntry();
            writeResults(writer, newEntry);
            resetVariables();
        }
    }

    /**
     * The read lines get splittet at a Tabulator.
     * @param scanner
     * @return 
     */
    private static String[] getNextLine(Scanner scanner) {
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
            lastEpoch = epoch;
            epoch = generatedJavaDate.getTime();
            timeSinceLastInteraction = epoch - lastEpoch;
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
            generatedDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(simpleDate);
        } catch (ParseException ex) {
            Logger.getLogger(SearchlogAnalysis_SheetOne.class.getName()).log(Level.SEVERE, null, ex);
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
        } else if(timeSinceLastInteraction > 1800000){
            sessionId++;
        }
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
                    ","+epoch;
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
}
