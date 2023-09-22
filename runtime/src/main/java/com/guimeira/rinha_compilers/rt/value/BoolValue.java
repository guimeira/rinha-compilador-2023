package com.guimeira.rinha_compilers.rt.value;

public class BoolValue extends Value {
  public static BoolValue TRUE = new BoolValue(true);
  public static BoolValue FALSE = new BoolValue(false);

  private boolean v;

  public static BoolValue of(boolean v) {
    if(v) {
      return BoolValue.TRUE;
    }

    return BoolValue.FALSE;
  }

  private BoolValue(boolean v) {
    this.v = v;
  }

  public boolean value() {
    return v;
  }

  @Override
  public BoolValue eq(Value value) {
    //Só existem duas instâncias de BoolValue, então podemos comparar com ==
    return BoolValue.of(this == value);
  }

  @Override
  public BoolValue neq(Value value) {
    return BoolValue.of(this != value);
  }

  @Override
  public BoolValue and(Value value) {
    if(value instanceof BoolValue bv) {
      return BoolValue.of(v && bv.v);
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public BoolValue or(Value value) {
    if(value instanceof BoolValue bv) {
      return BoolValue.of(v || bv.v);
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public String toStringRepresentation() {
    return String.valueOf(v);
  }
}
