package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Loc {
  public int start;
  public int end;
  public String filename;

  @JsonCreator
  public Loc(int start, int end, String filename) {
    this.start = start;
    this.end = end;
    this.filename = filename;
  }
}
