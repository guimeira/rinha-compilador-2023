package com.guimeira.rinha_compilers.compiler.preprocessing;

import java.util.*;

public class PreprocessingContext {
  private Scope currentScope = null;

  //Aridades de função existentes no código sendo processado:
  private Set<Integer> functionArities = new HashSet<>(List.of(0));

  public void pushScope() {
    currentScope = new Scope(currentScope);
  }

  public Variable addVariable(String name) {
    return currentScope.addVariable(name);
  }

  /**
   * Marca o escopo atual como uma closure. Referências a variáveis de escopos pai serão capturadas.
   */
  public void markScopeAsClosure(int arity) {
    currentScope.markAsClosureScope(arity);
  }

  public void markAsTailCall() {
    Scope scope = findNearestClosureScope();

    if(scope != null) {
      scope.markAsTailCall();
    }
  }

  public void markAsNotTailCall() {
    Scope scope = findNearestClosureScope();

    if(scope != null) {
      scope.markAsNotTailCall();
    }
  }

  public boolean isTailCall() {
    Scope scope = findNearestClosureScope();

    return scope != null && scope.isTailCall;
  }

  public int getCurrentFunctionArity() {
    Scope scope = findNearestClosureScope();

    if(scope != null) {
      return scope.arity;
    }

    return -1;
  }

  private Scope findNearestClosureScope() {
    Scope scope = currentScope;
    while(scope != null && !scope.isClosure) {
      scope = scope.parent;
    }
    return scope;
  }

  public void addFunctionArity(int arity) {
    functionArities.add(arity);
  }

  public List<CapturedVariable> getCapturedVariablesInScope() {
    return currentScope.capturedVariables;
  }

  public List<Variable> getVariablesInScope() {
    return currentScope.variables;
  }

  public Variable locateVariable(String name) {
    return locateVariable(currentScope, name);
  }

  private Variable locateVariable(Scope scope, String name) {
    if(scope == null) {
      return null;
    }

    //Primeiro, procurar nas variáveis definidas neste escopo
    //Aqui, percorremos a lista de trás para frente. Shadowing não será testado, mas se fosse, isso aqui seria útil
    //pra garantir que no caso de múltiplas variáveis com o mesmo nome, nós sempre nos referíssemos à mais recente.
    ListIterator<Variable> it = scope.variables.listIterator(scope.variables.size());
    while(it.hasPrevious()) {
      Variable variable = it.previous();
      if(variable.name.equals(name)) {
        return variable;
      }
    }

    //Variável não foi encontrada nas variáveis definidas neste contexto. Vamos verificar se ela já foi capturada
    //por este contexto:
    for(CapturedVariable cv : scope.capturedVariables) {
      if(cv.variable.name.equals(name)) {
        return cv;
      }
    }

    //Variável não foi definida neste contexto nem foi capturada do contexto pai. Vamos continuar a busca no contexto pai:
    Variable parentVar = locateVariable(scope.parent, name);

    if(parentVar == null) {
      return null;
    }

    //Variável foi encontrada no contexto pai
    //Se este contexto é um contexto de closure, precisamos capturar essa variável:
    if(scope.isClosure) {
      return scope.addCapturedVariable(parentVar);
    }

    //Caso contrário, podemos acessar a variável diretamente:
    return parentVar;
  }

  public void popScope() {
    currentScope = currentScope.parent;
  }

  public Set<Integer> getFunctionArities() {
    return functionArities;
  }

  //Armazena as variáveis do escopo atual:
  private static class Scope {
    //Escopo pai deste escopo:
    private Scope parent;

    //Se este escopo é uma closure (referências a escopos pais serão capturadas):
    private boolean isClosure;

    //Se o term que estamos avaliando no momento está numa posição de tail, isto é, nenhuma outra operação será realizada pela função neste ramo da AST (usado somente se isClosure = true)
    private boolean isTailCall = true;

    //Aridade da função que estamos avaliando no momento (usado somente se isClosure = true)
    private int arity;

    //Variáveis do contexto pai referenciadas por este escopo:
    private List<CapturedVariable> capturedVariables = new ArrayList<>();

    //Contém as variáveis criadas no escopo atual (variáveis definidas por let e parâmetros da função):
    private List<Variable> variables = new ArrayList<>();

    public Scope(Scope parent) {
      this.parent = parent;
    }

    public void markAsClosureScope(int arity) {
      isClosure = true;
      this.arity = arity;
    }

    public Variable addVariable(String name) {
      Variable v = new Variable(getNextVariableIndex(), name);
      variables.add(v);
      return v;
    }

    public int getNextVariableIndex() {
      if(!variables.isEmpty()) {
        return variables.get(variables.size()-1).id + 1;
      }

      if(parent != null && !isClosure) {
        return parent.getNextVariableIndex();
      }

      return 1;
    }

    public CapturedVariable addCapturedVariable(Variable variable) {
      CapturedVariable cv = new CapturedVariable(getNextCapturedVariableIndex(), variable);
      capturedVariables.add(cv);
      return cv;
    }

    public int getNextCapturedVariableIndex() {
      if(capturedVariables.isEmpty()) {
        return 1;
      }

      return capturedVariables.get(capturedVariables.size()-1).id + 1;
    }

    public void markAsTailCall() {
      isTailCall = true;
    }

    public void markAsNotTailCall() {
      isTailCall = false;
    }
  }
}
