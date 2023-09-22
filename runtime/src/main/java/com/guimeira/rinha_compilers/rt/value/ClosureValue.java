package com.guimeira.rinha_compilers.rt.value;

public abstract class ClosureValue extends Value {
  @Override
  public String toStringRepresentation() {
    return "<#closure>";
  }
}
