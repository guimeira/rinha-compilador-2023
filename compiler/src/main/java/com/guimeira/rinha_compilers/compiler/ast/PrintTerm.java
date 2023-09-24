package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.codegen.constants.TypeDescriptors;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class PrintTerm extends Term {
  public Term value;
  public Loc location;

  @JsonCreator
  public PrintTerm(Term value, Loc location) {
    this.value = value;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    //Uma chamada de função dentro do print não pode ser uma tail call:
    ctx.markAsNotTailCall();

    value = value.preprocess(ctx);
    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    MethodVisitor visitor = ctx.getMethodVisitor();

    //Processar o parâmetro do print. Quando este código terminar, devemos ter um Value no topo da pilha:
    value.codeGen(ctx); //pilha: VALUE

    //Duplicar o valor. Após a execução do print, queremos deixar este valor no topo da pilha:
    visitor.visitInsn(DUP); //pilha: VALUE VALUE

    //Colocar o System.out na pilha:
    visitor.visitFieldInsn(GETSTATIC, InternalNames.SYSTEM, "out", TypeDescriptors.PRINT_STREAM); //pilha: VALUE VALUE S.OUT
    visitor.visitInsn(SWAP); //pilha: VALUE S.OUT VALUE

    //Chamar toStringRepresentation no value que estava no topo da pilha:
    visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "toStringRepresentation", MethodDescriptors.Value.TO_STRING_REPRESENTATION, false); //pilha: VALUE S.OUT STR

    //Agora a pilha contém o System.out seguido da string a ser impressa. Vamos chamar o método print to System.out:
    visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.PRINT_STREAM, "println", MethodDescriptors.SystemOut.PRINT, false); //pilha: VALUE
  }
}
