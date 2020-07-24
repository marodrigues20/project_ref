package com.tenx.universalbanking.transactionmanager.utils;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public class FileReaderUtil {

  public String getFileContent(String fileName) throws IOException {
    try (InputStream is = FileReaderUtil.class.getClassLoader().getResourceAsStream(fileName)) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
  }

}
