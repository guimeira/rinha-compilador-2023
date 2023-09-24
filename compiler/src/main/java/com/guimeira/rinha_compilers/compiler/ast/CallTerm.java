package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.constants.ClosureNames;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import java.util.List;
import java.util.ListIterator;

public class CallTerm extends Term {
  public Term callee;
  public List<Term> arguments;
  public Loc location;
  public boolean isTailCall;

  @JsonCreator
  public CallTerm(Term callee, List<Term> arguments, Loc location) {
    this.callee = callee;
    this.arguments = arguments;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    isTailCall = ctx.isTailCall() && arguments.size() == ctx.getCurrentFunctionArity();
    callee = callee.preprocess(ctx);

    ListIterator<Term> it = arguments.listIterator();
    while(it.hasNext()) {
      Term term = it.next();
      term = term.preprocess(ctx);
      it.set(term);
    }

    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    //Ao final deste código, o ClosureValue a ser chamado deve estar no topo da pilha:
    callee.codeGen(ctx);

    if(isTailCall) {
      codeGenTailCall(ctx);
    } else {
      codeGenRegularMethodCall(ctx);
    }
  }

  private void codeGenTailCall(CodegenContext ctx) {
    MethodVisitor visitor = ctx.getMethodVisitor();

    //Duplicar a referência à closure que será chamada:
    visitor.visitInsn(DUP);

    //Colocar this na pilha:
    visitor.visitVarInsn(ALOAD, 0);

    //Se a closure a ser chamada é esta função e já sabemos que estamos numa tail call, não faremos invoke.
    //Em vez disso, atualizaremos as variables com os parâmetros da nova chamada de função e faremos um jump para o início da função
    Label lblRegularFunctionCall = new Label();
    visitor.visitJumpInsn(IF_ACMPNE, lblRegularFunctionCall);

    //Descartar a referência à closure que a ser chamada que não será necessária no caso de tail call:
    visitor.visitInsn(POP);

    //Calcular o novo valor de cada argumento e deixar na pilha:
    //Calculamos cada argumento antes de chamar setValue pra garantir que os argumentos não serão calculados com base nos novos valores:
    int arity = arguments.size();
    for(int i = 0; i < arity; i++) {
      arguments.get(i).codeGen(ctx);
    }

    //Agora que calculamos o valor de cada argumento, vamos atualizar as Variables. Armazenamos na ordem inversa devido
    //à posição dos argumentos na pilha:
    for(int i = arguments.size() -1; i >= 0; i--) {
      //Colocar a Variable que armazena este argumento na pilha:
      visitor.visitVarInsn(ALOAD, i+1);

      visitor.visitInsn(SWAP);

      //Guardar este valor na Variable:
      visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VARIABLE, "setValue", MethodDescriptors.Variable.SET_VALUE, false);
    }

    //Voltar para o início da função:
    visitor.visitJumpInsn(GOTO, ctx.getTopLabel());

    //Pularemos para este ponto se formos fazer uma chamada de método comum:
    visitor.visitLabel(lblRegularFunctionCall);
    codeGenRegularMethodCall(ctx);
  }

  private void codeGenRegularMethodCall(CodegenContext ctx) {
    //Colocar na pilha cada um dos parâmetros que esta função espera:
    for(Term arg : arguments) {
      arg.codeGen(ctx);
    }

    //Chamar função:
    MethodVisitor visitor = ctx.getMethodVisitor();
    int arity = arguments.size();
    visitor.visitMethodInsn(INVOKEINTERFACE, ClosureNames.getFullInterfaceName(arity), ClosureNames.INTERFACE_METHOD, ClosureNames.getInterfaceMethodDescriptor(arity), true);
  }
}
