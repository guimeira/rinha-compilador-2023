package com.guimeira.rinha_compilers.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class CompilationTest {
  static Stream<Path> methodSource() throws Exception {
    Path resourcesFolder = Path.of("src/test/resources");

    List<Path> paths = new ArrayList<>();
    try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(resourcesFolder, "*.json")) {
      dirStream.forEach(paths::add);
    }

    return paths.stream();
  }

  /**
   * Roda vários programas e verifica que a saída bate com o esperado.
   * Tá longe de ser o ideal, mas isso deve garantir que as funcionalidades básicas funcionam.
   */
  @ParameterizedTest
  @MethodSource("methodSource")
  void compilationTest(Path input) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(output);
    System.setOut(printStream);

    Main.main(new String[] { "--run", input.toRealPath().toString() });

    printStream.flush();
    String outputFileName = input.getFileName().toString().split("\\.")[0] + ".out";
    Path outputFile = input.getParent().resolve(outputFileName);
    String expectedOutput = Files.readString(outputFile);
    String outputStr = output.toString(StandardCharsets.UTF_8);
    Assertions.assertEquals(expectedOutput.trim(), outputStr.trim());
  }
}
