package sorra.tracesonar.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.*;

import sorra.tracesonar.model.Method;
import sorra.tracesonar.util.Pair;
import sorra.tracesonar.util.StringUtil;

import static org.objectweb.asm.Opcodes.ASM5;

public class MethodInsnCollector {

  private final QualifierFilter qualifierFilter;

  private String className;
  private Set<String> calledClasses = new HashSet<>();

  private String topClassName;

  public MethodInsnCollector(InputStream classInput, QualifierFilter qualifierFilter) throws IOException {
    this.qualifierFilter = qualifierFilter;

    ClassReader classReader = new ClassReader(classInput);
    classReader.accept(classVisitor, 0);
  }

  @SuppressWarnings("FieldCanBeLocal")
  private ClassVisitor classVisitor = new ClassVisitor(ASM5) {
    ClassMap.ClassOutline classOutline;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      className = name;
      topClassName = StringUtil.substringBefore(className, "$");
      classOutline = new ClassMap.ClassOutline(name, superName, interfaces);
      ClassMap.INSTANCE.addClassOutline(className, classOutline);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      Method caller = new Method(className, name, desc);

      if ((access & Opcodes.ACC_PRIVATE) == 0 // Non-private
          && !isIgnore(className, name, desc)) {
        classOutline.addMethod(caller);
      }

      return new MethodVisitor(ASM5) {
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
//          if (Strings.substringBefore(owner, "$").equals(topClassName)) { // Ignore self class calls
//            return;
//          }
          if (isIgnore(owner, name, desc)) {
            return;
          }

          Method callee = new Method(owner, name, desc);
          GraphStore.INSTANCE.getCallerCollector(callee).addCaller(caller);
          calledClasses.add(owner);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
          Handle handle = findInvokedHandle(bsmArgs);
          if (handle == null) {
            return;
          }
          if (isIgnore(handle.getOwner(), handle.getName(), handle.getDesc())) {
            return;
          }

          Method callee = new Method(handle.getOwner(), handle.getName(), handle.getDesc());
          GraphStore.INSTANCE.getCallerCollector(callee).addCaller(caller);
          calledClasses.add(handle.getOwner());
        }

        private Handle findInvokedHandle(Object[] bsmArgs) {
          for (Object arg : bsmArgs) {
            if (arg instanceof Handle) return (Handle) arg;
          }
          return null;
        }
      };
    }
  };

  private boolean isIgnore(String owner, String name, String desc) {
//    for (String pkg : IGNORE_PACKAGE) { // Ignore basic libraries
//      if (owner.startsWith(pkg)) {
//        return true;
//      }
//    }

    for (Pair<String, String> meth : IGNORE_METHODS) { // Ignore Object methods
      if (meth._1.equals(name) && meth._2.equals(desc)) {
        return true;
      }
    }

    return !qualifierFilter.filter(q ->
        owner.equals(q) || owner.startsWith(q + '/') || owner.startsWith(q + '$'));
  }

//  private static final Set<String> IGNORE_PACKAGE = new HashSet<>();
//  static {
//    IGNORE_PACKAGE.add("java/");
//    IGNORE_PACKAGE.add("sun/");
//    IGNORE_PACKAGE.add("com/sun/");
//  }

  private static final Set<Pair<String, String>> IGNORE_METHODS = new HashSet<>();
  static {
    IGNORE_METHODS.add(Pair.of("equals", "(Ljava/lang/Object;)Z"));
    IGNORE_METHODS.add(Pair.of("hashCode", "I"));
    IGNORE_METHODS.add(Pair.of("toString", "Ljava/lang/String;"));
  }
}
