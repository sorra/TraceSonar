package sorra.tracesonar.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Factory {
  public static <K, V> Map<K, V> infoMap() {
    return new ConcurrentHashMap<>();
  }

  public static <E> Set<E> infoSet() {
    return Collections.newSetFromMap(new ConcurrentHashMap<>());
  }
}
