package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.constants.ClosureNames;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import java.util.List;
import java.util.ListIterator;

public class CallTerm extends Term {
  public Term callee;
  public List<Term> arguments;
  public Loc location;

  @JsonCreator
  public CallTerm(Term callee, List<Term> arguments, Loc location) {
    this.callee = callee;
    this.arguments = arguments;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
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
