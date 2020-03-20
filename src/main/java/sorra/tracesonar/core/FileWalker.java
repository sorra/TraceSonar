package sorra.tracesonar.core;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import sorra.tracesonar.util.FileUtil;

/**
 * Scans directories and files, applying the consumer to each class file
 */
public class FileWalker {

  public static void walkAll(Collection<String> roots, QualifierFilter qualifierFilter) {
    BiConsumer<Path, InputStream> classConsumer = (path, inputStream) -> {
      if (!qualifierFilter.filterClassFile(path.toString())) {
        return;
      }

      try (InputStream classInput = inputStream) {
        new MethodInsnCollector(classInput, qualifierFilter);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } catch (Throwable t) {
        System.err.printf("class_file=%s, error=%s", path, t);
        throw t;
      }
    };

    try {
      walkAll(roots, classConsumer);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void walkAll(Collection<String> roots, BiConsumer<Path, InputStream> consumer) throws IOException {
    for (String root : roots) {
      Path rootPath = Paths.get(root);

      if (rootPath.toFile().isDirectory()) {
        Files.walk(rootPath)
            .forEach(path -> {
              if (isClassFile(path)) {
                handleClassFile(path, consumer);
              } else if (isJarFile(path)) {
                handleJarFile(path, consumer);
              }
            });
      } else if (isJarFile(rootPath)) {
        handleJarFile(rootPath, consumer);
      }
    }
  }

  private static boolean isClassFile(Path path) {
    String string = path.toString();
    return string.endsWith(".class") && !string.endsWith("-info.class") && !path.toFile().isDirectory();
  }

  private static boolean isJarFile(Path path) {
    return AR_SUFFIXES.stream().anyMatch(path.toString()::endsWith);
  }

  private static void handleClassFile(Path path, BiConsumer<Path, InputStream> consumer) {
    try {
      consumer.accept(path, new FileInputStream(path.toFile()));
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void handleJarFile(Path jarPath, BiConsumer<Path, InputStream> consumer) {
    try {
      JarFile jarFile = new JarFile(jarPath.toFile());
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (jarEntryIsClassFile(entry)) {
          try (InputStream classIn = jarFile.getInputStream(entry)) {
            byte[] classBytes = FileUtil.read(classIn, 1024);
            consumer.accept(Paths.get(jarPath + "!" + entry.getName()), new ByteArrayInputStream(classBytes));
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static boolean jarEntryIsClassFile(JarEntry entry) {
    String string = entry.getName();
    return string.endsWith(".class") && !string.endsWith("-info.class") && !entry.isDirectory();
  }

  private static final Collection<String> AR_SUFFIXES = Arrays.asList(".jar", ".war", ".ear");
}
