package com.dcb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TheNormalization {

  public static void main(String args[]) {
    TheNormalization t = new TheNormalization();
    List<String> userIds = new ArrayList<>();
    String userId = "newExample --";
    userIds.add(userId);
    System.out.println(t.normalize(userIds));
  }

  public Set<String> normalize(List<String> userIds) {

    Set<String> ans = new HashSet<>();
  //Edge Case
    if (userIds == null) {
      return ans;
    }
//    Remove all spaces (both inside the string and at the ends).
//
//        Remove all non-alphanumeric characters (like hyphens -, underscores _, or punctuation !).
//
//    Convert all characters to lowercase.
//
//        Return only the unique IDs. (The order of the returned IDs does not matter).

//    1&3
    for (int i = 0; i < userIds.size(); i++) {
      String oneThree = userIds.get(i).trim().toLowerCase();
      String ansString  = oneThree.replaceAll("[^a-zA-Z0-9]","") ;
//      System.out.println(ansString);
      ans.add(ansString);
    }

    return ans;
  }
}
