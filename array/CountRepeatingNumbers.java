package com.dcb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CountRepeatingNumbers {


  public static void main(String[] args) {
    List<Integer> list = List.of(9, 7, 6, 3, 3, 1, 6, 0, 1, 1);
    list.stream()
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream().filter(k -> k.getValue() > 1)
        .forEach(k -> System.out.println(k.getKey()));

    //Without Stream use HashSet or HashMap (Map is more preferred for counter questions)
    Map<Integer, Integer> countMap = new HashMap<>();

    for (Integer i : list) {
//        countMap.compute(i, (k,v)-> v!=null ? v+1 : 1);
      countMap.merge(i, 1, Integer::sum);
    }
    countMap.entrySet().stream().filter(k -> k.getValue() > 1)
        .forEach(k -> System.out.println(k.getKey()));


  }

}
