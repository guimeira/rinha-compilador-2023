package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.rt.value.IntValue;
import com.guimeira.rinha_compilers.rt.value.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class IntTerm extends Term implements ProcessableInCompilationTime {
  public int value;
  public Loc location;

  @JsonCreator
  public IntTerm(int value, Loc location) {
    this.value = value;
    this.location = location;
  }

  @Override
  public Value toRuntimeValue() {
    return IntValue.of(value);
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    MethodVisitor visitor = ctx.getMethodVisitor();
    visitor.visitLdcInsn(Integer.valueOf(value));
    visitor.visitMethodInsn(INVOKESTATIC, InternalNames.Value.INT_VALUE, "of", MethodDescriptors.Value.OF_INT, false);
  }
}
