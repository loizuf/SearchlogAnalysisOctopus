/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchloganalysis_sheetone;

// Clear Explanation of this structure can be found here: http://en.wikipedia.org/wiki/Sparse_matrix#Storing_a_sparse_matrix

import java.util.ArrayList;


/**
 *
 * @author Soeren
 */
public class CompressedMatrix {
    private int[] A;
    private int[] IA;
    private int[] JA;
    
    private ArrayList<Integer> buffer;
    
    private int NNZ;
    
    public CompressedMatrix(ArrayList<int[]> root, int NNZ){
        if(NNZ >= (root.size()*(root.get(0).length-1)-1)/2){
            System.out.println("This is not optimal");
        }
        
        this.NNZ = NNZ;
        A = new int[NNZ];
        IA = new int[root.size()+1];
        JA = new int[NNZ];
        fill(root);
    }

    private void fill(ArrayList<int[]> root) {
        int counterA = 0;
        boolean firstInRow = false;
        
        for(int i=0; i<root.size(); i++){
            int[] c = root.get(i);
            firstInRow = true;
            for (int j = 0; j < c.length; j++) {
                if(c[j]!=0){
                    getA()[counterA] = c[j];
                    if(firstInRow){
                        getIA()[i] = counterA;
                        firstInRow = false;
                    }
                    getJA()[counterA++] = j;
                }
            }
        }
        IA[IA.length-1] = NNZ;
    }
    
    public int[] getA() {
        return A;
    }

    public int[] getIA() {
        return IA;
    }

    public int[] getJA() {
        return JA;
    }
    
    public int get(int x, int y){
        return A[IA[x]+y];
    }
}
