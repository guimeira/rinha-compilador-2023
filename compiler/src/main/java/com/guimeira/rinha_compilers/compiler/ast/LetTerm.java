package com.guimeira.rinha_compilers.compiler.ast;

import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import com.guimeira.rinha_compilers.compiler.preprocessing.Variable;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class LetTerm extends Term {
  //Parâmetros carregados do JSON:
  public Parameter name;
  public Term value;
  public Term next;
  public Loc location;

  //Parâmetros computados durante o preprocessamento:
  public Variable variable;

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    //Roubadinha: adicionar a variável ao contexto antes de processarmos o rhs (isso permite capturarmos as referências
    //corretas em funções recursivas)
    variable = ctx.addVariable(name.text);

    //Criar novo escopo:
    ctx.pushScope();

    //Não pode haver uma tail call aqui porque sabemos que há o "next" logo em seguida:
    ctx.markAsNotTailCall();

    //Processar o rhs:
    value = value.preprocess(ctx);

    //Remover o escopo atual:
    ctx.popScope();

    //Processar os próximos terms:
    ctx.markAsTailCall();
    next = next.preprocess(ctx);

    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    MethodVisitor visitor = ctx.getMethodVisitor();

    //Criar nova instância de Variable:
    visitor.visitTypeInsn(NEW, InternalNames.VARIABLE);
    visitor.visitInsn(DUP);
    visitor.visitMethodInsn(INVOKESPECIAL, InternalNames.VARIABLE, CodegenContext.CONSTRUCTOR_INTERNAL_NAME, MethodDescriptors.DEFAULT_CONSTRUCTOR, false);

    //Guardar a nova instância de Variable em uma variável local:
    visitor.visitInsn(DUP);
    visitor.visitVarInsn(ASTORE, variable.id);

    //Gerar código para o termo que será armazenado nesta variável. Ao final da execução deste código, esperamos que
    //o valor a ser atribuído à variável esteja no topo da pilha:
    value.codeGen(ctx);

    //Guardar o resultado dentro da variável:
    visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VARIABLE, "setValue", MethodDescriptors.Variable.SET_VALUE, false);

    //Continuar compilação dos próximos termos:
    next.codeGen(ctx);
  }
}
