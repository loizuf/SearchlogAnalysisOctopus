/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchloganalysis_sheetone;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
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
        
        thisUser = null;
        thisUser = reinitializeIntArray(thisUser);
        userIdRegistry.add(0);
    }
    
    public void addNewRow(int userID) {
        userIdRegistry.add(userID);
        int[] copy = thisUser.clone();
        dailyCountMatrix.add(copy);
        thisUser = reinitializeIntArray(thisUser);
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
    
    private static int[] getRandomUserIDs(int totalUsers) {
        Random r = new Random();
        int[] randomArray = new int[Util.getSampleSize()];
        for(int i = 0; i<randomArray.length; i++){
            randomArray[i] = r.nextInt(totalUsers+1);
        }
        return randomArray;
    }
    
    private static int[] reinitializeIntArray(int[] intArray){
        intArray = new int[Util.getTotalDays()+1];
        return intArray;
    }
    
}
