package br.ufscar.dc.compiladores.T3;

import br.ufscar.dc.compiladores.T3.AlgumaLexer;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;


public class ErrorHandler extends BaseErrorListener {

    public static final ErrorHandler INSTANCE = new ErrorHandler();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
            int charPositionInLine, String msg, RecognitionException e) 
                throws ParseCancellationException{
        

        Token token = (Token) offendingSymbol;


        // Mensagem padrao para erros
        String base = "Linha " + token.getLine() + ": "; 

        //Tratativa de erros
        if(eh_erro(token.getType())) {
            if (token.getType() == AlgumaLexer.Caracter_invalido) {
                throw new ParseCancellationException(base + token.getText() + " - simbolo nao identificado");
            }
            else if(AlgumaLexer.VOCABULARY.getSymbolicName(token.getType()).equals("CADEIA_SEM_FIM"))
            {
                throw new ParseCancellationException(base + "cadeia literal nao fechada");
            }
            else {
                throw new ParseCancellationException(base + "comentario nao fechado");
            }

        }
        //Se o erro nao for lexico, verifica se eh EOF, se nao for eh sintatico
        else if (token.getType() == Token.EOF)
                throw new ParseCancellationException(base + "erro sintatico proximo a EOF");
        else
                throw new ParseCancellationException(base + "erro sintatico proximo a " + token.getText());
        }

    private static Boolean eh_erro(int tkType) {
        //True caso seja um dos 3 tipos de erro lexico
                return tkType == AlgumaLexer.CADEIA_SEM_FIM || tkType == AlgumaLexer.COMENTARIO_SEM_FIM
                        || tkType == AlgumaLexer.Caracter_invalido;
    }
}