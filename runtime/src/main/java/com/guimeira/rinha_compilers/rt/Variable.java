package com.guimeira.rinha_compilers.rt;

import com.guimeira.rinha_compilers.rt.value.Value;

public class Variable {
  private Value value;

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }
}
