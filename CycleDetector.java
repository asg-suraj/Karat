package com.dcb;

import java.util.*;

public class CycleDetector {

    // 1. We use an Enum for our 3 colors so the code reads like plain English.
    private enum State {
        UNVISITED, // White: Haven't seen it yet
        VISITING,  // Gray: Currently exploring this path
        DONE       // Black: Safe, no cycles here
    }

    public static List<String> findCycle(Map<String, List<String>> graph) {
        // These HashMaps act as our tracking ledgers
        Map<String, State> colors = new HashMap<>();
        Map<String, String> parents = new HashMap<>();
        List<String> cycle = new ArrayList<>();

        // First, paint every single account UNVISITED (White)
        for (String account : graph.keySet()) {
            colors.put(account, State.UNVISITED);
            for (String neighbor : graph.get(account)) {
                colors.put(neighbor, State.UNVISITED); // Make sure receivers are painted too
            }
        }

        // Pick an account and start walking!
        for (String startAccount : colors.keySet()) {
            // Only start exploring if it's White
            if (colors.get(startAccount) == State.UNVISITED) {
                if (explore(startAccount, graph, colors, parents, cycle)) {
                    return cycle; // Alarm went off! Stop everything and return the loop.
                }
            }
        }
        
        return new ArrayList<>(); // If we check everything and find nothing, return empty.
    }

    // 2. The Explorer Method (DFS)
    private static boolean explore(String current, 
                                   Map<String, List<String>> graph, 
                                   Map<String, State> colors, 
                                   Map<String, String> parents, 
                                   List<String> cycle) {
                                       
        // Step A: Paint the current account Gray (VISITING)
        colors.put(current, State.VISITING);

        // Step B: Look at all the accounts this one transfers money to
        List<String> transfersTo = graph.getOrDefault(current, new ArrayList<>());
        
        for (String nextAccount : transfersTo) {
            State nextColor = colors.get(nextAccount);

            if (nextColor == State.UNVISITED) {
                // It's White! Leave a breadcrumb and explore down this new path.
                parents.put(nextAccount, current);
                if (explore(nextAccount, graph, colors, parents, cycle)) {
                    return true; // Pass the alarm back up the chain
                }
            } 
            else if (nextColor == State.VISITING) {
                // CYCLE DETECTED! We hit a Gray account.
                buildCycleReport(current, nextAccount, parents, cycle);
                return true; 
            }
        }

        // Step C: Hit a dead end? Paint it Black (DONE) and rewind.
        colors.put(current, State.DONE);
        return false;
    }

    // 3. The Report Builder
    private static void buildCycleReport(String current, String loopStart, 
                                         Map<String, String> parents, 
                                         List<String> cycle) {
        cycle.add(current);
        String step = current;
        
        // Trace backward using the breadcrumbs until we close the loop
        while (!step.equals(loopStart)) {
            step = parents.get(step);
            cycle.add(step);
        }
        
        // The list is backward (e.g., [A, C, B, A]), so we reverse it to [A, B, C, A]
        Collections.reverse(cycle);
        cycle.add(loopStart); // close the visual loop at the end
    }
}
