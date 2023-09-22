package com.guimeira.rinha_compilers.rt.value;

public abstract class Value {
  public Value add(Value value) {
    throw new UnsupportedOperationException();
  }

  public Value sub(Value value) {
    throw new UnsupportedOperationException();
  }

  public Value mul(Value value) {
    throw new UnsupportedOperationException();
  }

  public Value div(Value value) {
    throw new UnsupportedOperationException();
  }

  public Value rem(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue eq(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue neq(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue lt(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue gt(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue lte(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue gte(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue and(Value value) {
    throw new UnsupportedOperationException();
  }

  public BoolValue or(Value value) {
    throw new UnsupportedOperationException();
  }

  public Value first() {
    throw new UnsupportedOperationException();
  }

  public Value second() {
    throw new UnsupportedOperationException();
  }

  public String toStringRepresentation() {
    throw new UnsupportedOperationException();
  }
}
