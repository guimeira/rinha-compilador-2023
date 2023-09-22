package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.exception.CompilationException;
import com.guimeira.rinha_compilers.compiler.preprocessing.CapturedVariable;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import com.guimeira.rinha_compilers.compiler.preprocessing.Variable;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class VarTerm extends Term {
  //Valores carregados do JSON da AST:
  public String text;
  public Loc location;

  //Valores computados pelo preprocessamento:
  public Variable variable;

  @JsonCreator
  public VarTerm(String text, Loc location) {
    this.text = text;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    Variable v = ctx.locateVariable(text);
    if(v == null) {
      throw new CompilationException("Variável " + text + " não definida", location);
    }

    variable = v;
    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    if(variable instanceof CapturedVariable cv) {
      //Se a variável foi capturada do contexto externo, ela é um atributo da classe:
      ctx.pushCapturedVariableValue(cv.id);
    } else {
      //Se a variável foi definida neste escopo ela é uma variável local:
      MethodVisitor visitor = ctx.getMethodVisitor();
      visitor.visitVarInsn(ALOAD, variable.id);
      visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VARIABLE, "getValue", MethodDescriptors.Variable.GET_VALUE, false);
    }
  }
}
