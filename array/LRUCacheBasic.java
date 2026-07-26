package com.dcb.tree;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

//Basic LRUCache in Java
public class LRUCacheBasic {

  public static void main(String[] args) {
    int maxCapacity = 4; //Size of Cache
    Map<Integer, Integer> lruCache = new LinkedHashMap<>(maxCapacity, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Entry<Integer, Integer> eldest) {
        return size() > maxCapacity;
      }
    };
    //  The Thread Safe version of basic LRU Cache 
//    Map<Integer, Integer> lruCache = Collections.synchronizedMap(
//        new LinkedHashMap<>(maxCapacity, 0.75f, true) {
//          @Override
//          protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
//            return size() > maxCapacity;
//          }
//        }
//    );
    lruCache.put(1, 2);
    lruCache.put(2, 3);
    lruCache.put(3, 4);
    lruCache.put(4, 5);

    System.out.println(lruCache);

    lruCache.put(6, 7);

    lruCache.get(2);
    System.out.println(lruCache);
    lruCache.get(4);
    System.out.println(lruCache);


  }

}
