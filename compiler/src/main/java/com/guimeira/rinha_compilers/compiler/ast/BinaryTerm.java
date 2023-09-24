package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.codegen.constants.InternalNames;
import com.guimeira.rinha_compilers.compiler.codegen.constants.MethodDescriptors;
import com.guimeira.rinha_compilers.compiler.codegen.constants.Types;
import com.guimeira.rinha_compilers.compiler.exception.CompilationException;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import com.guimeira.rinha_compilers.rt.value.Value;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class BinaryTerm extends Term {
  public Term lhs;
  public BinaryOp op;
  public Term rhs;
  public Loc location;

  @JsonCreator
  public BinaryTerm(Term lhs, BinaryOp op, Term rhs, Loc location) {
    this.lhs = lhs;
    this.op = op;
    this.rhs = rhs;
    this.location = location;
  }

  @Override
  public Term preprocess(PreprocessingContext ctx) {
    //Qualquer chamada de função no LHS ou RHS não pode ser uma tail call porque após a chamada de função precisaremos
    //executar esta operação binária:
    ctx.markAsNotTailCall();

    //Este preprocessamento é uma pequena otimização: operações que não envolvam variáveis serão computadas aqui
    //Exemplo: "x = 2 + a" precisa ser compilado para "x = 2 + a" porque não sabemos o conteúdo de a
    //mas "x = 2 + 2" pode ser compilado para "x = 4" e evitamos fazer essa operação em tempo de execução

    lhs = lhs.preprocess(ctx);
    rhs = rhs.preprocess(ctx);

    if(lhs instanceof ProcessableInCompilationTime plhs && rhs instanceof ProcessableInCompilationTime prhs) {
      //Converter os termos em instâncias de Value (usadas em runtime). Isso evita de termos que implementar
      //todas as operações duas vezes (uma nas classes de runtime e outra nas classes de compilação)
      Value runtimeLhs = plhs.toRuntimeValue();
      Value runtimeRhs = prhs.toRuntimeValue();

      Value result = null;
      try {
        switch (op) {
          case ADD:
            result = runtimeLhs.add(runtimeRhs);
            break;

          case SUB:
            result = runtimeLhs.sub(runtimeRhs);
            break;

          case MUL:
            result = runtimeLhs.mul(runtimeRhs);
            break;

          case DIV:
            result = runtimeLhs.div(runtimeRhs);
            break;

          case REM:
            result = runtimeLhs.rem(runtimeRhs);
            break;

          case EQ:
            result = runtimeLhs.eq(runtimeRhs);
            break;

          case NEQ:
            result = runtimeLhs.neq(runtimeRhs);
            break;

          case LT:
            result = runtimeLhs.lt(runtimeRhs);
            break;

          case GT:
            result = runtimeLhs.gt(runtimeRhs);
            break;

          case LTE:
            result = runtimeLhs.lte(runtimeRhs);
            break;

          case GTE:
            result = runtimeLhs.gte(runtimeRhs);
            break;

          case AND:
            result = runtimeLhs.and(runtimeRhs);
            break;

          case OR:
            result = runtimeLhs.or(runtimeRhs);
            break;

          default:
            throw new CompilationException("Operação inválida: " + op, location);
        }
      } catch(Exception e) {
        throw new CompilationException("Operação inválida: " + op, location);
      }

      return Term.fromRuntimeValue(result, location);
    }

    return this;
  }

  @Override
  public void codeGen(CodegenContext ctx) {
    MethodVisitor visitor = ctx.getMethodVisitor();
    //Processar o LHS. Ao final desse código, o resultado do LHS deve estar no topo da pilha:
    lhs.codeGen(ctx);

    //Processar o RHS. Ao final, devemos ter o LHS e o RHS no topo da pilha:
    rhs.codeGen(ctx);

    //Chamar o método correspondente a esta operação:
    switch(op) {
      case ADD:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "add", MethodDescriptors.Value.BINARY_OPERATION, false);
        break;

      case SUB:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "sub", MethodDescriptors.Value.BINARY_OPERATION, false);
        break;

      case MUL:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "mul", MethodDescriptors.Value.BINARY_OPERATION, false);
        break;

      case DIV:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "div", MethodDescriptors.Value.BINARY_OPERATION, false);
        break;

      case REM:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "rem", MethodDescriptors.Value.BINARY_OPERATION, false);
        break;

      case EQ:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "eq", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      case NEQ:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "neq", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      case LT:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "lt", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      case GT:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "gt", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      case LTE:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "lte", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      case GTE:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "gte", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      case AND:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "and", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      case OR:
        visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "or", MethodDescriptors.Value.BOOLEAN_BINARY_OPERATION, false);
        break;

      default:
        throw new CompilationException("Operação desconhecida: " + op, location);
    }
  }
}
