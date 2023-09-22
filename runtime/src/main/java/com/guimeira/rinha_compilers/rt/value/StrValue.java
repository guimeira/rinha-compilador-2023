package com.guimeira.rinha_compilers.rt.value;

public class StrValue extends Value {
  private String v;

  private StrValue(String v) {
    this.v = v;
  }

  public static StrValue of(String v) {
    return new StrValue(v);
  }

  public String value() {
    return v;
  }

  @Override
  public Value add(Value value) {
    return StrValue.of(v + value.toStringRepresentation());
  }

  @Override
  public Value mul(Value value) {
    if(value instanceof IntValue iv) {
      return StrValue.of(v.repeat(iv.value()));
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public BoolValue eq(Value value) {
    return BoolValue.of(value instanceof StrValue sv && v.equals(sv.v));
  }

  @Override
  public BoolValue neq(Value value) {
    return BoolValue.of(!(value instanceof StrValue sv) || !v.equals(sv.v));
  }

  @Override
  public BoolValue lt(Value value) {
    if(!(value instanceof StrValue)) {
      throw new UnsupportedOperationException();
    }

    return BoolValue.of(v.compareTo(((StrValue)value).v) < 0);
  }

  @Override
  public BoolValue gt(Value value) {
    if(!(value instanceof StrValue)) {
      throw new UnsupportedOperationException();
    }

    return BoolValue.of(v.compareTo(((StrValue)value).v) > 0);
  }

  @Override
  public BoolValue lte(Value value) {
    if(!(value instanceof StrValue)) {
      throw new UnsupportedOperationException();
    }

    return BoolValue.of(v.compareTo(((StrValue)value).v) <= 0);
  }

  @Override
  public BoolValue gte(Value value) {
    if(!(value instanceof StrValue)) {
      throw new UnsupportedOperationException();
    }

    return BoolValue.of(v.compareTo(((StrValue)value).v) >= 0);
  }

  @Override
  public String toStringRepresentation() {
    return v;
  }
}
