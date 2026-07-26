package com.dcb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupAnagrams {

  /**
   * Groups transaction reference IDs that are anagrams of each other.
   *
   * @param arr An array of transaction reference IDs.
   * @return A list of grouped anagrams.
   */
  static List<List<String>> anagrams(String[] arr) {
    // TODO: Implement your logic to group anagrams here
    List<List<String>> res = new ArrayList<>();

    Map<String, List<String>> ansMap = new HashMap<>();

    for (String word : arr) {
      char[] allChars = word.toCharArray();
      Arrays.sort(allChars);
      String sign = new String(allChars); // for act,tac,cat -> act is the sign
      List<String> anagram;
      if (!ansMap.containsKey(sign)) {
        anagram = new ArrayList<>();
        ansMap.put(sign, anagram);
      } else {
        anagram = ansMap.get(sign);

      }
      anagram.add(word); //as anagram is refrence to list
      // it will add it in the same list no need to put again
//      ansMap.put(sign, anagram);

    }

    ansMap.forEach((k, v) -> res.add(v));

    return res;
  }

  public static void main(String[] args) {
    // Test case
    String[] arr = {"act", "god", "cat", "dog", "tac"};

    List<List<String>> res = anagrams(arr);

    // Print results
    for (List<String> group : res) {
      System.out.println(group);
    }
  }
}
