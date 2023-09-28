package com.guimeira.rinha_compilers.rt.value;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BoolValue boolValue = (BoolValue) o;
    return v == boolValue.v;
  }

  @Override
  public int hashCode() {
    return Objects.hash(v);
  }
}
