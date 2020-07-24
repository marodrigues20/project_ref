package com.tenx.universalbanking.transactionmanager.componenttest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public class FileUtils {

  public static String getFileContent(String file) throws IOException {
    try (InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(file)) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
  }
}
