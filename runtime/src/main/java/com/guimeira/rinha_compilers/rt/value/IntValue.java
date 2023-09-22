package com.guimeira.rinha_compilers.rt.value;

public class IntValue extends Value {
  private int v;

  private IntValue(int v) {
    this.v = v;
  }

  public static IntValue of(int v) {
    return new IntValue(v);
  }

  public int value() {
    return v;
  }

  @Override
  public Value add(Value value) {
    //Se o outro operando for int, soma:
    if(value instanceof IntValue iv) {
      return IntValue.of(v + iv.v);
    }

    //Se for string, concatena:
    if(value instanceof StrValue sv) {
      return StrValue.of(v + sv.value());
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public Value sub(Value value) {
    if(value instanceof IntValue iv) {
      return IntValue.of(v - iv.v);
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Value mul(Value value) {
    //Se o outro operando for int, soma:
    if(value instanceof IntValue iv) {
      return IntValue.of(v * iv.v);
    }

    //Se for string, repete a string (PyRinha):
    if(value instanceof StrValue sv) {
      return StrValue.of(sv.value().repeat(v));
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public Value div(Value value) {
    if(value instanceof IntValue iv) {
      return IntValue.of(v / iv.v);
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Value rem(Value value) {
    if(value instanceof IntValue iv) {
      return IntValue.of(v % iv.v);
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public BoolValue eq(Value value) {
    return BoolValue.of(value instanceof IntValue iv && v == iv.v);
  }

  @Override
  public BoolValue neq(Value value) {
    return BoolValue.of(!(value instanceof IntValue iv) || v != iv.v);
  }

  @Override
  public BoolValue lt(Value value) {
    return BoolValue.of(value instanceof IntValue iv && v < iv.v);
  }

  @Override
  public BoolValue gt(Value value) {
    return BoolValue.of(value instanceof IntValue iv && v > iv.v);
  }

  @Override
  public BoolValue lte(Value value) {
    return BoolValue.of(value instanceof IntValue iv && v <= iv.v);
  }

  @Override
  public BoolValue gte(Value value) {
    return BoolValue.of(value instanceof IntValue iv && v >= iv.v);
  }

  @Override
  public String toStringRepresentation() {
    return String.valueOf(v);
  }
}
