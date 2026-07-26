package com.dcb;

import java.util.*;

public class SevenMinuteSongPair {

    public static void main(String[] args) {
        // Example 1
        List<String[]> songs1 = Arrays.asList(
            new String[]{"Stairway to Heaven", "8:02"},
            new String[]{"Yesterday", "2:05"},
            new String[]{"Bohemian Rhapsody", "5:55"},
            new String[]{"Hey Jude", "1:05"}
        );

        // Example 2
        List<String[]> songs2 = Arrays.asList(
            new String[]{"Song A", "3:30"},
            new String[]{"Song B", "4:00"},
            new String[]{"Song C", "2:00"}
        );

        // Example 3
        List<String[]> songs3 = Arrays.asList(
            new String[]{"Track 1", "4:10"},
            new String[]{"Track 2", "3:40"},
            new String[]{"Track 3", "2:50"},
            new String[]{"Track 4", "3:20"}
        );

        System.out.println("Example 1: " + findSevenMinutePair(songs1)); // Expected: [Bohemian Rhapsody, Hey Jude]
        System.out.println("Example 2: " + findSevenMinutePair(songs2)); // Expected: []
        System.out.println("Example 3: " + findSevenMinutePair(songs3)); // Expected: [Track 3, Track 1] or [Track 2, Track 4]
    }

    /**
     * Finds two distinct songs that add up to exactly 7 minutes (420 seconds).
     * 
     * @param songs A list of string arrays where index 0 is the song name and index 1 is the duration ("M:SS")
     * @return A list containing the names of the two songs, or an empty list if no pair exists.
     */
    public static List<String> findSevenMinutePair(List<String[]> songs) {
        // TODO 1: Write out your edge cases here as comments (e.g., empty list, nulls)
        List<String> ans = new ArrayList<>();
        if (songs == null || songs.isEmpty()) {
            return ans;
        }
        Map<Integer , String> timeAndSong = new TreeMap<>();
        // TODO 2: Think about time parsing. 
        // Hint: You might want a helper method to convert "M:SS" into total seconds.
                for(String[] songNameTime : songs){
                    String name = songNameTime[0];
                    int whatWeHave = convertToSeconds(songNameTime[1]);
                    int totalNeed = 420;

                    int whatMoreWeNeed = totalNeed - whatWeHave;
                    if(timeAndSong.containsKey(whatMoreWeNeed)){
                        ans.add(timeAndSong.get(whatMoreWeNeed));
                        ans.add(name);
                        return ans;
                    }
                    timeAndSong.put(whatWeHave, name);
                }
        // TODO 3: Implement the logic avoiding the O(N^2) trap.
        // Hint: How can a HashMap or HashSet help you remember what you've already seen?

        return new ArrayList<>(); // Placeholder return
    }

  private static int convertToSeconds(String s) {
      if (s == null || s.isEmpty() ) {
          return  0;
      }
      String[] split = s.split(":");
      int min = Integer.parseInt(split[0]);
      int sec = Integer.parseInt(split[1]);
      return (min * 60)+sec;

  }

    // Optional: You can create a helper method here for parsing the time string
    // private static int parseTimeToSeconds(String timeStr) { ... }
}