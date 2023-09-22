# Rinha de Compilador
Meu código da Rinha de Backend tirou zero e eu sou desenvolvedor backend. Agora vou participar da Rinha de Compilador
sem saber nada de compiladores. Eu sinto que vou reviver um momento da faculdade quando um professor inventou um
esquema de pontuação que deixou metade da turma com nota negativa.

𝕏 [@gtmeira](https://twitter.com/gtmeira)

## Execução com Docker
Construir a imagem do Docker:
```bash
docker build -t <tag> .
```

Compilar e executar o arquivo no caminho padrão (`/var/rinha/source.rinha.json`):
```bash
docker run --rm -v ./arquivo.json:/var/rinha/source.rinha.json <tag>
```

Compilar e executar outros arquivos:
```bash
docker run --rm -v ./:/volume <tag> --write --run /volume/arquivo.json
```

### Parâmetros
* `--write`: escreve as classes Java na pasta `target` no mesmo diretório do código fonte
* `--run`: executa o programa após o fim da compilação

## Execução sem Docker
Construir o projeto:
```bash
mvn clean package
```
O comando acima gerará o arquivo `runtime/target/runtime-1.0-SNAPSHOT.jar` que deve estar presente
no classpath durante a execução do programa, bem como o arquivo executável `compiler/target/compiler-1.0-SNAPSHOT.jar`
contendo o compilador.

Para compilar um programa:
```bash
java -cp runtime/target/runtime-1.0-SNAPSHOT.jar -jar compiler/target/compiler-1.0-SNAPSHOT.jar --write --run ./arquivo.json
```
O comando acima salva as classes Java (`--write`) na pasta `target` e também executa o programa (`--run`).

Para executar o programa de forma standalone, sem depender do compilador:
```bash
java -cp target:runtime/target/runtime-1.0-SNAPSHOT.jar com.guimeira.rinha_compilers.rt.gen.EntryPoint
```

## Sobre
Este compilador transforma o JSON da AST da linguagem Rinha em bytecode da Java Virtual Machine (JVM).

O projeto é dividido em dois submódulos.

### Módulo `runtime`
O módulo `runtime` contém classes que devem estar presentes em tempo de execução.
A classe abstrata `Value` representa um valor e existe uma subclasse concreta para cada tipo existente na 
linguagem: `IntValue`, `BoolValue`, `StrValue`, `TupleValue` e `ClosureValue`. A classe `Value` define métodos
que correspondem a cada um dos operadores da linguagem e o processo de compilação converte esses operadores em
chamadas de métodos da classe `Value`. Por exemplo, o programa:

```
let a = 1;
let b = 2;
let c = 3;
let d = a + b * c;
print(d)
```

é convertido em bytecode da JVM que corresponde aproximadamente ao código Java a seguir:
```java
Variable a = new Variable();
a.setValue(IntValue.of(1));
Variable b = new Variable();
b.setValue(IntValue.of(2));
Variable c = new Variable();
c.setValue(IntValue.of(3));
Variable d = new Variable();
d.setValue(a.getValue().add(b.getValue().mul(c.getValue())));
System.out.println(d.getValue().toStringRepresentation());
```

### Módulo `compiler`
O módulo `compiler` é responsável pelo processo de compilação. Uma vez realizada a compilação,
o código resultante não depende deste módulo para executar.

A leitura do JSON é feita com a biblioteca [Jackson](https://github.com/FasterXML/jackson) que é o 
"de facto standard" para manipulação de JSON no ecossistema Java.

A geração de bytecode é feita por meio da biblioteca [ASM](https://asm.ow2.io/), também amplamente
utilizada no ecossistema Java, inclusive dentro do próprio JDK.

### Processo de compilação
O ✌design✌ deste compilador foi feito de forma completamente freestyle, sem consulta
à literatura existente. Considerando que estamos caminhando para quase um século de pesquisa e desenvolvimento de compiladores,
a chance de eu ter feito qualquer coisa remotamente parecida com as melhores práticas é próxima de zero.

Após carregar a AST do arquivo JSON, o compilador realiza uma etapa de **pré-processamento**. O objetivo
principal dessa etapa é determinar quais variáveis cada função vai capturar do escopo pai. Essa etapa
é importante em casos em que uma função captura valores vários níveis acima do escopo pai. Por exemplo:

```
let a = "A";
let f1 = fn(x) => {
  let bx = "B" + x;
  fn(x) => {
    (a + bx) * x
  }
};
let f1Z = f1("Z");
print(f1Z(2))
```
No código acima, a função mais externa (atribuíta a `f1`) não usa `a` explicitamente, mas a função mais interna faz.
Assim, a função externa precisa capturar `a` para que a função interna possa fazer uso desse valor.

O processo de pré-processamento também realiza uma otimização bem simples: expressões que não envolvam variáveis são
calculadas durante a compilação. Por exemplo:
```
let a = 1 + 2 * 3;
let b = 2 * 2 + a;
print(b)
```

é convertido em bytecode aproximadamente equivalente ao código Java:
```java
Variable a = new Variable();
a.setValue(IntValue.of(7)); //primeira expressão é resolvida completamente
Variable b = new Variable();
b.setValue(IntValue.of(4).add(a.getValue())); //segunda expressão é resolvida parcialmente
System.out.println(b.getValue().toStringRepresentation());
```

Terminado o pré-processamento, começa o processo de geração de código.

Cada função definida no programa original é convertida para uma classe Java. O código principal
do arquivo é considerado uma função sem parâmetros.

As classes são nomeadas `Closure[x]` onde `[x]` é um índice. O código principal fica na classe `Closure1`,
a primeira função definida no arquivo fica na classe `Closure2` e assim sucessivamente.

Cada variável capturada de um contexto externo é convertida para um atributo dessa classe e é gerado
um construtor que recebe cada um desses atributos (instâncias de `Value`). O construtor envolve cada um desses
valores em uma instância de `Variable` e salva no atributo da classe.

O único outro método de cada uma dessas classes é o método `call`. Chamadas de função são convertidas em
chamadas do método `call` dessa classe. Cada parâmetro da função no programa original é convertida em
um parâmetro do método `call` (instâncias de `Value`).

Não é possível usar a instrução `invokevirtual` para implementar a chamada ao método `call` visto que não sabemos
durante a compilação qual classe estamos invocando. Uma variável pode conter uma referência a qualquer uma
das funções definidas no programa e cada uma corresponde a uma classe diferente.

Para contornar esse problema, o compilador gera uma interface `F[x]` para cada aridade de função existente
no programa, onde `[x]` é a aridade. Por exemplo, se o programa possui funções com 2 e 3 parâmetros,
serão geradas as interfaces `F2` e `F3` (além de `F0` para o programa principal). Cada uma dessas interfaces
define o método `call` com o número apropriado de parâmetros. O compilador, então, faz com que as classes
`Closure[x]` implementem uma dessas interfaces.

Dessa forma, uma chamada de função é implementada por meio da instrução `invokeinterface`. Como sabemos em
tempo de compilação a aridade da função sendo chamada, fica fácil determinar qual interface devemos usar.

Por exemplo:
```
let a = 10;
let f1 = fn(x) => {
  x + a
};
print(f1(20))
```
será convertido em bytecode aproximadamente equivalente ao código Java:
```java
//Código principal:
public class Closure1 extends ClosureValue implements F0 { //implementa F0 pois não tem parâmetros
  public Closure1() {}

  @Override
  public Value call() {
    Variable a = new Variable();
    a.setValue(IntValue.of(10));
    Variable f1 = new Variable();
    f1.setValue(new Closure2(a)); //constroi a closure passando a variável capturada como parâmetro
    Value y = f1.getValue().call(IntValue.of(20)); //chama a função passando um parâmetro
    System.out.println(y.toStringRepresentation());
    return y;
  }
}

//Função atribuída a f1:
public class Closure2 extends ClosureValue implements F1 { //implementa F1 pois espera um parâmetro
  //Variável capturada do contexto pai:
  private Variable a;

  //Variável capturada é passada no construtor:
  public Closure2(Variable a) {
    this.a = a;
  }

  //Função call espera um parâmetro:
  @Override
  public Value call(Value x) {
    Variable y = new Variable();
    y.setValue(x);
    return y.getValue().add(this.a.getValue());
  }
}

```

Uma vez que as classes são geradas em memória, é possível realizar duas operações:
* O comando `--write` escreve as classes em arquivos `.class` para que possam ser executadas posteriormente. Para
executar essas classes não é necessário o compilador, basta que as classes do módulo `runtime` estejam presentes
no classpath.
* O comando `--run` roda o código dentro do mesmo processo que executou a compilação. O classloader `RinhaClassloader`
carrega as classes que geramos e depois usamos reflection para invocar o método `Closure1.call()`.

### Melhorias
Difícil é encontrar alguma coisa que não possa ser melhorada nesse projeto. Em termos otimizações da compilação em si,
eu gostaria de tentar alguma implementação de tail-call optimization. Também tenho quase certeza que é possível evitar
várias chamadas de método e substituí-las por operações em tipos primitivos.

No projeto em si, eu gostaria de refatorar todo o processo. Hoje, as classes da AST também possuem métodos pra preprocessamento
e geração de código. Seria bom se essas coisas ficassem separadas. E idealmente as classes que representam a AST devem
ser imutáveis.

Por fim, testes mais decentes também seriam muito bem vindos.

## Obrigado!
Muito obrigado às organizadoras da Rinha de Compiladores. 🙌

Fazia tempo que eu não tinha disposição pra, após um dia de trabalho, voltar pro computador e programar noite adentro
sem ver a hora passar.