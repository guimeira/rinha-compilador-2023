package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class IfTerm extends Term {
  public Term condition;
  public Term then;
  public Term otherwise;
  public Loc location;

  @JsonCreator
  public IfTerm(Term condition, Term then, Term otherwise, Loc location) {
    this.condition = condition;
    this.then = then;
    this.otherwise = otherwise;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    boolean currentlyIsTailCall = ctx.isTailCall();

    //Chamada de função dentro da condição do if não pode ser uma tail call:
    ctx.markAsNotTailCall();

    condition = condition.preprocess(ctx);

    if(currentlyIsTailCall) {
      ctx.markAsTailCall();
    }

    //Se por acaso a condição puder ser avaliada para um booleano em tempo de compilação, eliminamos o if:
    if(condition instanceof BoolTerm bt) {
      if(bt.value) {
        return then.preprocess(ctx);
      }
      return otherwise.preprocess(ctx);
    }

    then = then.preprocess(ctx);
    otherwise = otherwise.preprocess(ctx);
    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    //Se tivermos removido o if em tempo de compilação:
    if(condition instanceof BoolTerm bt) {
      if(bt.value) {
        then.codeGen(ctx);
        return;
      }
      otherwise.codeGen(ctx);
      return;
    }

    MethodVisitor visitor = ctx.getMethodVisitor();
    Label lblOtherwise = new Label();
    Label lblEnd = new Label();

    //Ao final, o BoolValue que é a condição do if deve estar no topo da pilha:
    condition.codeGen(ctx);

    //Agora o topo da pilha contém um booleano (0 ou 1):
    visitor.visitTypeInsn(CHECKCAST, InternalNames.Value.BOOL_VALUE);
    visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.Value.BOOL_VALUE, "value", MethodDescriptors.BoolValue.VALUE, false);

    //Se for == 0, a condição é falsa e pulamos para o else:
    visitor.visitJumpInsn(IFEQ, lblOtherwise);

    //Agora vamos gerar o código para o then:
    then.codeGen(ctx);

    //Ao final do código do then, saltamos para o final do if:
    visitor.visitJumpInsn(GOTO, lblEnd);

    //Colocar a label do otherwise e gerar o código dele:
    visitor.visitLabel(lblOtherwise);
    otherwise.codeGen(ctx);

    //A label ao final do código do otherwise:
    visitor.visitLabel(lblEnd);
  }
}
