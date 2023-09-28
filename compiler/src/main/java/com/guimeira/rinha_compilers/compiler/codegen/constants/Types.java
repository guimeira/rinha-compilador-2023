package com.guimeira.rinha_compilers.compiler.codegen.constants;

import com.guimeira.rinha_compilers.rt.Variable;
import com.guimeira.rinha_compilers.rt.value.*;
import org.objectweb.asm.Type;

import java.io.PrintStream;

/**
 * Tipos
 */
public class Types {
  public static final Type OBJECT = Type.getType(Object.class);
  public static final Type VALUE = Type.getType(Value.class);
  public static final Type VARIABLE = Type.getType(Variable.class);
  public static final Type INT_VALUE = Type.getType(IntValue.class);
  public static final Type BOOL_VALUE = Type.getType(BoolValue.class);
  public static final Type STR_VALUE = Type.getType(StrValue.class);
  public static final Type TUPLE_VALUE = Type.getType(TupleValue.class);
  public static final Type STRING = Type.getType(String.class);
  public static final Type STRING_ARRAY = Type.getType(String[].class);
  public static final Type OVERRIDE = Type.getType(Override.class);
}
