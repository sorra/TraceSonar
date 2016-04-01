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
  public static void walkAll(Collection<String> files) {
    try {
      ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      Consumer<InputStream> submitter = in -> es.submit(() -> {
        try (InputStream classInput = in) {
          GreatMap.INSTANCE.addMethodInsnCollector(new MethodInsnCollector(classInput));
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      for (String file : files) {
        Path fPath = Paths.get(file);
        if (fPath.toFile().isDirectory()) {
          Files.walk(fPath)
              .filter(path -> path.toString().endsWith(".class") && !path.toFile().isDirectory())
              .forEach(path -> {
                try {
                  submitter.accept(new FileInputStream(path.toFile()));
                } catch (FileNotFoundException e) {
                  throw new UncheckedIOException(e);
                }
              });
        } else {
          if (arSuffixes.stream().anyMatch(file::endsWith)) {
            JarFile jarFile = new JarFile(fPath.toFile());
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
          }
        }
      }
      es.shutdown();
      es.awaitTermination(10, TimeUnit.MINUTES);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static final Collection<String> arSuffixes = Arrays.asList(".jar", ".war", ".ear");
}
