package com.dcb;

import java.util.Arrays;

public class RotateArrayByK {


  public static void main(String[] args) {
    int[] array = rotateArray(new int[]{1, 2, 3, 4, 5}, 2);
    System.out.println(Arrays.toString(array));
  }

  private static int[] rotateArray(int[] arr , int k){
    int n = arr.length;
    //reverse Entire array 
    reverseArr(0,n-1 , arr);
    reverseArr(0 , k-1 , arr); // reverse part till k
    reverseArr(k , n-1 , arr); // from k to arr length
    return arr;
  }

  private static  void reverseArr(int start , int end , int[] arr){
    while(start < end ){
      //Simple 2 pointer array reverse logic
      int temp = arr[end];
      arr[end] = arr[start];
      arr[start] = temp;
      start++;
      end--;
    }
  }

 private static int[]  rotateArray0(int[] arr , int k){
//Normal Logic but it has Space Complexity 
   int[] temp = new int[arr.length];

    for(int i=0 ; i<arr.length ; i++){
      int new_index = (i+k) % arr.length;
      temp[new_index] = arr[i];
    }

   for (int i = 0; i < arr.length; i++) {
     arr[i] = temp[i];
   }
   return arr;
 }

}
