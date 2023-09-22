package com.guimeira.rinha_compilers.compiler.codegen.constants;

import com.guimeira.rinha_compilers.rt.Variable;
import com.guimeira.rinha_compilers.rt.value.*;
import org.objectweb.asm.Type;

import java.io.PrintStream;

/**
 * Nomes internos de classes.
 */
public class InternalNames {
  public static final String OBJECT = Type.getInternalName(Object.class);
  public static final String VARIABLE = Type.getInternalName(Variable.class);
  public static final String VALUE = Type.getInternalName(com.guimeira.rinha_compilers.rt.value.Value.class);
  public static final String SYSTEM = Type.getInternalName(System.class);
  public static final String PRINT_STREAM = Type.getInternalName(PrintStream.class);

  /**
   * Subclasses de {@link com.guimeira.rinha_compilers.rt.value.Value}
   */
  public static class Value {
    public static final String CLOSURE_VALUE = Type.getInternalName(ClosureValue.class);
    public static final String INT_VALUE = Type.getInternalName(IntValue.class);
    public static final String BOOL_VALUE = Type.getInternalName(BoolValue.class);
    public static final String STR_VALUE = Type.getInternalName(StrValue.class);
    public static final String TUPLE_VALUE = Type.getInternalName(TupleValue.class);
  }
}

