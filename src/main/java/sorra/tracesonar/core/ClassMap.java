package sorra.tracesonar.core;

import java.util.*;

import sorra.tracesonar.model.Method;

public class ClassMap {
  public static final ClassMap INSTANCE = new ClassMap();

  Map<String, ClassOutline> classOutlines = Factory.infoMap();

  public void addClassOutline(String className, ClassOutline classOutline) {
    classOutlines.put(className, classOutline);
  }

  public List<Method> findSuperMethods(Method method) {
    List<String> superTypes = new ArrayList<>();
    findSuperTypes(method.owner, superTypes);
    List<Method> list = new ArrayList<>();
    for (String xtype : superTypes) {
      ClassOutline co = classOutlines.get(xtype);
      if (co != null) exploreSuperMethods(method, co, list);
    }
    return list;
  }

  private void findSuperTypes(String selfName, List<String> types) {
    ClassOutline start = classOutlines.get(selfName);
    if (start == null) return;

    types.add(start.superName);
    start.intfs.stream().filter(intf -> !types.contains(intf)).forEach(types::add);
    // Recursion
    findSuperTypes(start.superName, types);
    start.intfs.forEach(intf -> findSuperTypes(intf, types));
  }

  private void exploreSuperMethods(Method m, ClassOutline co, List<Method> list) {
    co.methods.stream().filter(x -> x.methodName.equals(m.methodName) && x.desc.equals(m.desc)).findAny()
        .ifPresent(list::add);
  }

  static class ClassOutline {
    String superName;
    List<String> intfs;
    List<Method> methods = new ArrayList<>();

    ClassOutline(String superName, String[] intfs) {
      this.superName = superName;
      this.intfs = Arrays.asList(intfs);
    }

    void addMethod(Method method) {
      methods.add(method);
    }
  }
}
