# Compiladores - Trabalho 3, 4 e 5

## Grupo:
Matheus Rezende Milani Videira - 790809

Mauricio Herrera Fontes - 790986

Victor Germano Moreira Batista da Silva -769775

> Esse trabalho possui as implementaç�es dos trabalhos T3, T4 e T5, juntos, por conta disso, existem partes do c�digo dedicadas ao T4 e T5 juntas no programa 

## Requisitos:
Para realizar o trabalho foram utilizados os seguintes programas:
| Nome do programa | Versão | Link para download |
|:-:|:-:|:-:|
| Intellij IDEA | 2024.1 (Ultimate Edition) | https://www.jetbrains.com/pt-br/idea/download/?section=windows |
| Apache Maven | 3.9.6 | https://archive.apache.org/dist/maven/maven-3/3.9.6/ |
| OpenJDK | 22.0.1 | https://jdk.java.net/22/ |
| antlr4 | 4.10.1 | Instalado via Maven |

## Para compilar o projeto:
Execute os comandos abaixo no terminal de comandos:
```
git clone https://github.com/MatheusRMVideira/compiladores-trabalho-1.git
cd compiladores-trabalho-1.git
mvn install
mvn compile assembly:single
```

## Para executar:
Para executar de maneira independente:
```
java -jar ./target/trabalho1-1.0-SNAPSHOT-jar-with-dependencies.jar {ARQUIVO DE ENTRADA} {ARQUIVO DE SAÍDA}
```
>[!IMPORTANT]
>Alterar __{ARQUIVO DE ENTRADA}__ e __{ARQUIVO DE SAÍDA}__ para os arquivos desejados
