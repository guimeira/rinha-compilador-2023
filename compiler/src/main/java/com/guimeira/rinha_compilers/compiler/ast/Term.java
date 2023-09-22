package com.guimeira.rinha_compilers.compiler.ast;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.guimeira.rinha_compilers.compiler.codegen.CodegenContext;
import com.guimeira.rinha_compilers.compiler.exception.CompilationException;
import com.guimeira.rinha_compilers.compiler.preprocessing.PreprocessingContext;
import com.guimeira.rinha_compilers.rt.value.BoolValue;
import com.guimeira.rinha_compilers.rt.value.IntValue;
import com.guimeira.rinha_compilers.rt.value.StrValue;
import com.guimeira.rinha_compilers.rt.value.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BinaryTerm.class, name = "Binary"),
        @JsonSubTypes.Type(value = BoolTerm.class, name = "Bool"),
        @JsonSubTypes.Type(value = CallTerm.class, name = "Call"),
        @JsonSubTypes.Type(value = FirstTerm.class, name = "First"),
        @JsonSubTypes.Type(value = SecondTerm.class, name = "Second"),
        @JsonSubTypes.Type(value = FunctionTerm.class, name = "Function"),
        @JsonSubTypes.Type(value = IfTerm.class, name = "If"),
        @JsonSubTypes.Type(value = IntTerm.class, name = "Int"),
        @JsonSubTypes.Type(value = LetTerm.class, name = "Let"),
        @JsonSubTypes.Type(value = PrintTerm.class, name = "Print"),
        @JsonSubTypes.Type(value = StrTerm.class, name = "Str"),
        @JsonSubTypes.Type(value = TupleTerm.class, name = "Tuple"),
        @JsonSubTypes.Type(value = VarTerm.class, name = "Var")
})
public abstract class Term {
  public Term preprocess(PreprocessingContext ctx) {
    return this;
  }

  public void codeGen(CodegenContext ctx) {}

  public static Term fromRuntimeValue(Value value, Loc location) {
    if(value instanceof BoolValue bv) {
      return new BoolTerm(bv.value(), location);
    }

    if(value instanceof IntValue iv) {
      return new IntTerm(iv.value(), location);
    }

    if(value instanceof StrValue sv) {
      return new StrTerm(sv.value(), location);
    }

    throw new CompilationException("Instância de Value não pode ser convertida para Term", location);
  }
}
