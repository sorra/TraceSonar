package sorra.tracesonar.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import sorra.tracesonar.model.Caller;
import sorra.tracesonar.util.Strings;

import static org.objectweb.asm.Opcodes.ASM5;

public class MethodInsnCollector {
  private String className;
  private Set<String> calledClasses = new HashSet<>();

  private String topClassName;

  public MethodInsnCollector(InputStream classInput) {
    ClassReader classReader;
    try {
      classReader = new ClassReader(classInput);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    classReader.accept(classVisitor, 0);
    GreatMap.INSTANCE.getCallerCollector(topClassName);
  }

  public String getClassName() {
    return className;
  }

  private ClassVisitor classVisitor = new ClassVisitor(ASM5) {
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      className = name;
      topClassName = Strings.substringBefore(className, "$");
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
      super.visitOuterClass(owner, name, desc);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
      super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      Caller caller = new Caller(className, name, desc);
      return new MethodVisitor(ASM5) {
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
          owner = Strings.substringBefore(owner, "$");
          if (owner.equals(topClassName)) { // Ignore self class calls
            return;
          }
          for (String prefix : IGNORE_PACKAGE_PREFIXES) { // Ignore basic libraries
            if (owner.startsWith(prefix)) {
              return;
            }
          }
          GreatMap.INSTANCE.getCallerCollector(owner).regCaller(caller);
          calledClasses.add(owner);
        }
      };
    }
  };

  private static Set<String> IGNORE_PACKAGE_PREFIXES = new HashSet<>();
  static {
    IGNORE_PACKAGE_PREFIXES.add("java/");
    IGNORE_PACKAGE_PREFIXES.add("sun/");
    IGNORE_PACKAGE_PREFIXES.add("com/sun/");
  }
}
