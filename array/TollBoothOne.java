package com.dcb;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
* "We are writing software to analyze logs for toll booths on a highway. This highway is a divided highway with limited access; the only way on to or off of the highway is through a toll booth.

There are three types of toll booths:
* ENTRY toll booths, where a car goes through a booth as it enters the highway.
* EXIT toll booths, where a car goes through a booth as it exits the highway.
* MAINROAD (M in the diagram), which have sensors that record a license plate as a car drives through at full speed.

        Exit Booth                         Entry Booth
            |                                   |
            |                                   |
             \                                 /
---<------------<---------M---------<-----------<---------<----
                                         (West-bound side)

===============================================================

                                         (East-bound side)
------>--------->---------M--------->--------->--------->------
             /                                 \
            |                                   |
            |
                                      |
        Entry Booth                         Exit Booth
*


There are total 3 tasks 
For our first task:
1-1) Read through and understand the code and comments below. Feel free to run the code and tests.
1-2) The tests are not passing due to a bug in the code. Make the necessary changes to LogEntry to fix the bug.

second and third tasks are to complete the methods with logic.
* */

public class TollBoothOne {

  public static void main(String[] args) {
    // In a real Karat environment, these tests would run automatically against the code.
    System.out.println("Environment ready. Run tests to begin.");

//    LogEntry l = new LogEntry("34400.409 SXY288 210E ENTRY");
  }
}


class LogEntry {

  private final Double timestamp;
  private final String licensePlate;
  private final String boothType;
  private final int location;
  private final String direction;

  public LogEntry(String logLine) {
    String[] tokens = logLine.split(" ");

    // TODO: There is a bug in the parsing logic below. Find and fix it (Task 1).
//    this.timestamp = Float.parseFloat(tokens[0]);  //this was error
    this.timestamp = Double.parseDouble(tokens[0]);  //this was fix
    this.licensePlate = tokens[1];
    this.boothType = tokens[3];
    this.location = Integer.parseInt(tokens[2].substring(0, tokens[2].length() - 1));

    String directionLetter = tokens[2].substring(tokens[2].length() - 1);
    if (directionLetter.equals("E")) {
      this.direction = "EAST";
    } else if (directionLetter.equals("W")) {
      this.direction = "WEST";
    } else {
      throw new IllegalArgumentException();
    }
  }

  public Double getTimestamp() {
    return timestamp;
  }

  public String getLicensePlate() {
    return licensePlate;
  }

  public String getBoothType() {
    return boothType;
  }

  public int getLocation() {
    return location;
  }

  public String getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return String.format(
        "<LogEntry timestamp: %s  license: %s  location: %s  direction: %s  booth type: %s>",
        String.valueOf(timestamp), licensePlate, location, direction, boothType);
  }
}

class LogFile {

  List<LogEntry> logEntries;

  public LogFile(BufferedReader reader) throws IOException {
    this.logEntries = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      LogEntry logEntry = new LogEntry(line.strip());
      this.logEntries.add(logEntry);
    }
  }

  public int size() {
    return this.logEntries.size();
  }

  // TODO: Task 2
  public int countJourneys() {
    Set<String> setData = new HashSet<>();
    int journeyCounter = 0;
    if (size() < 2) {
      return 0;
    }

    for (LogEntry l : logEntries) {
      if (l.getBoothType().equals("ENTRY")) {
        setData.add(l.getLicensePlate());
      } else if (l.getBoothType().equals("EXIT")) {
        boolean contains = setData.contains(l.getLicensePlate());
        if (contains) {
          journeyCounter++;
          setData.remove(l.getLicensePlate());
        }
      }
    }

    return journeyCounter;
  }

  // TODO: Task 3
  public List<String> catchSpeeders() {
    double oneWayLimit = 130.0;
    double twoWayLimit = 120.0;
    List<String> speeders = new ArrayList<>();
    //we will store Licence Plate and Information in Map
    Map<String, LogEntry> licenceAndInfo = new LinkedHashMap<>();

    // Implement speed calculation and detection logic here
    for (LogEntry l : logEntries) {
      if (l.getBoothType().equals("ENTRY")) {
        licenceAndInfo.put(l.getLicensePlate(), l);
      }
      if (l.getBoothType().equals("EXIT")) {
        LogEntry entryLog = licenceAndInfo.get(l.getLicensePlate());
        if (entryLog != null) {
          double distance = Math.abs(l.getLocation() - entryLog.getLocation());
          double time = Math.abs((l.getTimestamp() - entryLog.getTimestamp()) / 3600.0); //for Kmph
          if (time == 0) {
            // this is rare scenario where Driver instantly takes U-turn but that will give
            // Arithmatic Exception
            continue; //let's Ignore the Error!!
          }
          double speed = distance / time;
          if (entryLog.getDirection().equals(l.getDirection()) && speed > oneWayLimit) {
            speeders.add(l.getLicensePlate());
          }
          if ((!entryLog.getDirection().equals(l.getDirection())) && speed > twoWayLimit) {
            speeders.add(l.getLicensePlate());
          }
          licenceAndInfo.remove(l.getLicensePlate());
        }
      }
    }

    return speeders;
  }
}


