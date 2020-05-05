package sorra.tracesonar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One-off resolver, create to use for every time, don't reuse!
 */
public class BytecodeMethodParamsResolver {

  private static Map<String, String> primitiveTypeMap = new HashMap<>();
  static {
    primitiveTypeMap.put("Z", "boolean");
    primitiveTypeMap.put("C", "char");
    primitiveTypeMap.put("B", "byte");
    primitiveTypeMap.put("S", "short");
    primitiveTypeMap.put("I", "int");
    primitiveTypeMap.put("F", "float");
    primitiveTypeMap.put("J", "long");
    primitiveTypeMap.put("D", "double");
  }

  private List<String> params = new ArrayList<>();
  private int arrayLayers = 0;

  private void ensureClean() {
    if (!params.isEmpty() || arrayLayers != 0) {
      throw new IllegalStateException(this + " is not clean before usage!");
    }
  }

  public List<String> resolve(String desc) {
    ensureClean();

    desc = desc.substring(desc.indexOf('(') + 1, desc.lastIndexOf(')'));

    int total = desc.length();
    for (int i = 0; i < total; i++) {
      char c = desc.charAt(i);
      if (c == '[') {
        arrayLayers++;
      } else if (c == 'L') {
        int tail = desc.indexOf(';', i);
        if (tail > 0) {
          addParam(desc.substring(i + 1, tail).replace('/', '.').replace('$', '.'));
          i = tail;
        } else {
          throw new IllegalArgumentException("Bytecode method desc " + desc + " has L not tailed with ;");
        }
      } else {
        String type = primitiveTypeMap.get(Character.toString(c));
        if (type != null) {
          addParam(type);
        } else {
          throw new IllegalArgumentException("Bytecode method desc" + desc + " has unknown primitive " + c);
        }
      }
    }

    return params;
  }

  private void addParam(String param) {
    if (arrayLayers > 0) {
      param += repeat("[]", arrayLayers);
      arrayLayers = 0;
    }
    params.add(param);
  }

  private static String repeat(String str, int times) {
    if (times <= 0) {
      return "";
    }

    return String.join("", Collections.nCopies(times, str));
  }
}
