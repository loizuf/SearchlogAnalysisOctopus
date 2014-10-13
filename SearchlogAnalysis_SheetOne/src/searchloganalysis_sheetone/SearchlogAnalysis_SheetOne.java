/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package searchloganalysis_sheetone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.String;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cip
 */
public class SearchlogAnalysis_SheetOne {
    
    private final static String BIG_DATA_LOCATION = "C:\\Users\\Soean\\Documents\\Uni\\AOL-user-ct-collection\\user-ct-test-collection-01.txt";
    private static ArrayList<Long> convertedDates = new ArrayList<Long>();
    
    //Variables for Output
    private static int sessionId;
    private static int userId = 0;
    private static int lastUserId;
    private static String query;
    private static String rawDate;
    private static Date generatedJavaDate;
    private static long timeSinceLastInteraction;
    private static long epoch;
    private static long lastEpoch;
    
    private static Writer writer;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        resetVariables();
        registerWriter();
        readBigData();
        try {writer.close();}
        catch (Exception ex) {}
        
    }

    private static void readBigData() {
        Scanner scanner;
        try { 
            scanner = new Scanner(new File(BIG_DATA_LOCATION), "UTF-8");
            tokenizeInput(scanner);
            scanner.close();
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        }
    }

    private static void tokenizeInput(Scanner scanner) {
        scanner.nextLine();
        int counter = 0;
        while(scanner.hasNextLine()){
            String currentString = scanner.nextLine();
            String[] currentTokens = currentString.split("\t");
            
            lastUserId = userId;
            userId = Integer.parseInt(currentTokens[0]);
            query = currentTokens[1];
            rawDate = currentTokens[2];
            generatedJavaDate = convertToDate(rawDate);
            lastEpoch = epoch;
            epoch = generatedJavaDate.getTime();
            timeSinceLastInteraction = epoch - lastEpoch;
            if(userId != lastUserId){
                sessionId++;
                timeSinceLastInteraction = 0;
            } else if(timeSinceLastInteraction > 1800000){
                sessionId++;
            }
            String newEntry = buildNewEntry();
            System.out.println("pre");
            
            
            
            writeResults(writer, newEntry);
            System.out.println("post");
            resetVariables();
            counter++;
        }
    }
    
    private static Date convertToDate(String simpleDate){
        Date generatedDate = null;
        try {
            generatedDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(simpleDate);
        } catch (ParseException ex) {
            Logger.getLogger(SearchlogAnalysis_SheetOne.class.getName()).log(Level.SEVERE, null, ex);
        }
        return generatedDate;
    }
    
    private static Date convertEpochToDate(long epoch){
        return new Date (epoch);
    }

    private static void resetVariables() {
        query = "";
        rawDate = "";
        generatedJavaDate = null;
        timeSinceLastInteraction = 0;
    }

    private static String buildNewEntry() {
        return "\""+sessionId+
                    ","+userId+
                    ","+query+
                    ","+rawDate+
                    ","+generatedJavaDate+
                    ","+timeSinceLastInteraction+
                    ","+epoch+"\"";
    }

    private static void writeResults(Writer writer, String newEntry) {
        try {
            writer.write(newEntry + "/n");
        } catch (IOException ex) {
            System.out.println("error");
        }
    }

    private static void registerWriter() {
        writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("results.txt"), "utf-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SearchlogAnalysis_SheetOne.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SearchlogAnalysis_SheetOne.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
