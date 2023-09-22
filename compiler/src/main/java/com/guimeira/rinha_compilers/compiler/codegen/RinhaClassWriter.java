package com.guimeira.rinha_compilers.compiler.codegen;

import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import org.objectweb.asm.ClassWriter;

import java.util.regex.Pattern;

/**
 * O ClassWriter padrão do ASM é capaz de gerar os stack maps automaticamente, mas ele faz o uso do método
 * getCommonSuperClass para determinar a superclasse em comum entre duas classes. Esse método carrega as duas classes
 * e usa reflection para determinar a superclasse em comum. Este método lança uma exceção se uma das classes passadas
 * como parâmetro é uma classe gerada durante o processo de compilação, visto que essa classe ainda não faz parte do
 * classpath e, portanto, não pode ser carregada.
 * Aqui, extendemos o ClassWriter do ASM e sobrescrevemos getCommonSuperclass para dar tratamento especial às classes
 * que estamos gerando durante a compilação. Como não estamos usando nenhum tipo de hierarquia entre as classes,
 * a classe comum entre uma das nossas classes geradas e qualquer outra classe é Object.
 */
public class RinhaClassWriter extends ClassWriter {
  private static final Pattern CLOSURE_PATTERN = Pattern.compile(CodegenContext.PACKAGE_NAME + "/Closure[0-9]+$");
  private static final Pattern INTERFACE_PATTERN = Pattern.compile(CodegenContext.PACKAGE_NAME + "/F[0-9]+$");

  public RinhaClassWriter(int flags) {
    super(flags);
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    if(CLOSURE_PATTERN.matcher(type1).matches() || CLOSURE_PATTERN.matcher(type2).matches() ||
    INTERFACE_PATTERN.matcher(type1).matches() || INTERFACE_PATTERN.matcher(type2).matches()) {
      return InternalNames.OBJECT;
    }

    return super.getCommonSuperClass(type1, type2);
  }
}
