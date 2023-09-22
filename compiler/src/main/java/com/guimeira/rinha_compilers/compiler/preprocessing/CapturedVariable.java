package com.guimeira.rinha_compilers.compiler.preprocessing;

/**
 * Representa uma variável capturada de um contexto pai.
 */
public class CapturedVariable extends Variable {
  public Variable variable;

  public CapturedVariable(int id, Variable variable) {
    super(id, variable.name);
    this.variable = variable;
  }
}
