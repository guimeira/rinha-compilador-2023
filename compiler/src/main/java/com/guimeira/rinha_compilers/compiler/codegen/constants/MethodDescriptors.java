package com.guimeira.rinha_compilers.compiler.codegen.constants;

import com.guimeira.rinha_compilers.rt.value.StrValue;
import org.objectweb.asm.Type;

public class MethodDescriptors {
  //Construtor default (sem parâmetros):
  public static final String DEFAULT_CONSTRUCTOR = Type.getMethodDescriptor(Type.VOID_TYPE);

  //Método main:
  public static final String MAIN = Type.getMethodDescriptor(Type.VOID_TYPE, Types.STRING_ARRAY);

  /**
   * Métodos de {@link com.guimeira.rinha_compilers.rt.value.Value}
   */
  public static class Value {
    public static final String TO_STRING_REPRESENTATION = Type.getMethodDescriptor(Types.STRING);
    public static final String OF_INT = Type.getMethodDescriptor(Types.INT_VALUE, Type.INT_TYPE);
    public static final String OF_BOOL = Type.getMethodDescriptor(Types.BOOL_VALUE, Type.BOOLEAN_TYPE);
    public static final String OF_STR = Type.getMethodDescriptor(Types.STR_VALUE, Types.STRING);
    public static final String OF_TUPLE = Type.getMethodDescriptor(Types.TUPLE_VALUE, Types.VALUE, Types.VALUE);
    public static final String FIRST_SECOND = Type.getMethodDescriptor(Types.VALUE);
    public static final String BINARY_OPERATION = Type.getMethodDescriptor(Types.VALUE, Types.VALUE);
    public static final String BOOLEAN_BINARY_OPERATION = Type.getMethodDescriptor(Types.BOOL_VALUE, Types.VALUE);
    public static final String SET_MEMOIZABLE = Type.getMethodDescriptor(Type.VOID_TYPE, Type.BOOLEAN_TYPE);
    public static final String IS_MEMOIZABLE = Type.getMethodDescriptor(Type.BOOLEAN_TYPE);
  }

  public static class BoolValue {
    public static final String VALUE = Type.getMethodDescriptor(Type.BOOLEAN_TYPE);
  }

  /**
   * Métodos de System.out
   */
  public static class SystemOut {
    public static final String PRINT = Type.getMethodDescriptor(Type.VOID_TYPE, Types.STRING);
  }

  /**
   * Métodos de {@link com.guimeira.rinha_compilers.rt.Variable}
   */
  public static class Variable {
    public static final String SET_VALUE = Type.getMethodDescriptor(Type.VOID_TYPE, Types.VALUE);
    public static final String GET_VALUE = Type.getMethodDescriptor(Types.VALUE);
  }

  public static class ArrayList {
    public static final String CAPACITY_CONSTRUCTOR = Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE);
    public static final String ADD = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.OBJECT);
  }

  public static class LinkedHashMap {
    public static final String GET = Type.getMethodDescriptor(Types.OBJECT, Types.OBJECT);
  }

  public static class HashMap {
    public static final String PUT = Type.getMethodDescriptor(Types.OBJECT, Types.OBJECT, Types.OBJECT);
  }
}
