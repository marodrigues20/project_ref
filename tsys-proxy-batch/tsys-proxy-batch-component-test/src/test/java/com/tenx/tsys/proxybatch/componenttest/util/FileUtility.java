package com.tenx.tsys.proxybatch.componenttest.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtility {

  public static String getFileContent(String fileName) throws IOException, URISyntaxException {
    Path filePath = Paths.get(WiremockUtil.class.getClassLoader()
        .getResource(fileName).toURI());
    return new String(Files.readAllBytes(filePath));
  }

}
