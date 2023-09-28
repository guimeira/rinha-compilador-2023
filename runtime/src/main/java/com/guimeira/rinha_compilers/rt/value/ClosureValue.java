package com.guimeira.rinha_compilers.rt.value;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class ClosureValue extends Value {
  protected final LinkedHashMap<List<Value>, Value> memoizedInvocations = new LinkedHashMap<>(100, .75f, true);

  @Override
  public String toStringRepresentation() {
    return "<#closure>";
  }
}
