package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RinhaFile {
  public String name;
  public Term expression;
  public Loc location;

  @JsonCreator
  public RinhaFile(String name, Term expression, Loc location) {
    this.name = name;
    this.expression = expression;
    this.location = location;
  }
}
