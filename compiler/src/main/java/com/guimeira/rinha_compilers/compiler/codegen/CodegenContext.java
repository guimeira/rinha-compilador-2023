package com.guimeira.rinha_compilers.compiler.codegen;

import com.guimeira.rinha_compilers.compiler.codegen.constants.*;
import com.guimeira.rinha_compilers.compiler.exception.CompilationException;
import com.guimeira.rinha_compilers.compiler.run.RinhaClassloader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.objectweb.asm.Opcodes.*;

public class CodegenContext {
  private static final String OUTPUT_FOLDER = "target/";
  public static final String PACKAGE_NAME = "com/guimeira/rinha_compilers/rt/gen/";
  private static final String CLASS_EXTENSION = ".class";
  private static final String ENTRY_POINT_CLASS = "EntryPoint";
  private static final String ENTRY_POINT_METHOD = "main";
  public static final String CONSTRUCTOR_INTERNAL_NAME = "<init>";

  private final Path workDir;
  private final boolean writeClasses;
  private final boolean runCode;
  private int classNameCounter = 1;
  private Ctx currentCtx;
  private RinhaClassloader classloader;

  public CodegenContext(Path workDir, boolean writeClasses, boolean runCode) {
    this.workDir = workDir;
    this.writeClasses = writeClasses;
    this.runCode = runCode;

    if(runCode) {
      classloader = new RinhaClassloader();
    }
  }

  /**
   * Inicia a geração de código para uma nova função.
   */
  public String startFunction(int capturedVariables, int localVariables, int arity) {
    //Criar classe que conterá esta função:
    String className = ClosureNames.getFullClassInternalName(classNameCounter++);
    ClassWriter writer = new RinhaClassWriter(ClassWriter.COMPUTE_FRAMES);
    writer.visit(V17, ACC_PUBLIC, className, null, InternalNames.Value.CLOSURE_VALUE, new String[]{ClosureNames.getFullInterfaceName(arity)});

    //Criar um atributo para cada variável que será capturada pela closure:
    for (int i = 1; i <= capturedVariables; i++) {
      writer.visitField(ACC_PRIVATE, ClosureNames.getCapturedFieldName(i), TypeDescriptors.VARIABLE, null, null).visitEnd();
    }

    //Criar um construtor que recebe todas as variáveis capturadas pela closure:
    MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, CONSTRUCTOR_INTERNAL_NAME, ClosureNames.getConstructorDescriptor(capturedVariables), null, null);

    //Chamada ao construtor da superclasse:
    constructorVisitor.visitVarInsn(ALOAD, 0);
    constructorVisitor.visitMethodInsn(INVOKESPECIAL, InternalNames.Value.CLOSURE_VALUE, CONSTRUCTOR_INTERNAL_NAME, MethodDescriptors.DEFAULT_CONSTRUCTOR, false);

    //Armazenar cada parâmetro do construtor em um dos atributos que criamos acima:
    for (int i = 1; i <= capturedVariables; i++) {
      constructorVisitor.visitVarInsn(ALOAD, 0);
      constructorVisitor.visitVarInsn(ALOAD, i);
      constructorVisitor.visitFieldInsn(PUTFIELD, className, ClosureNames.getCapturedFieldName(i), TypeDescriptors.VARIABLE);
    }
    constructorVisitor.visitInsn(RETURN);
    constructorVisitor.visitMaxs(0, 0);
    constructorVisitor.visitEnd();

    //Criar método que conterá o código da função:
    MethodVisitor methodVisitor = writer.visitMethod(ACC_PUBLIC, ClosureNames.INTERFACE_METHOD, ClosureNames.getInterfaceMethodDescriptor(arity), null, null);

    methodVisitor.visitAnnotation(Types.OVERRIDE.getDescriptor(), true).visitEnd();

    //Verificar se temos um resultado memoizado para esta chamada:
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitFieldInsn(GETFIELD, InternalNames.Value.CLOSURE_VALUE, "memoizedInvocations", TypeDescriptors.LINKED_HASH_MAP);
    methodVisitor.visitTypeInsn(NEW, InternalNames.ARRAY_LIST);
    methodVisitor.visitInsn(DUP);
    methodVisitor.visitLdcInsn(arity);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, InternalNames.ARRAY_LIST, "<init>", MethodDescriptors.ArrayList.CAPACITY_CONSTRUCTOR, false);
    for(int i = 1; i <= arity; i++) {
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitVarInsn(ALOAD, i);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.ARRAY_LIST, "add", MethodDescriptors.ArrayList.ADD, false);
      methodVisitor.visitInsn(POP);
    }
    methodVisitor.visitInsn(DUP);
    methodVisitor.visitVarInsn(ASTORE, localVariables + LocalVars.MEMOIZATION_KEY);
    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.LINKED_HASH_MAP, "get", MethodDescriptors.LinkedHashMap.GET, false);
    methodVisitor.visitInsn(DUP);
    Label afterMemoizationCheck = new Label();
    methodVisitor.visitJumpInsn(IFNULL, afterMemoizationCheck);
    methodVisitor.visitTypeInsn(CHECKCAST, InternalNames.VALUE);
    methodVisitor.visitInsn(ARETURN);

    methodVisitor.visitLabel(afterMemoizationCheck);
    methodVisitor.visitInsn(POP);
    //Criar uma instância de Variable para cada parâmetro:
    for(int i = 1; i <= arity; i++) {
      methodVisitor.visitTypeInsn(NEW, InternalNames.VARIABLE);
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, InternalNames.VARIABLE, CONSTRUCTOR_INTERNAL_NAME, MethodDescriptors.DEFAULT_CONSTRUCTOR, false);
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitVarInsn(ALOAD, i);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VARIABLE, "setValue", MethodDescriptors.Variable.SET_VALUE, false);
      methodVisitor.visitVarInsn(ASTORE, i);
    }

    //Criar uma variável local que armazena se a chamada da função é memoizable:
    methodVisitor.visitLdcInsn(Boolean.TRUE);
    methodVisitor.visitVarInsn(ISTORE, localVariables + LocalVars.IS_MEMOIZABLE);

    //Criar uma label para o início do código da função (poderá ser usado caso haja tail calls recursivas no corpo da função:
    Label lblTop = new Label();
    methodVisitor.visitLabel(lblTop);
    currentCtx = new Ctx(currentCtx, className, writer, lblTop, localVariables, methodVisitor);

    return className;
  }

  public Label getTopLabel() {
    return currentCtx.lblTop;
  }

  /**
   * Gera código para colocar uma variável capturada do contexto pai no topo da pilha.
   */
  public void pushCapturedVariable(int index) {
    currentCtx.visitor.visitVarInsn(ALOAD, 0);
    currentCtx.visitor.visitFieldInsn(GETFIELD, currentCtx.className, ClosureNames.getCapturedFieldName(index), TypeDescriptors.VARIABLE);
  }

  /**
   * Gera código para colocar o valor de uma variável capturada do contexto pai no topo da pilha.
   */
  public void pushCapturedVariableValue(int index) {
    pushCapturedVariable(index);
    currentCtx.visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VARIABLE, "getValue", MethodDescriptors.Variable.GET_VALUE, false);
  }

  public int getCurrentFunctionLocalVariableCount() {
    return currentCtx.localVariables;
  }
  /**
   * Finaliza a geração de uma função. Se writeClasses está habilitado, escreve o arquivo .class. Se runCode está
   * habilitado, adiciona a classe ao classloader.
   */
  public void endFunction() {
    MethodVisitor visitor = currentCtx.visitor;
    visitor.visitInsn(DUP);
    visitor.visitVarInsn(ILOAD, currentCtx.localVariables + LocalVars.IS_MEMOIZABLE);
    visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.VALUE, "setMemoizable", MethodDescriptors.Value.SET_MEMOIZABLE, false);
    Label afterMemoization = new Label();
    visitor.visitVarInsn(ILOAD, currentCtx.localVariables + LocalVars.IS_MEMOIZABLE);
    visitor.visitJumpInsn(IFEQ, afterMemoization);
    visitor.visitInsn(DUP);
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, InternalNames.Value.CLOSURE_VALUE, "memoizedInvocations", TypeDescriptors.LINKED_HASH_MAP);
    visitor.visitInsn(SWAP);
    visitor.visitVarInsn(ALOAD, currentCtx.localVariables + LocalVars.MEMOIZATION_KEY);
    visitor.visitInsn(SWAP);
    visitor.visitMethodInsn(INVOKEVIRTUAL, InternalNames.HASH_MAP, "put", MethodDescriptors.HashMap.PUT, false);
    visitor.visitInsn(POP);

    visitor.visitLabel(afterMemoization);
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
    currentCtx.writer.visitEnd();
    process(currentCtx.className, currentCtx.writer);
    currentCtx = currentCtx.parent;
  }

  /**
   * Obtém o {@link MethodVisitor} da classe sendo gerada no momento.
   */

  public MethodVisitor getMethodVisitor() {
    return currentCtx.visitor;
  }

  /**
   * Gera uma interface a ser implementada pelas nossas closures.
   * O nome de cada interface será F[x] onde [x] é a aridade do método.
   * A interface terá um único método "call" que recebe [x] parâmetros do tipo Value.
   */
  public void generateFunctionInterface(int arity) {
    String interfaceName = ClosureNames.getFullInterfaceName(arity);
    ClassWriter writer = new RinhaClassWriter(ClassWriter.COMPUTE_FRAMES);
    writer.visit(V17, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE, interfaceName, null, "java/lang/Object", null);
    writer.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, ClosureNames.INTERFACE_METHOD, ClosureNames.getInterfaceMethodDescriptor(arity), null, null).visitEnd();
    writer.visitEnd();
    process(interfaceName, writer);
  }

  /**
   * Gera uma classe que será o ponto de entrada do nosso programa (caso ele seja executado de forma standalone).
   * Esta classe conterá um método main que chama o método "call" da classe Closure1.
   */
  public void generateEntryPoint() {
    //Criar classe que conterá esta função:
    String className = PACKAGE_NAME + ENTRY_POINT_CLASS;
    ClassWriter writer = new RinhaClassWriter(ClassWriter.COMPUTE_FRAMES);
    writer.visit(V17, ACC_PUBLIC, className, null, InternalNames.OBJECT, null);

    //Criar um construtor que recebe todas as variáveis capturadas pela closure:
    MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, CONSTRUCTOR_INTERNAL_NAME, MethodDescriptors.DEFAULT_CONSTRUCTOR, null, null);
    constructorVisitor.visitVarInsn(ALOAD, 0);
    constructorVisitor.visitMethodInsn(INVOKESPECIAL, InternalNames.OBJECT, CONSTRUCTOR_INTERNAL_NAME, MethodDescriptors.DEFAULT_CONSTRUCTOR, false);
    constructorVisitor.visitInsn(RETURN);
    constructorVisitor.visitMaxs(0, 0);
    constructorVisitor.visitEnd();

    //Método main:
    MethodVisitor mainVisitor = writer.visitMethod(ACC_PUBLIC + ACC_STATIC, ENTRY_POINT_METHOD, MethodDescriptors.MAIN, null, null);
    mainVisitor.visitTypeInsn(NEW, PACKAGE_NAME + ClosureNames.CLASS_NAME_PREFIX + "1");
    mainVisitor.visitInsn(DUP);
    mainVisitor.visitMethodInsn(INVOKESPECIAL, ClosureNames.getFullClassInternalName(1), CONSTRUCTOR_INTERNAL_NAME, MethodDescriptors.DEFAULT_CONSTRUCTOR, false);
    mainVisitor.visitMethodInsn(INVOKEINTERFACE, ClosureNames.getFullInterfaceName(0), ClosureNames.INTERFACE_METHOD, ClosureNames.getInterfaceMethodDescriptor(0), true);
    mainVisitor.visitInsn(RETURN);
    mainVisitor.visitMaxs(0, 0);
    mainVisitor.visitEnd();
    writer.visitEnd();

    process(className, writer);
  }

  /**
   * Processa uma classe após o fim de sua geração.
   * Se writeClasses está habilitado, escreve o arquivo .class
   * Se runCode está habilitado, adiciona a classe ao classloader.
   */
  private void process(String className, ClassWriter writer) {
    byte[] generatedClass = writer.toByteArray();

    if(writeClasses) {
      write(generatedClass, className);
    }

    if(runCode) {
      classloader.addClass(className, generatedClass);
    }
  }

  /**
   * Escreve uma classe no disco.
   */
  private void write(byte[] generatedClass, String className) {
    try {
      Path path = workDir.resolve(OUTPUT_FOLDER + className + CLASS_EXTENSION);
      Files.createDirectories(path.getParent());
      Files.write(path, generatedClass);
    } catch(Exception e) {
      throw new CompilationException("Erro de IO ao escrever classe " + className, e);
    }
  }

  /**
   * Retorna o classloader capaz de carregar as classes geradas durante a compilação.
   */
  public ClassLoader getClassloader() {
    return classloader;
  }

  /**
   * Contexto da função sendo gerada neste momento.
   */
  private static class Ctx {
    private Ctx parent;
    private String className;
    private ClassWriter writer;
    private Label lblTop;
    private int localVariables;
    private MethodVisitor visitor;

    public Ctx(Ctx parent, String className, ClassWriter writer, Label lblTop, int localVariables, MethodVisitor visitor) {
      this.parent = parent;
      this.className = className;
      this.writer = writer;
      this.lblTop = lblTop;
      this.localVariables = localVariables;
      this.visitor = visitor;
    }
  }
}
