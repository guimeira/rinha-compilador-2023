package com.guimeira.rinha_compilers.rt.value;

public class TupleValue extends Value {
  private Value v1, v2;

  private TupleValue(Value v1, Value v2) {
    this.v1 = v1;
    this.v2 = v2;
  }

  public static TupleValue of(Value v1, Value v2) {
    return new TupleValue(v1, v2);
  }

  @Override
  public Value first() {
    return v1;
  }

  @Override
  public Value second() {
    return v2;
  }

  @Override
  public BoolValue eq(Value value) {
    return BoolValue.of(
            value instanceof TupleValue tv &&
                    v1.eq(tv.v1) == BoolValue.TRUE &&
                    v2.eq(tv.v2) == BoolValue.TRUE
    );
  }

  @Override
  public BoolValue neq(Value value) {
    return BoolValue.of(
            !(value instanceof TupleValue tv) ||
                    v1.eq(tv.v1) == BoolValue.FALSE &&
                    v2.eq(tv.v2) == BoolValue.FALSE
    );
  }

  @Override
  public String toStringRepresentation() {
    return "(" + v1.toStringRepresentation() + "," + v2.toStringRepresentation() + ")";
  }
}
