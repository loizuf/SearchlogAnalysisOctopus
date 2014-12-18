/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchloganalysis_sheetone;

/**
 *
 * @author Soeren
 */
public class Util {
    
    private static final String DEFAULT_ERROR = "Error";
    
    private static final String BIG_DATA_LOCATION_LAPTOP = "C:\\Users\\Soean\\Documents\\Uni\\AOL-user-ct-collection\\user-ct-test-collection-01.txt";
    private static final String BIG_DATA_LOCATION_PC = "Y:\\Uni\\Serachlogs\\AOL-user-ct-collection\\user-ct-test-collection-01.txt";
    private static final String BIG_DATA_LOCATION_CIP = "C:\\Users\\cip\\AOL-user-ct-collection\\user-ct-test-collection-01.txt";
    private static final String HEADER = "\"sessionID\",\"userId\",\"query\",\"timeSinceLast\",\"epoc\",\"queryLength\",\"queryWordCount\",\"sessionLength\",\"sessionQueryCount\",\"clickRank\"";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String ENCODING = "UTF-8";
    private static final String CSV_LOCATION = "results.csv";
    private static final String DAILY_COUNT_MATRIX_LOCATION = "dailyCountMatrix.txt";
    private static final String DAILY_SAMPLE_COUNT_MATRIX_LOCATION = "sampleDailyCountMatrix.txt";
    private static final String CHARACTER_COUNT_ARRAY_LOCATION = "characterCountMatrix.txt";
    private static final String SAMPLE_USER_INDEX_LOCATION = "sampleUserIndex.txt";
    private static final String QUERY_TOKEN_FILE_LOCATION = "queryTokens.txt";
    private static final String QUERY_PAIR_FILE_LOCATION = "queryPairs.csv";
    
    private static final int TOO_MANY_QUERIES_FOR_SESSION_LENGTH = 1;
    private static final int TOO_LONG_QUERIES = 2;
    private static final int TOO_SIMILAR_QUERIES = 3;
    private static final int TOO_FAST_QUERY_INPUT = 4;
    
    private static final int totalDays = 91;
    private static final int EPOC_30_MINUTES = 1800000;
    private static final int queryAmountBorder = 100;
    private static final int SAMPLE_SIZE = 1000;
    private static final long START_EPOC = 1141193832000l;
    
    
    public static String getDataLocation(int i){
        switch(i){
            case 1:
                return BIG_DATA_LOCATION_LAPTOP;
            case 2:
                return BIG_DATA_LOCATION_PC;
            case 3:
                return BIG_DATA_LOCATION_CIP;
            default:
                return DEFAULT_ERROR;
        }
    }
    
    public static String getHeader(){
        return HEADER;
    }
    
    public static String getDateFormat(){
        return DATE_FORMAT;
    }
    
    public static String getEncoding(){
        return ENCODING;
    }
    
    public static long minutesToEpoc(int time){
        return time * 60000;
    }
    
    public static long getStartEpoc(){
        return START_EPOC;
    }

    public static int getTotalDays() {
        return totalDays;
    }
    
    public static String getCSVLocation() {
        return CSV_LOCATION;
    }

    public static String getDailyCountMatrixLocation() {
        return DAILY_COUNT_MATRIX_LOCATION;
    }

    public static int getQueryAmountBorder() {
        return queryAmountBorder;
    }

    public static String getCharacterCountMatrixLocation() {
        return CHARACTER_COUNT_ARRAY_LOCATION;
    }

    public static String getSampleDailyCountMatrixLocation() {
        return DAILY_SAMPLE_COUNT_MATRIX_LOCATION;
    }
    
    public static String getSampleUserIndexLocation() {
        return SAMPLE_USER_INDEX_LOCATION;
    }
    
    public static String getQueryTokenFileLocation(){
        return QUERY_TOKEN_FILE_LOCATION;
    }
    
    public static int getSampleSize() {
        return SAMPLE_SIZE;
    }

    static String getQueryPairFileLocation() {
        return QUERY_PAIR_FILE_LOCATION;
    }

}
