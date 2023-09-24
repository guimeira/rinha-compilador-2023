package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.ClosureNames;
import com.guimeira.rinha_compilers.compiler.preprocessing.CapturedVariable;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import com.guimeira.rinha_compilers.compiler.preprocessing.Variable;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import java.util.List;

public class FunctionTerm extends Term {
  //Valores carregados do JSON da AST:
  public List<Parameter> parameters;
  public Term value;
  public Loc location;

  //Valores computados pelo preprocessamento:
  //Variáveis do contexto pai capturadas por esta clojure:
  public List<CapturedVariable> capturedVariables;

  //Variáveis definidas dentro desta função (parâmetros + variáveis definidas por let):
  public List<Variable> variables;

  @JsonCreator
  public FunctionTerm(List<Parameter> parameters, Term value, Loc location) {
    this.parameters = parameters;
    this.value = value;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    //Criar novo escopo:
    ctx.pushScope();

    //Marcar escopo como closure (se acessarmos variáveis fora deste escopo, elas serão capturadas):
    ctx.markScopeAsClosure(parameters.size());

    //Alocar variáveis para os parâmetros da função:
    parameters.forEach(p -> ctx.addVariable(p.text));

    //Adicionar a aridade desta função:
    ctx.addFunctionArity(parameters.size());

    //Rodar o preprocessamento no corpo da função:
    value = value.preprocess(ctx);

    //Armazenar essas informações na árvore:
    capturedVariables = ctx.getCapturedVariablesInScope();
    variables = ctx.getVariablesInScope();

    //Remover o contexto que criamos para esta função:
    ctx.popScope();
    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    //Gerar código desta função (será colocado em uma classe separada):
    String className = ctx.startFunction(capturedVariables.size(), parameters.size());
    value.codeGen(ctx);
    ctx.endFunction();

    //Na classe que estamos compilando no momento, criaremos uma instância da classe que acabamos de gerar
    //Os parâmetros do construtor dessa classe são as variáveis capturadas do escopo atual:
    MethodVisitor visitor = ctx.getMethodVisitor();
    visitor.visitTypeInsn(NEW, className);
    visitor.visitInsn(DUP);

    for(CapturedVariable cap : capturedVariables) {
      //Aqui estamos olhando para o escopo atual. Se essa variável que a função captura também foi capturada pelo
      //contexto atual, ela é um atributo da classe. Caso contrário, ela é uma variável local:
      if(cap.variable instanceof CapturedVariable cv) {
        ctx.pushCapturedVariable(cv.id);
      } else {
        visitor.visitVarInsn(ALOAD, cap.variable.id);
      }
    }
    visitor.visitMethodInsn(INVOKESPECIAL, className, CodegenContext.CONSTRUCTOR_INTERNAL_NAME, ClosureNames.getConstructorDescriptor(capturedVariables.size()), false);
  }
}
