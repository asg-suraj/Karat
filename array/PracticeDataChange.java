package com.dcb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PracticeDataChange {


  /**
   * Finds shared courses for every pair of students.
   *
   * @param enrollments A 2D array where each element is [studentID, courseName]
   * @return A Map where the key is a string representing the student pair (e.g., "58,17") and the
   * value is a List of courses shared by both students.
   */
  public static Map<String, List<String>> findPairs(String[][] enrollments) {
    // TODO: Implement your solution here
    Map<String, Set<String>> studWiseSubs = new HashMap<>();

    for (String[] s : enrollments) {
      //student -> subjects
      studWiseSubs.computeIfAbsent(s[0], k -> new HashSet<>()).add(s[1]);
    }

    List<String> students = new ArrayList<>(studWiseSubs.keySet());
    Map<String, List<String>> ans = new HashMap<>();
    for (int i = 0; i < students.size(); i++) {
      String sOne = students.get(i);
      for (int j = i + 1; j < students.size(); j++) {
        String sTwo = students.get(j);
        String pair = sOne + " " + sTwo;
        Set<String> studentOneSubjects = studWiseSubs.get(sOne);
        Set<String> studentTwoSubjects = studWiseSubs.get(sTwo);
        List<String> commonSubList = new ArrayList<>();
        for (String subject : studentOneSubjects) {
          if (studentTwoSubjects.contains(subject)) {
            commonSubList.add(subject);
          }
        }
        ans.put(pair, commonSubList);
      }
    }

    return ans;
  }

  public static void main(String[] argv) {
    String[][] enrollments1 = {
        {"58", "Linear Algebra"},
        {"94", "Art History"},
        {"94", "Operating Systems"},
        {"17", "Software Design"},
        {"58", "Mechanics"},
        {"58", "Economics"},
        {"17", "Linear Algebra"},
        {"17", "Political Science"},
        {"94", "Economics"},
        {"25", "Economics"},
        {"58", "Software Design"}
    };

    Map<String, List<String>> result = findPairs(enrollments1);

    // Print output to verify
    for (Map.Entry<String, List<String>> entry : result.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
  }
}
