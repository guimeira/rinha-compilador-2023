package com.guimeira.rinha_compilers.compiler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.guimeira.rinha_compilers.compiler.ast.RinhaFile;
import com.guimeira.rinha_compilers.compiler.codegen.CodeGenerator;
import com.guimeira.rinha_compilers.compiler.preprocessing.Preprocessor;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {
  public static void main(String[] args) throws Exception {
    List<String> argsList = Arrays.asList(args);
    boolean runCode = argsList.contains("--run");
    boolean writeClasses = argsList.contains("--write");

    if(!runCode && !writeClasses) {
      throw new RuntimeException("Use --write para escrever arquivos .class e --run para rodar o programa após a compilação");
    }

    String source = argsList.stream().filter(p -> !p.startsWith("--")).findFirst()
            .orElseThrow(() -> new RuntimeException("Caminho do arquivo a ser compilado não foi informado"));
    Path sourcePath = Paths.get(source);

    ObjectMapper jsonMapper = JsonMapper.builder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .build();
    RinhaFile file = jsonMapper.readValue(new FileInputStream(sourcePath.toFile()), RinhaFile.class);

    Preprocessor preprocessor = new Preprocessor();
    preprocessor.preprocess(file);

    CodeGenerator codeGen = new CodeGenerator(sourcePath.getParent(), writeClasses, runCode);
    codeGen.generateFunctionInterfaces(preprocessor.getContext().getFunctionArities());
    codeGen.process(preprocessor);

    if(runCode) {
      codeGen.run();
    }
  }
}
