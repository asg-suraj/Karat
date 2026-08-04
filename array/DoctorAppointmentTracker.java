package com.dcb.array;

import java.util.*;
import java.util.stream.Collectors;

public class DoctorAppointmentTracker {

    // ==========================================
    // DATA MODEL
    // ==========================================
    public static class Appointment {
        private String appointmentId;
        private String doctorName;
        private String appointmentType;
        private Integer durationInMinutes;

        public Appointment(String appointmentId, String doctorName, String appointmentType, Integer durationInMinutes) {
            this.appointmentId = appointmentId;
            this.doctorName = doctorName;
            this.appointmentType = appointmentType;
            this.durationInMinutes = durationInMinutes;
        }

        public String getAppointmentId() { return appointmentId; }
        public String getDoctorName() { return doctorName; }
        public String getAppointmentType() { return appointmentType; }
        public Integer getDurationInMinutes() { return durationInMinutes; }
    }

    // ==========================================
    // QUESTION 4: BUG FIX
    // Fix the bug in this method so it correctly 
    // counts appointments by type.
    // ==========================================
    public static Map<String, Integer> countAppointmentsByType(List<Appointment> appointments) {
        Map<String, Integer> typeCounts = new HashMap<>();

        if (appointments == null) {
            return typeCounts;
        }

        for (Appointment appt : appointments) {
            if (appt == null || appt.getAppointmentType() == null) {
                continue;
            }
            
            String type = appt.getAppointmentType();

            // TODO: FIX THE BUG HERE
            if (typeCounts.containsKey(type)) {
                typeCounts.put(type, typeCounts.get(type) + 1);
            } else {
                typeCounts.put(type, 1);
            }
        }

        return typeCounts;
    }

    // ==========================================
    // QUESTION 5: IMPLEMENTATION FROM SCRATCH
    // Calculate average duration in minutes for 
    // a given appointment type. Return 0.0 if none.
    // ==========================================
    public static double calculateAverageDuration(List<Appointment> appointments, String targetType) {
        // TODO: Implement this method from scratch
//       AI answers
//        return appointments.stream()
//            .filter(a -> a != null
//                && targetType.equalsIgnoreCase(a.getAppointmentType())
//                && a.getDurationInMinutes() != null)
//            .mapToInt(Appointment::getDurationInMinutes)
//            .average()
//            .orElse(0.0);

        if(appointments==null || appointments.isEmpty()){
            return 0.0;
        }
        return appointments.stream().filter(a->a.appointmentType.equals(targetType)).mapToInt(Appointment::getDurationInMinutes)
            .average().orElse(0.0);

    }

    // ==========================================
    // MAIN METHOD / TEST RUNNER
    // ==========================================
    public static void main(String[] args) {
        List<Appointment> sampleAppointments = Arrays.asList(
            new Appointment("A101", "Dr. Smith", "General Checkup", 30),
            new Appointment("A102", "Dr. Jones", "Follow-up", 15),
            new Appointment("A103", "Dr. Smith", "General Checkup", 45),
            new Appointment("A104", "Dr. Lee", "Consultation", 60),
            new Appointment("A105", "Dr. Jones", "Follow-up", 30),
            new Appointment("A106", "Dr. Smith", "General Checkup", 15)
        );

        // Test Question 4
        System.out.println("--- Q4: Counts By Type ---");
        Map<String, Integer> counts = countAppointmentsByType(sampleAppointments);
        System.out.println("Result: " + counts);
        // Expected: {General Checkup=3, Follow-up=2, Consultation=1}

        // Test Question 5
        System.out.println("\n--- Q5: Average Duration ---");
        double avgGeneral = calculateAverageDuration(sampleAppointments, "General Checkup");
        System.out.println("Avg for 'General Checkup': " + avgGeneral); 
        // Expected: 30.0  ((30 + 45 + 15) / 3)

        double avgFollowUp = calculateAverageDuration(sampleAppointments, "Follow-up");
        System.out.println("Avg for 'Follow-up': " + avgFollowUp); 
        // Expected: 22.5  ((15 + 30) / 2)

        double avgUnknown = calculateAverageDuration(sampleAppointments, "Emergency");
        System.out.println("Avg for 'Emergency': " + avgUnknown); 
        // Expected: 0.0
    }
}