/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchloganalysis_sheetone;

import java.util.ArrayList;

/**
 *
 * @author Soean
 */
public class razacForBots {
    
    public ArrayList<String> queryOfOneUser;
    
    public static int checkQueryOnLength(String query){
        int result = 0;
       if(query.length()>200){
           result = 3;
       } else if(query.length()>100){
           result = 2;
       }else if(query.length()>50){
           result = 1;
       }
       return result;
    }
}
