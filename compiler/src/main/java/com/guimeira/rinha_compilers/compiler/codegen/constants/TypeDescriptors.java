package com.guimeira.rinha_compilers.compiler.codegen.constants;

import com.guimeira.rinha_compilers.rt.Variable;
import com.guimeira.rinha_compilers.rt.value.Value;
import org.objectweb.asm.Type;

import java.io.PrintStream;

/**
 * Descritores de tipos
 */
public class TypeDescriptors {
  public static final String VALUE = Type.getDescriptor(Value.class);
  public static final String PRINT_STREAM = Type.getDescriptor(PrintStream.class);
  public static final String VARIABLE = Type.getDescriptor(Variable.class);
}
