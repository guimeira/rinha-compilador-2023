package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.rt.value.BoolValue;
import com.guimeira.rinha_compilers.rt.value.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class BoolTerm extends Term implements ProcessableInCompilationTime {
  public boolean value;
  public Loc location;

  @JsonCreator
  public BoolTerm(boolean value, Loc location) {
    this.value = value;
    this.location = location;
  }

  @Override
  public Value toRuntimeValue() {
    return BoolValue.of(value);
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    MethodVisitor visitor = ctx.getMethodVisitor();
    visitor.visitInsn(value ? ICONST_1 : ICONST_0);
    visitor.visitMethodInsn(INVOKESTATIC, InternalNames.Value.BOOL_VALUE, "of", MethodDescriptors.Value.OF_BOOL, false);
  }
}
