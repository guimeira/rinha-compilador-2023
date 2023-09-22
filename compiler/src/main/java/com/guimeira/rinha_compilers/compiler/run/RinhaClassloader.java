package com.guimeira.rinha_compilers.compiler.run;

import java.util.HashMap;
import java.util.Map;

/**
 * Este classloader é capaz de carregar as classes que geramos durante a compilação.
 * Ele será usado caso o usuário deseje executar o programa assim que terminarmos a compilação.
 */
public class RinhaClassloader extends ClassLoader {
  private Map<String, byte[]> generatedClasses = new HashMap<>();

  public void addClass(String internalName, byte[] bytecode) {
    String className = internalName.replace('/', '.');
    generatedClasses.put(className, bytecode);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytecode = generatedClasses.get(name);

    if(bytecode == null) {
      //Se a classe sendo procurada não é uma das que nós geramos, delega para o classloader pai:
      return super.findClass(name);
    }

    return defineClass(name, bytecode, 0, bytecode.length);
  }
}
