package com.guimeira.rinha_compilers.compiler.codegen;

import com.guimeira.rinha_compilers.compiler.ast.Term;
import com.guimeira.rinha_compilers.compiler.codegen.constants.ClosureNames;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Set;

/**
 * Realiza a geração de código a partir de uma AST que já passou pelo preprocessamento.
 */
public class CodeGenerator {
  private CodegenContext ctx;

  public CodeGenerator(Path workDir, boolean writeClasses, boolean runCode) {
    ctx = new CodegenContext(workDir, writeClasses, runCode);
  }

  /**
   * Realiza a geração de código.
   */
  public void process(Term term) {
    ctx.startFunction(0, 0);
    term.codeGen(ctx);
    ctx.endFunction();
    ctx.generateEntryPoint();
  }

  /**
   * Executa o programa.
   */
  public void run() {
    try {
      Class<?> mainClass = ctx.getClassloader().loadClass(ClosureNames.getFullClassName(1));
      Object mainClassInstance = mainClass.getDeclaredConstructor().newInstance();
      Method callMethod = mainClass.getDeclaredMethod(ClosureNames.INTERFACE_METHOD);
      callMethod.invoke(mainClassInstance);
    } catch(Exception e) {
      throw new RuntimeException("Erro ao carregar e executar código compilado", e);
    }
  }

  /**
   * Gera interfaces a serem implementadas pelas classes Closure.
   * @param arities
   */
  public void generateFunctionInterfaces(Set<Integer> arities) {
    arities.forEach(ctx::generateFunctionInterface);
  }

}
