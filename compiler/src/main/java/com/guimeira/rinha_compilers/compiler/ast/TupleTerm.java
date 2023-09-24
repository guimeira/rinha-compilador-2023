package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class TupleTerm extends Term {
  public Term first;
  public Term second;
  public Loc location;

  @JsonCreator
  public TupleTerm(Term first, Term second, Loc location) {
    this.first = first;
    this.second = second;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    ctx.markAsNotTailCall();
    first = first.preprocess(ctx);
    second = second.preprocess(ctx);
    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    first.codeGen(ctx);
    second.codeGen(ctx);

    MethodVisitor visitor = ctx.getMethodVisitor();
    visitor.visitMethodInsn(INVOKESTATIC, InternalNames.Value.TUPLE_VALUE, "of", MethodDescriptors.Value.OF_TUPLE, false);
  }
}
