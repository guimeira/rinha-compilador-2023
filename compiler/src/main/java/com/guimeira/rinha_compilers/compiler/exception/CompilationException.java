package com.guimeira.rinha_compilers.compiler.exception;

import com.guimeira.rinha_compilers.compiler.ast.Loc;

public class CompilationException extends RuntimeException {
  private Loc location;

  public CompilationException(String message, Loc location) {
    super(message);
    this.location = location;
  }

  public CompilationException(String message, Throwable cause) {
    super(message, cause);
  }

  public Loc getLocation() {
    return location;
  }
}
