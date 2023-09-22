package com.guimeira.rinha_compilers.compiler.ast;

import com.guimeira.rinha_compilers.rt.value.Value;

public interface ProcessableInCompilationTime {
  Value toRuntimeValue();
}
