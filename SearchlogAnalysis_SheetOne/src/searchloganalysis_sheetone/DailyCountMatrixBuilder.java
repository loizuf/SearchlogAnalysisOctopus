/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchloganalysis_sheetone;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Soean
 */


public class DailyCountMatrixBuilder {
    
    private int NNZ = 0;
    private int[] sampleUser;
    private int[] thisUser;
    private ArrayList<int[]> dailyCountMatrix;
    private ArrayList<Integer> userIdRegistry;
    private Writer writer;
    
    public DailyCountMatrixBuilder(int dayCount, boolean sample){
        dailyCountMatrix = new ArrayList<>();
        userIdRegistry = new ArrayList<>();
        
        if(sample){
            sampleUser = getRandomUserIDs(dayCount);
        }
        thisUser = null;
        thisUser = reinitializeIntArray(thisUser);
        userIdRegistry.add(0);
    }
    
    public void addNewRow(int userID) {
        userIdRegistry.add(userID);
        int[] copy = thisUser.clone();
        dailyCountMatrix.add(copy);
        thisUser = reinitializeIntArray(thisUser);
        //System.out.println(Arrays.toString(dailyCountMatrix.get(dailyCountMatrix.size()-1)));
   }

    public void countUserQueryDayUp(int queryDate) {
        if(thisUser[queryDate] == 0){
            NNZ += 1;
        }
        thisUser[queryDate]++;
    }

    public void correctOffByOne() {
        dailyCountMatrix.remove(0);
    }

    public void writeFile(boolean sample) {
        writer = registerWriter(Util.getSampleDailyCountMatrixLocation());
        for (int i=0; i<dailyCountMatrix.size(); i++){
            if(sample){
                if(Arrays.asList(toObject(sampleUser)).contains(i)){
                    String newEntry = buildNewMatrixLine(i);
                    writeResults(writer, newEntry);
                }
            } else {
                String newEntry = buildNewMatrixLine(i);
                writeResults(writer, newEntry);
            }
        }
        if(sample){
            writeUserIndex(writer);
        }
        closeWriter(writer);
    }
    
    private Writer registerWriter(String resultFileName) {
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
    
    private int[] getRandomUserIDs(int totalUsers) {
        Random r = new Random();
        int[] randomArray = new int[Util.getSampleSize()];
        for(int i = 0; i<randomArray.length; i++){
            randomArray[i] = r.nextInt(totalUsers+1);
        }
        return randomArray;
    }
    
    private int[] reinitializeIntArray(int[] intArray){
        intArray = new int[Util.getTotalDays()+1];
        return intArray;
    }

    private void logMatrix() {
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

    private String buildNewMatrixLine(int n) {
        String returnString = "";
        int[] currentMatrixLine = dailyCountMatrix.get(n);
        for(int i=0; i<currentMatrixLine.length; i++){
            returnString += currentMatrixLine[i]+" ";
        }
        return returnString;
    }
    
    private void writeResults(Writer writer, String newEntry) {
        try {
            writer.write(newEntry + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Integer[] toObject(int[] intArray) {
        Integer[] result = new Integer[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            result[i] = Integer.valueOf(intArray[i]);
        }
        return result;
    }    
    
    public void writeUserIndex(Writer writer) {
        writer = registerWriter(Util.getSampleUserIndexLocation());
        String newEntry = ""+sampleUser[0];
        for (int i = 1; i < sampleUser.length; i++) {
            newEntry += ", "+sampleUser[i];
        }
        writeResults(writer, newEntry);
    }
    
}
