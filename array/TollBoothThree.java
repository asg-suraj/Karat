package com.dcb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
New Approcach and most efficent for TollBoothTwo 


*/
public class TollBoothThree {

  public static List<String> catchSpeeders(String[] logLines) {
    List<String> speeders = new ArrayList<>();

    // TODO: Implement your logic here
    List<LogClass> logEntries = new ArrayList<>();
    for (String log : logLines) {
      logEntries.add(getLogClass(log));
    }

    Map<String, JourneyTracker> trackerMap = new HashMap<>();
    for (LogClass lc : logEntries) {

      if (lc.getSensor().equals("ENTRY")) {
        trackerMap.put(lc.getNumPlate(), new JourneyTracker(lc.getTimeStamp(), lc.getLocation()));
      }
      if (lc.getSensor().equals("MAINROAD") || lc.getSensor().equals("EXIT")) {
        JourneyTracker jt = trackerMap.get(lc.getNumPlate());
        if (!jt.alreadyFlaggedAndAdded) {

          calculateAndUpdateJourneyTracker(jt, lc);
          if (jt.over130 || jt.over120) {
            speeders.add(lc.getNumPlate());
            jt.alreadyFlaggedAndAdded = true;
          }
        }
      }

      if (lc.getSensor().equals("EXIT")) {
        //remove tracking of this journey !!
        trackerMap.remove(lc.getNumPlate());
      }
    }

    return speeders;
  }

  private static void calculateAndUpdateJourneyTracker(JourneyTracker jt, LogClass lc) {

    double distance = Math.abs(lc.getLocation() - jt.location);
    double time = Math.abs(lc.getTimeStamp() - jt.timeStampFromLastSensor) / 3600.0;
    if (time <= 0) {
      //to resolve arithmetic Exception
      return;
    }
    double speed = distance / time;

    if (speed >= 130) {
      jt.over130 = true;
    }

    if (speed >= 120 && speed <= 130) {
      jt.over120Count++;
      if (!(jt.over120Count < 2)) {
        jt.over120 = true;

      }
    }

    jt.timeStampFromLastSensor = lc.getTimeStamp();
    jt.location = lc.getLocation();

  }

  static class JourneyTracker {

    private Double timeStampFromLastSensor;
    private boolean over120;
    private boolean over130;
    private boolean alreadyFlaggedAndAdded;
    private int over120Count;
    private int location;

    public JourneyTracker(Double timeStampFromLastSensor, int location) {
      this.timeStampFromLastSensor = timeStampFromLastSensor;
      over120 = false;
      over130 = false;
      over120Count = 0;
      this.location = location;
    }
  }

  private static LogClass getLogClass(String log) {
    String[] split = log.split(" ");
    LogClass l = new LogClass();
    l.setTimeStamp(Double.parseDouble(split[0]));
    l.setNumPlate(split[1]);
    l.setLocation(Integer.parseInt(split[2].substring(0, split[2].length() - 1)));
    l.setDirection(split[2].substring(split[2].length() - 1));
    l.setSensor(split[3]);
    return l;
  }

  static class LogClass {

    private Double timeStamp;
    private String numPlate;
    private int location;
    private String direction;
    private String sensor;

    public Double getTimeStamp() {
      return timeStamp;
    }

    public void setTimeStamp(Double timeStamp) {
      this.timeStamp = timeStamp;
    }

    public String getNumPlate() {
      return numPlate;
    }

    public void setNumPlate(String numPlate) {
      this.numPlate = numPlate;
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
    String[] logLines = {"90750.191 JOX304 250E ENTRY", "91081.684 JOX304 260E MAINROAD",
        "91082.101 THX138 110E ENTRY", "91483.251 JOX304 270E MAINROAD",
        "91873.920 THX138 120E MAINROAD", "91874.493 JOX304 280E EXIT",
        "91982.102 THX138 290E EXIT", "92301.302 THX138 300E ENTRY", "92371.302 THX138 310E EXIT",
        "1000.000 TST002 270W ENTRY", "1275.000 TST002 260W EXIT"};

    List<String> result = catchSpeeders(logLines);

    System.out.println("Expected Output: [TST002, THX138] (Order may vary)");
    System.out.println("Actual Output: " + result);
  }
}
