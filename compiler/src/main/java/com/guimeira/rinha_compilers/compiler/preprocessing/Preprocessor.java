package com.guimeira.rinha_compilers.compiler.preprocessing;

import com.guimeira.rinha_compilers.compiler.ast.*;

/**
 * Realiza o preprocessamento de uma AST.
 * Neste compilador, o preprocessamento é responsável por determinar quais variáveis cada função precisa capturar do
 * contexto externo. Este processo também resolve expressões aritméticas que não envolvam variáveis.
 */
public class Preprocessor {
  private PreprocessingContext ctx;
  private Term preprocessedAst;

  public void preprocess(RinhaFile file) {
    ctx = new PreprocessingContext();
    ctx.pushScope();
    preprocessedAst = file.expression.preprocess(ctx);
    ctx.popScope();
  }

  public PreprocessingContext getContext() {
    return ctx;
  }

  public Term getPreprocessedAst() {
    return preprocessedAst;
  }
}
