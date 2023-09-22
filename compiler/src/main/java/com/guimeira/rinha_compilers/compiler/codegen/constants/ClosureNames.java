package com.guimeira.rinha_compilers.compiler.codegen.constants;

import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import org.objectweb.asm.Type;

import java.util.Arrays;

/**
 * Constantes e geração de métodos referentes às classes Closure que vamos gerar.
 */
public class ClosureNames {
  //Nome da classe (este nome será seguido de um número):
  public static final String CLASS_NAME_PREFIX = "Closure";

  //Nome do método que a classe irá conter:
  public static final String INTERFACE_METHOD = "call";

  //Nome de cada atributo da classe (este nome será seguido de um número):
  public static final String CAPTURED_FIELD_PREFIX = "captured";

  /**
   * Gera o nome interno (com /s) de uma classe que vamos gerar.
   * A classe será chamada Closure[x] onde [x] é o index passado como parâmetro.
   */
  public static String getFullClassInternalName(int index) {
    return CodegenContext.PACKAGE_NAME + CLASS_NAME_PREFIX + index;
  }

  /**
   * Gera o nome externo (com .s) de uma classe que vamos gerar.
   */
  public static String getFullClassName(int index) {
    return getFullClassInternalName(index).replace('/','.');
  }

  /**
   * Gera o descritor do construtor da classe que vamos gerar.
   * @param arity aridade do construtor (o construtor recebe todas as variaveis capturadas do contexto pai)
   */
  public static String getConstructorDescriptor(int arity) {
    Type[] constructorParams = new Type[arity];
    Arrays.fill(constructorParams, Types.VARIABLE);
    return Type.getMethodDescriptor(Type.VOID_TYPE, constructorParams);
  }

  /**
   * Gera o descritor do método que contém a lógica da função que corresponde a esta classe.
   * @param arity aridade do método (este método recebe cada um dos parâmetros passados para a função)
   */
  public static String getInterfaceMethodDescriptor(int arity) {
    Type[] params = new Type[arity];
    Arrays.fill(params, Types.VALUE);
    return Type.getMethodDescriptor(Types.VALUE, params);
  }

  /**
   * Gera o nome de um dos atributos da classe que estamos gerando.
   */
  public static String getCapturedFieldName(int index) {
    return CAPTURED_FIELD_PREFIX + index;
  }

  /**
   * Gera o nome da interface que conterá o método call desta classe.
   * Não inclui nome do pacote.
   */
  public static String getInterfaceName(int arity) {
    return "F" + arity;
  }

  /**
   * Gera o nome da interface que conterá o método call desta classe.
   * Inclui nome do pacote.
   */
  public static String getFullInterfaceName(int arity) {
    return CodegenContext.PACKAGE_NAME + getInterfaceName(arity);
  }
}
