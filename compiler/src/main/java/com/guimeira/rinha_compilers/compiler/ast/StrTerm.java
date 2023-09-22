package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.rt.value.StrValue;
import com.guimeira.rinha_compilers.rt.value.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StrTerm extends Term implements ProcessableInCompilationTime {
  public String value;
  public Loc location;

  @JsonCreator
  public StrTerm(String value, Loc location) {
    this.value = value;
    this.location = location;
  }

  @Override
  public Value toRuntimeValue() {
    return StrValue.of(value);
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    MethodVisitor visitor = ctx.getMethodVisitor();
    visitor.visitLdcInsn(value);
    visitor.visitMethodInsn(INVOKESTATIC, InternalNames.Value.STR_VALUE, "of", MethodDescriptors.Value.OF_STR, false);
  }
}
