package com.tenxbanking.cardrails.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.core.io.ClassPathResource;

public class FileUtils {
  public static String readFile(String file) {
    try {
      return Files.readString(Paths.get(new ClassPathResource(file).getURI()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
