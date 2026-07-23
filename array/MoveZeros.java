package com.dcb;

import java.util.Arrays;
import java.util.stream.IntStream;

public class MoveZeros {


  public static void main(String[] args) {
    MoveZeros m = new MoveZeros();
    int[] arrAns = m.moveZeros(new int[]{5, 1, 5, 3, 12});
    System.out.println(Arrays.toString(arrAns));

  }

  //Q   0,1,0,3,12
// A  1,3,12,0,0

  private int[] moveZeros(int[] arr) {
    int j = 0; //Writer

    for (int i = 0; i < arr.length; i++) {
//      here i acts as reader and j acts as writer
//       for sure j<=i
//      each time for non Zero element we will save current value
//      so for zeroth value we will not move jth index which will eventually
//      write to Zero present at value and rest we can make Zero Manually as at the end
//       it will reach a point from which zero should start
//      eg  i,j 0,0 -> j=0 , i=0 array will remain as it is 0,1,0,3,12
//
//      i,j 1,0,  --> Now after the loop it will become 1,1,0,3,12  as j was at 0th Position at last j=1(0+1)
//
//      i,j 2,1  --> same as i=2 the val is 0
//       i,j 3,1 --> it will become 1,3,0,3,12 and j=2
//      i , j 4,2 --> it will become 1,3,12,3,12 and j=3
//
//      Now loop will stop and we can observe that j is at point from where we must get Zeros
//      we will add those !

      if (arr[i] != 0) {

        arr[j] = arr[i];
        j++;
      }
    }

    for (int i = j; i < arr.length; i++) {
      arr[i] = 0;
    }

    return arr;
  }

  private int[] moveZeros2(int[] arr) {
    return IntStream.concat(Arrays.stream(arr).filter(n -> n != 0)
        , Arrays.stream(arr).filter(n -> n == 0)
    ).toArray();
    //this is also not optimal as this will read array twice.

  }

  private int[] moveZeros1(int[] arr) {
// very basic and not at all Optimal !!
    int n = arr.length;
    int[] temp = new int[n];
    int count = 0;
    int j = 0; //This j is temp index
    for (int i = 0; i < n; i++) {
      if (arr[i] != 0) {

        temp[j] = arr[i];
        j++;
      }
    }

    return temp;
  }
}
