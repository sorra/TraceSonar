package sorra.tracesonar.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import sorra.tracesonar.util.FileUtil;

public class FileWalker {
  public static void walkAll(Collection<String> roots, Collection<String> ignores) {
    try {
      ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      Consumer<InputStream> submitter = in -> es.submit(() -> {
        try (InputStream classInput = in) {
          GreatMap.INSTANCE.addMethodInsnCollector(new MethodInsnCollector(classInput, ignores));
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      for (String root : roots) {
        Path rootPath = Paths.get(root);
        if (rootPath.toFile().isDirectory()) {
          Files.walk(rootPath)
              .forEach(path -> {
                String pathStr = path.toString();
                if (pathStr.endsWith(".class") && !path.toFile().isDirectory()) {
                  handleClassFile(path, submitter);
                }
                if (arSuffixes.stream().anyMatch(pathStr::endsWith)) {
                  handleJarFile(path, submitter);
                }
              });
        } else {
          if (arSuffixes.stream().anyMatch(root::endsWith)) {
            handleJarFile(rootPath, submitter);
          }
        }
      }
      es.shutdown();
      es.awaitTermination(10, TimeUnit.MINUTES);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static void handleJarFile(Path path, Consumer<InputStream> submitter) {
    try {
      JarFile jarFile = new JarFile(path.toFile());
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
          try (InputStream classIn = jarFile.getInputStream(entry)) {
            byte[] classBytes = FileUtil.read(classIn, 1024);
            submitter.accept(new ByteArrayInputStream(classBytes));
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void handleClassFile(Path path, Consumer<InputStream> submitter) {
    try {
      submitter.accept(new FileInputStream(path.toFile()));
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static final Collection<String> arSuffixes = Arrays.asList(".jar", ".war", ".ear");
}
