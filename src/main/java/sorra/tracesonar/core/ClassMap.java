package sorra.tracesonar.core;

import java.util.*;
import java.util.stream.Collectors;

import sorra.tracesonar.model.Method;

/**
 * Data store of ClassName->ClassOutline pairs
 */
class ClassMap {
  static final ClassMap INSTANCE = new ClassMap();

  private Map<String, ClassOutline> classOutlines = Factory.infoMap();

  void addClassOutline(String className, ClassOutline classOutline) {
    classOutlines.put(className, classOutline);
  }

  Collection<ClassMap.ClassOutline> allClassOutlines() {
    return classOutlines.values().stream()
        .peek(this::fillInheritedMethods)
        .collect(Collectors.toList());
  }

  ClassOutline getClassOutline(String className) {
    ClassOutline classOutline = classOutlines.get(className);
    if (classOutline == null) {
      throw new RuntimeException("Cannot find class: " + className);
    }

    fillInheritedMethods(classOutline);
    return classOutline;
  }

  List<Method> findSuperMethods(Method method) {
    List<String> superTypes = new ArrayList<>();
    findSuperTypes(method.owner, superTypes);

    List<Method> list = new ArrayList<>();
    for (String xtype : superTypes) {
      ClassOutline co = classOutlines.get(xtype);
      if (co != null) {
        exploreSuperMethods(method, co, list);
      }
    }
    return list;
  }

  // Some methods are callable from current class, but only declared in super class
  // In Java reflection, that differs Class#getMethods() and Class#getDeclaredMethods()
  // Bottom-up recursively fill inherited methods to current class
  private void fillInheritedMethods(ClassOutline co) {
    if (co.runtimeMethodsFilled) {
      return;
    }

    co.runtimeMethodsFilled = true;

    if (co.superName == null) {
      return;
    }

    ClassOutline superCo = classOutlines.get(co.superName);
    if (superCo == null) {
      return;
    }
    fillInheritedMethods(superCo);

    for (Method superMethod : superCo.getMethods()) {
      boolean noneMatch = co.getMethods().stream()
          .noneMatch(m -> m.methodName.equals(superMethod.methodName) && m.desc.equals(superMethod.desc));
      if (noneMatch) {
        // add an inherited method like super
        co.addMethod(new Method(co.name, superMethod.methodName, superMethod.desc));
      }
    }
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
    co.methods.stream()
        .filter(x -> x.methodName.equals(m.methodName) && x.desc.equals(m.desc))
        .findAny()
        .ifPresent(list::add);
  }

  static class ClassOutline {
    private final String name;
    private final String superName;
    private final List<String> intfs;

    private final List<Method> methods = new ArrayList<>();

    private boolean runtimeMethodsFilled = false;

    ClassOutline(String name, String superName, String[] intfs) {
      this.name = name;
      this.superName = superName;
      this.intfs = Collections.unmodifiableList(Arrays.asList(intfs));
    }

    void addMethod(Method method) {
      methods.add(method);
    }

    List<Method> getMethods() {
      return methods;
    }
  }
}
