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

public class FileWalker {

  public static void walkAll(Collection<String> roots, Collection<String> ignores) {
    walkAll(roots, ignores, (path, inputStream) -> {
      try (InputStream classInput = inputStream) {
        GreatMap.INSTANCE.addMethodInsnCollector(new MethodInsnCollector(classInput, ignores));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  public static void walkAll(Collection<String> roots, Collection<String> ignores, BiConsumer<Path, InputStream> consumer) {
    try {
      for (String root : roots) {
        Path rootPath = Paths.get(root);
        if (rootPath.toFile().isDirectory()) {
          Files.walk(rootPath)
              .forEach(path -> {
                String pathStr = path.toString();
                if (pathStr.endsWith(".class") && !path.toFile().isDirectory()) {
                  handleClassFile(path, consumer);
                }
                if (arSuffixes.stream().anyMatch(pathStr::endsWith)) {
                  handleJarFile(path, consumer);
                }
              });
        } else {
          if (arSuffixes.stream().anyMatch(root::endsWith)) {
            handleJarFile(rootPath, consumer);
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void handleJarFile(Path path, BiConsumer<Path, InputStream> submitter) {
    try {
      JarFile jarFile = new JarFile(path.toFile());
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
          try (InputStream classIn = jarFile.getInputStream(entry)) {
            byte[] classBytes = FileUtil.read(classIn, 1024);
            submitter.accept(Paths.get(entry.getName()), new ByteArrayInputStream(classBytes));
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void handleClassFile(Path path, BiConsumer<Path, InputStream> submitter) {
    try {
      submitter.accept(path, new FileInputStream(path.toFile()));
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static final Collection<String> arSuffixes = Arrays.asList(".jar", ".war", ".ear");
}
