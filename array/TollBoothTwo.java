package com.dcb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * We would like to catch people who are driving at unsafe speeds on the highway.
 * To help us do that, we would like to identify journeys where a driver does either of the following:
 * * Drive 130 km/h or greater in any individual 10km segment of tollway.
 * * Drive 120 km/h or greater in any two 10km segments of tollway.
 *
 * For example, consider the following log:
 * 90750.191 JOX304 250E ENTRY
 * 91081.684 JOX304 260E MAINROAD
 * 91082.101 THX138 110E ENTRY
 * 91483.251 JOX304 270E MAINROAD
 * 91873.920 THX138 120E MAINROAD
 * 91874.493 JOX304 280E EXIT
 * .
 * .
 * 91982.102 THX138 290E EXIT
 * 92301.302 THX138 300E ENTRY
 * 92371.302 THX138 310E EXIT
 * .
 * .
 * 1000.000 TST002 270W ENTRY
 * 1275.000 TST002 260W EXIT
 *
 * In this case, the driver of TST002 drove 10 km in 275 seconds. We can calculate
 * that this driver drove an average speed of ~130.91km/hr over this segment:
 *
 * 10 km * 3600 sec/hr
 * ------------------- = 130.91 km/hr
 *       275 sec
 *
 * Note that:
 * * A license plate may have multiple journeys in one file, and if they drive at unsafe speeds in both journeys, both should be counted.
 * * We do not mark speeding if they are not on the highway (i.e. for any driving between an EXIT and ENTRY event).
 * * Speeding is only marked once per journey. For example, if there are 4 segments 120km/h or greater, or multiple segments 130km/h or greater, the journey is only counted once.
 *
 * 3-1) Write a function catchSpeeders in LogFile that returns a collection of license plates that drove at unsafe speeds during a journey in the LogFile.
 *      If the same license plate drives at unsafe speeds during two different journeys, the license plate should appear twice (once for each journey they drove at unsafe speeds).
 * */


public class TollBoothTwo {


  public static final String MAINROAD = "MAINROAD";
  public static final String ENTRY = "ENTRY";
  public static final String EXIT = "EXIT";

  /**
   * Returns a collection of license plates that drove at unsafe speeds during a journey in the
   * provided log lines.
   */
  public static List<String> catchSpeeders(String[] logLines) {
    List<String> speeders = new ArrayList<>();

    // TODO: Implement your logic here

    List<LogEntry> logs = convertLogLinesToEntries(logLines);
    //Keep Records in same 
    Map<String, LogEntry> carRec = new HashMap<>();
    for (LogEntry l : logs) {

      if (l.getSensor().equals(ENTRY)) {
        //the car just entered the Highway Start Tracking
        carRec.put(l.getLicensePlate(), l);

      }
      if (l.getSensor().equals(MAINROAD) || l.getSensor().equals(EXIT)) {
        //Catch Speeders
        LogEntry start = carRec.get(l.getLicensePlate());
        if(!start.isAlreadyflagged) {
          calculateSpeedAndOverSpeed(start, l);

          carRec.put(l.getLicensePlate(), l); //This will store/overwrite mainroad positions
          if (l.isOneWayOverSpeed) {
            //First Major offense must be added
            speeders.add(l.getLicensePlate());
           l.isAlreadyflagged =true;
          } else if (l.isTwoWayOverSpeed) {
            speeders.add(l.getLicensePlate());
            l.isAlreadyflagged =true;
          }
        }
        if (l.getSensor().equals(EXIT)) {

          carRec.remove(l.getLicensePlate());
        }
      }


    }

    return speeders;
  }

  private static void calculateSpeedAndOverSpeed(LogEntry start, LogEntry l) {
    if (l.isOneWayOverSpeed || l.isTwoWayOverSpeed) {
      //we will not waste time on calculations
      return;
    }

    //l is mid or end
    double time = Math.abs(l.getTime() - start.getTime()) / 3600.0;
    double distance = Math.abs(l.getLocation() - start.getLocation());
    if (time == 0) {
//      return; false; // to prevent exception case
      return;
    }
    double speed = distance / time;
    double oneWayLimit = 130.0;
    double twoWayLimit = 120.0;
    l.over120 = start.over120; // this is because We need to get data from past updation
    if (speed >= 120 && speed < 130) {
      l.over120++;
      if (l.over120 >= 2) {
        l.isTwoWayOverSpeed = true;
      }
    }
    if (speed >= 130) {
      l.isOneWayOverSpeed = true;
    }

    return;
  }

  private static List<LogEntry> convertLogLinesToEntries(String[] logLines) {
    List<LogEntry> logs = new ArrayList<>();
//    "90750.191 JOX304 250E ENTRY",
    for (String l : logLines) {
      String[] split = l.split(" ");

      if (split.length != 4) {
        //issue with log
        System.out.print("Error in String Log, Ignoring ");
        continue;
      }
      LogEntry le = new LogEntry();
      le.setTime(Double.parseDouble(split[0]));
      le.setLicensePlate(split[1]);
      le.setLocation(Integer.parseInt(split[2].substring(0, split[2].length() - 1)));
      le.setDirection(split[2].substring(split[2].length() - 1));
      le.setSensor(split[3]);
      logs.add(le);
    }

    return logs;
  }


  //Create class LogEntry for this
  static class LogEntry {

    private double time;
    private String licensePlate;
    private int location;
    private String direction;
    private String sensor;// , EXIT , MAINROAD
    private int over120;
    private boolean isOneWayOverSpeed;
    private boolean isTwoWayOverSpeed;
    private boolean isAlreadyflagged;

    public double getTime() {
      return time;
    }

    public void setTime(double time) {
      this.time = time;
    }

    public String getLicensePlate() {
      return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
      this.licensePlate = licensePlate;
    }

    public int getLocation() {
      return location;
    }

    public void setLocation(int location) {
      this.location = location;
    }

    public String getDirection() {
      return direction;
    }

    public void setDirection(String direction) {
      this.direction = direction;
    }

    public String getSensor() {
      return sensor;
    }

    public void setSensor(String sensor) {
      this.sensor = sensor;
    }
  }

  public static void main(String[] args) {
    String[] logLines = {
        "90750.191 JOX304 250E ENTRY",
        "91081.684 JOX304 260E MAINROAD",
        "91082.101 THX138 110E ENTRY",
        "91483.251 JOX304 270E MAINROAD",
        "91873.920 THX138 120E MAINROAD",
        "91874.493 JOX304 280E EXIT",
        "91982.102 THX138 290E EXIT",
        // 170E omitted in example, assume direct for testing or adjust markers
        "92301.302 THX138 300E ENTRY",
        "92371.302 THX138 310E EXIT",
        "1000.000 TST002 270W ENTRY",
        "1275.000 TST002 260W EXIT"
    };

    List<String> result = catchSpeeders(logLines);

    System.out.println(
        "Expected Output: [TST002, THX138] (Order may vary depending on implementation)");
    System.out.println("Actual Output: " + result);

    // Standard Karat environments often use basic asserts or boolean checks
    // if (result.contains("TST002")) { System.out.println("Pass"); }
  }
}



