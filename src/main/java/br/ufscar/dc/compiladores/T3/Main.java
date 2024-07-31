package br.ufscar.dc.compiladores.T3;

import br.ufscar.dc.compiladores.T3.AlgumaParser.ProgramaContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;
import java.io.PrintWriter;


public class Main {
    public static void main(String[] args) throws IOException {
        // Verifica o numero de argumentos
         if (args.length < 2) {
            System.out.println("Falha na execuÃ§Ã£o.\nNÃºmero de parÃ¢metros invÃ¡lidos.");
            System.exit(0);
        }

        // Faz a leitura do arquivo e cria o analisador lexico e semantico.
        AlgumaLexer input = new AlgumaLexer(CharStreams.fromFileName(args[0]));
        AlgumaParser parser = new AlgumaParser(new CommonTokenStream(input));


        parser.removeErrorListeners();
        parser.addErrorListener(ErrorHandler.INSTANCE);
        Visitor visitor = new Visitor();
        

        // abre o arquivo de output
        try (PrintWriter output = new PrintWriter(args[1])){

            try{
                // Analise semantica do arquivo
                ProgramaContext c = parser.programa();
                visitor.visitPrograma(c);
                            
                if (visitor.errorlist.getErrors().isEmpty()) {
                    CodeGenerator generator = new CodeGenerator(visitor.getScope());
                    generator.visit(c);
                    output.print(generator.finalOutput.toString());
                }
                // Log de erros
                else{
                    for (String ret : visitor.errorlist.getErrors())
                        output.println(ret);
                    output.println("Fim da compilacao");
                }
                output.close();
            }
           
            catch(ParseCancellationException exception) {
                output.println(exception.getMessage());
                output.println("Fim da compilacao");
                output.close();
            }
        }
        catch(IOException exception){
                System.out.println("Falha na execuÃ§Ã£o.\nO programa nÃ£o conseguiu abrir o arquivo: " + args[1]+ ".");
           }
  
    }
}