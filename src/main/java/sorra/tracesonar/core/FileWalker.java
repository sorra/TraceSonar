package sorra.tracesonar.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileWalker {
  public static void walkAll(String root) {
    try {
      ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      Files.walk(Paths.get(root))
          .filter(path -> path.toString().endsWith(".class"))
          .forEach(path -> es.submit(() -> {
            try (InputStream classInput = new FileInputStream(path.toFile())) {
              GreatMap.INSTANCE.addMethodInsnCollector(new MethodInsnCollector(classInput));
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }));
      es.shutdown();
      es.awaitTermination(10, TimeUnit.MINUTES);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
