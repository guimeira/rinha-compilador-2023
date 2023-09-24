package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class SecondTerm extends Term {
  public Term value;
  public Loc location;

  @JsonCreator
  public SecondTerm(Term value, Loc location) {
    this.value = value;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    //Se houver uma chamada de função no parâmetro de second() ele não pode ser uma tail call já que teremos que realizar esta operação em seguida:
    ctx.markAsNotTailCall();

    value = value.preprocess(ctx);
    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    //Ao final, um TupleValue deve estar no topo da pilha:
    value.codeGen(ctx);

    MethodVisitor visitor = ctx.getMethodVisitor();
    visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "second", MethodDescriptors.Value.FIRST_SECOND, false);
  }
}
