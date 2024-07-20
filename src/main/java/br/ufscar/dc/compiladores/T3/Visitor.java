package br.ufscar.dc.compiladores.T3;

import br.ufscar.dc.compiladores.T3.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.T3.AlgumaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;


public class Visitor extends AlgumaBaseVisitor<Void> {
    public Errors errorlist = new Errors();
    private final Scope scope = new Scope();
    public static final Visitor I = new Visitor();

    private boolean retAux;

    @Override
    public Void visitDecl_local_global(AlgumaParser.Decl_local_globalContext ctx) {
        ArrayList<Variable> entrada = new ArrayList<>();
        if (ctx.declaracao_local() != null) // Se for local,faz a verificação e adiciona em entrada
            entrada = verificaDeclLocal(scope, ctx.declaracao_local());
        else // Se for glogal, faz a verificação e adiciona em entrada
            entrada.add(verificaDeclGlobal(scope, ctx.declaracao_global()));
        
        addVarEscopo(entrada);
        
        return null;
    }
    
    public Scope getScope() {
        return scope;
    }
        
    //Adiciona as variáveis no escopo
    public void addVarEscopo(ArrayList<Variable> var) {
        for(Variable auxVar: var) {
            scope.peekScope().add(auxVar);
        }
    }

    @Override
    public Void visitCorpo(AlgumaParser.CorpoContext ctx) {
        
        ArrayList<Variable> variableList = new ArrayList<>();
        
        for (AlgumaParser.Declaracao_localContext x : ctx.declaracao_local()) 
        {
            variableList.addAll(verificaDeclLocal(scope, x));
            addVarEscopo(variableList);
        }
        for (AlgumaParser.CmdContext cmd : ctx.cmd())
            validaCmd(scope.peekScope(), cmd);
             
        return null;
    }
    
    
    public ArrayList<Variable> verificaDeclLocal(Scope escopo, AlgumaParser.Declaracao_localContext ctx) {
        ArrayList<Variable> retorno = new ArrayList<>();
        switch(Correspondencia(ctx.getStart().getText())){

            case 1:
                Type type1 = verificaTipo(ctx.tipo());
                if (type1.natives != null && type1.natives == Type.Nativos.INVALIDO) {
                    errorlist.addError(3,ctx.start.getLine(), ctx.tipo().getText());
                }else{
                    String nome = ctx.IDENT().getText();
                    Type.adicionaNovoTipo(nome);
                    Variable novoTipo = new Variable(nome, type1);

                    if (novoTipo.type.natives == Type.Nativos.REGISTRY)
                        novoTipo.registry = validaRegistro(escopo, ctx.tipo().registro()).registry;

                    retorno.add(novoTipo);
                }
            break;
            case 2:
                Type type2 = new Type(validaTipoNat(ctx.tipo_basico()));

                if (type2.natives != null && type2.natives == Type.Nativos.INVALIDO)
                    errorlist.addError(3,ctx.start.getLine(), ctx.tipo().getText());
                else
                    retorno.add(new Variable(ctx.IDENT().getText(), type2));
            break;
            case 3:
                retorno = validaVar(escopo, ctx.variavel());
            break;
        }    
        return retorno;
    }

    

    // Faz a verificação da declGlobal
    public Variable verificaDeclGlobal(Scope escopo, AlgumaParser.Declaracao_globalContext ctx) {
        Variable auxVar = null;
        
        switch(Correspondencia(ctx.getStart().getText())){
        
            case 4:    // Se for uma função  
                Type typeRetorno = validaEstendido(ctx.tipo_estendido());
                escopo.createNewScope();
                retAux = true;
                auxVar = new Variable(ctx.IDENT().getText(), new Type(Type.Nativos.FUNCTION));
                if (ctx.parametros() != null) {
                    
                    ArrayList<Variable> param = validaParametros(escopo, ctx.parametros());
                    auxVar.function.setParameters(param);
                    addVarEscopo(param);
                }
                auxVar.function.setReturnType(typeRetorno);
                ArrayList<Variable> declara = new ArrayList<>();
                for (AlgumaParser.Declaracao_localContext declaracao : ctx.declaracao_local())
                    declara.addAll(verificaDeclLocal(escopo, declaracao));
                addVarEscopo(declara);
                auxVar.function.setLocal(declara);
                for (AlgumaParser.CmdContext cmd : ctx.cmd())
                    validaCmd(escopo.peekScope(), cmd);
                escopo.leaveScope();
                retAux = false;
            break;
         
            case 5: // Se for um procedimento
                escopo.createNewScope();
                auxVar = new Variable(ctx.IDENT().getText(), new Type(Type.Nativos.PROCEDURE));
                
                if (ctx.declaracao_local() != null) {
                    ArrayList<Variable> decl = new ArrayList<>();
                    
                    for (AlgumaParser.Declaracao_localContext declaracao : ctx.declaracao_local()) {
                        decl.addAll(verificaDeclLocal(escopo, declaracao));                
                    }
                    addVarEscopo(decl);
                    auxVar.procedure.setLocal(decl);
                }
                if (ctx.parametros() != null) {
                    ArrayList<Variable> parametros = validaParametros(escopo, ctx.parametros());
                    
                    addVarEscopo(parametros);
                    auxVar.procedure.setParameters(parametros);
                }
                if (ctx.cmd() != null)
                    for (AlgumaParser.CmdContext cmd : ctx.cmd())
                        validaCmd(escopo.peekScope(), cmd);

                escopo.leaveScope();
            break;
        }
        return auxVar;
    }

    
    public Variable validaRegistro(Scope escopo, AlgumaParser.RegistroContext ctx) {
        
        Variable auxRegistro = new Variable("", new Type(Type.Nativos.REGISTRY));
        escopo.createNewScope();
       
        for (int i = 0; i < ctx.variavel().size(); i++) {
            auxRegistro.registry.addRegistry(validaVar(escopo, ctx.variavel(i)));
        }
       
        return auxRegistro;
    }
    
    
    // Verifica o tipo
    public int Correspondencia(String receptor){
        switch(receptor){
            case "tipo": return 1;
            case "constante": return 2;
            case "declare": return 3;
            case "funcao": return 4;
            case "procedimento": return 5;
        }
    return 0;
    }
    //Valida os parametros e retorna uma lista de variáveis correspondetes dos parametros
    public ArrayList<Variable> validaParametros(Scope escopo, AlgumaParser.ParametrosContext ctx) {
        
        ArrayList<Variable> retorno = new ArrayList<>();

        for (AlgumaParser.ParametroContext param : ctx.parametro()){
            ArrayList<Variable> parametros = new ArrayList<>();
            Type type = validaEstendido(param.tipo_estendido());
            
            for (AlgumaParser.IdentificadorContext i : param.identificador()) {
                    Variable auxvar = new Variable(i.getText(), type);
                        for (TokenTable ts : escopo.runScope()) {
                            Variable aux = adicionaNovoTipo(ts, auxvar, type.criados);
                            if (aux.type != null)
                                auxvar = aux;
                        }
                    parametros.add(auxvar);
                    escopo.peekScope().add(auxvar);
                }
            retorno.addAll(parametros);
        }
        
        return retorno;
    }

    public Type validaTipoIdent(AlgumaParser.Tipo_basico_identContext ctx) {
        if (ctx.tipo_basico() != null) 
            return new Type(validaTipoNat(ctx.tipo_basico()));
        
        if ((Type.getType(ctx.IDENT().getText()))!= null)
            return new Type(Type.getType(ctx.IDENT().getText()));
        
        return new Type(Type.Nativos.INVALIDO);
    }
    
    //Valida os comandos do contexto
    //Verifica todos os comandos: Escreva, leia, se, faca, enquanto e atribuição e retorne
    //Caso o comando esteja com erro, adiciona o erro na lista e erros.
    public void validaCmd(TokenTable ts, AlgumaParser.CmdContext ctx) {
        String base = "";
        if (ctx.cmdAtribuicao() != null){
            Variable left = validaIdent(ts, ctx.cmdAtribuicao().identificador());
            Type typeL = left.type;
            if (typeL == null) {
                errorlist.addError(0,ctx.cmdAtribuicao().identificador().start.getLine(), ctx.cmdAtribuicao().identificador().getText());
                return;
            }

            Type typeR = validaExpressao(ts, ctx.cmdAtribuicao().expressao());

            if (ctx.getChild(0).getText().contains("^")) {
                 base += "^";
                typeL = left.pointer.getType();
            }
 
             if (typeL.validaTipo(typeR).natives == Type.Nativos.INVALIDO && typeL.natives != null)  {
                errorlist.addError(2,ctx.cmdAtribuicao().identificador().start.getLine(), base + ctx.cmdAtribuicao().identificador().getText());
            }
        }
        else if (ctx.cmdEscreva() != null){
            for (AlgumaParser.ExpressaoContext exp : ctx.cmdEscreva().expressao()) 
                validaExpressao(ts, exp);
            }
        else if (ctx.cmdLeia() != null){
                    for (AlgumaParser.IdentificadorContext i : ctx.cmdLeia().identificador()) {
                        Variable auxVar = validaIdent(ts, i);
                        if (auxVar != null && auxVar.type == null)
                            errorlist.addError(0,i.getStart().getLine(), i.getText());
                    }
        }
        else if (ctx.cmdEnquanto() != null)
            validaExpressao(ts, ctx.cmdEnquanto().expressao());
        else if (ctx.cmdSe() != null){
            validaExpressao(ts, ctx.cmdSe().expressao());
            for (AlgumaParser.CmdContext cmd : ctx.cmdSe().cmd())
                validaCmd(ts, cmd);
        }
        else if (ctx.cmdFaca() != null){
            validaExpressao(ts, ctx.cmdFaca().expressao());
            for (AlgumaParser.CmdContext cmd : ctx.cmdFaca().cmd())
                validaCmd(ts, cmd); 
        }
        else if (ctx.cmdRetorne() != null){ 
            if (!retAux)
                errorlist.addError(5,ctx.start.getLine(),"");
        }
    }

    public ArrayList<Variable> validaVar(Scope escopo, AlgumaParser.VariavelContext ctx) {
        ArrayList<Variable> retorno = new ArrayList<>();
        Type type = verificaTipo(ctx.tipo());
               
        for (AlgumaParser.IdentificadorContext ident : ctx.identificador()){
            Variable auxVar;
            auxVar = validaIdent(escopo.peekScope(), ident);
            
            // Se já estiver sido declarado anteriormente, adiciona erro na lista de erros
            if (auxVar.type != null)
                errorlist.addError(1,ident.getStart().getLine(), ident.getText());
            else {
                auxVar = new Variable(auxVar.name, type);
                if (type.criados != null){
                    auxVar = adicionaNovoTipo(escopo.peekScope(), auxVar, type.criados);
                }
                if (type.natives == Type.Nativos.REGISTRY){
                    auxVar.registry = validaRegistro(escopo, ctx.tipo().registro()).registry;
                }
                escopo.peekScope().add(auxVar);
                retorno.add(auxVar);
            }
        }
        
        // Se tipo for nulo ou inválido, adiciona erro na lista de errros
        if (type.natives != null && type.natives == Type.Nativos.INVALIDO)
            errorlist.addError(3,ctx.start.getLine(), ctx.tipo().getText());
        
        return retorno;
    }

    public Type verificaTipo(AlgumaParser.TipoContext ctx) {
        return ((ctx.registro() != null) ? new Type(Type.Nativos.REGISTRY) : validaEstendido(ctx.tipo_estendido()));
    }

    public Type validaEstendido(AlgumaParser.Tipo_estendidoContext ctx) {
        return ((ctx.getChild(0).getText().contains("^")) ? new Type(validaTipoIdent(ctx.tipo_basico_ident())): validaTipoIdent(ctx.tipo_basico_ident()));
    }

    // Valida a parcela lógica da expressão
    // Se for uma expressão relacional, retorna a chamada da função validaExpRelacional. Senão, retorna um novo tipo lógico 
    public Type validaParcelaLogica(TokenTable ts, AlgumaParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null)
            return validaExpRelacional(ts, ctx.exp_relacional());
        return new Type(Type.Nativos.LOGICO);
    }

    private Type.Nativos validaTipoNat(AlgumaParser.Tipo_basicoContext ctx) {

        if(ctx.getText().equals("inteiro"))
            return Type.Nativos.INTEIRO;
        if(ctx.getText().equals("real"))
            return Type.Nativos.REAL;
        if(ctx.getText().equals("literal"))
            return Type.Nativos.LITERAL;
        if(ctx.getText().equals("logico"))
            return Type.Nativos.LOGICO;
        
        return Type.Nativos.INVALIDO;
    }

    // Faz a validação da  expressão do contexto
    public Type validaExpressao(TokenTable ts, AlgumaParser.ExpressaoContext ctx) {
        Type type = validaTermosLogicos(ts, ctx.termo_logico(0));
        if (ctx.termo_logico().size() > 1) {
            for (int i = 1; i < ctx.termo_logico().size(); i++) {
                type = type.validaTipo( validaTermosLogicos(ts, ctx.termo_logico(i)));
            }
            if (type.natives != Type.Nativos.INVALIDO)
                type.natives = Type.Nativos.LOGICO;
        }
        return type;
    }

    public Type validaTermosLogicos(TokenTable ts, AlgumaParser.Termo_logicoContext ctx) {
        Type type = validaFatorLogico(ts, ctx.fator_logico(0));
       
            for (int i = 1; i < ctx.fator_logico().size(); i++)
                type = type.validaTipo(validaFatorLogico(ts, ctx.fator_logico(i)));
        return type;
    }

    public Type validaExpRelacional(TokenTable ts, AlgumaParser.Exp_relacionalContext ctx) {
        Type type = validaExpAritmetica(ts, ctx.exp_aritmetica(0));

        if (ctx.exp_aritmetica().size() > 1) {
            type = type.validaTipo(validaExpAritmetica(ts, ctx.exp_aritmetica(1)));
            
            if (type.natives != Type.Nativos.INVALIDO)
                type.natives = Type.Nativos.LOGICO;
        }

        return type;
    }

    public Type validaFatorLogico(TokenTable ts, AlgumaParser.Fator_logicoContext ctx) {
        Type type = validaParcelaLogica(ts, ctx.parcela_logica());
            return ((ctx.getChild(0).getText().contains("nao"))? type.validaTipo(new Type(Type.Nativos.LOGICO)): type);
    }

 

    public Type validaExpAritmetica(TokenTable ts, AlgumaParser.Exp_aritmeticaContext ctx) {
        Type type = validaTermo(ts, ctx.termo(0));
        for (int i = 1; i < ctx.termo().size(); i++)
            type = type.validaTipo(validaTermo(ts, ctx.termo(i)));

        return type;
    }

    public Type validaTermo(TokenTable ts, AlgumaParser.TermoContext ctx) {
        Type type = validaFator(ts, ctx.fator(0));
            for (int i = 1; i < ctx.fator().size(); i++)
                type = type.validaTipo(validaFator(ts, ctx.fator(i)));
        
        return type;
    }

    public Type validaFator(TokenTable ts, AlgumaParser.FatorContext ctx) {
        Type type = validaParcela(ts, ctx.parcela(0));
            for (int i = 1; i < ctx.parcela().size(); i++)
                type = type.validaTipo(validaParcela(ts, ctx.parcela(i)));

        return type;
    }

    public Type validaParcela(TokenTable ts, AlgumaParser.ParcelaContext ctx) {
        
        if (ctx.parcela_unario() != null) {
            Type type = validaParcelaSimples(ts, ctx.parcela_unario());
            if (ctx.op_unario() != null) {
                if (type.natives != Type.Nativos.INTEIRO && type.natives != Type.Nativos.REAL)
                    return new Type(Type.Nativos.INVALIDO);
                return type;
            }
            return type;
        }
        return validaParcelaUnaria(ts, ctx.parcela_nao_unario());
    }

    public Variable validaIdent(TokenTable ts, AlgumaParser.IdentificadorContext ctx) {
        String nome = ctx.IDENT(0).getText();

        if (ts.contem(nome)) {
            Variable retorno = ts.getVar(nome);
            if (ctx.IDENT().size() > 1) {
                retorno = retorno.registry.getVariable(ctx.IDENT(1).getText());
                if (retorno == null)
                    errorlist.addError(0,ctx.start.getLine(), ctx.getText());
            }
            return retorno;
        }

        return new Variable(Adequacao(ctx,nome), null);
    }
    
    public Type validaMetodo(TokenTable ts, TerminalNode IDENT, List<AlgumaParser.ExpressaoContext> exprs) {
        
        Type retorno = null;
        Variable metodo = ts.getVar(IDENT.getText());
        
            for (AlgumaParser.ExpressaoContext exp : exprs) {
                Type typeExp = validaExpressao(ts, exp);
                if (retorno == null || retorno.natives != Type.Nativos.INVALIDO)
                    retorno = typeExp.verificaEquivalenciaTipo(metodo.function.getReturnType());
            }
      
        if (retorno.natives == Type.Nativos.INVALIDO){
            errorlist.addError(4,IDENT.getSymbol().getLine(), IDENT.getText());
            return new Type(Type.Nativos.INVALIDO);
        }

        return retorno;
    }

    public Type validaParcelaSimples(TokenTable ts, AlgumaParser.Parcela_unarioContext ctx) {
        
        if (ctx.NUM_INT() != null)
            return new Type(Type.Nativos.INTEIRO);
        if (ctx.NUM_REAL() != null)
            return new Type(Type.Nativos.REAL);
        if (ctx.IDENT() != null)
            return validaMetodo(ts, ctx.IDENT(), ctx.expressao());
        
        if (ctx.identificador() != null) {
            Variable ident = validaIdent(ts, ctx.identificador());

            if (ident.type == null) {
                errorlist.addError(0,ctx.identificador().start.getLine(), ident.name);
                return new Type(Type.Nativos.INVALIDO);
            }

            return ident.type;
        }   

        
        Type type = validaExpressao(ts, ctx.expressao(0));
            for (int i = 1; i < ctx.expressao().size(); i++)
                type = type.validaTipo( validaExpressao(ts, ctx.expressao(i)));

        return type;
    }

    public Type validaParcelaUnaria(TokenTable ts, AlgumaParser.Parcela_nao_unarioContext ctx) {
        if (ctx.CADEIA() != null)
            return new Type(Type.Nativos.LITERAL);
        else {
            if (ctx.getChild(0).getText().contains("&"))
                return new Type(Type.Nativos.ENDERECO);

            Variable ident = validaIdent(ts, ctx.identificador());
            if (ident.type == null) {
                errorlist.addError(0,ctx.identificador().start.getLine(), ident.name);
                return new Type(Type.Nativos.INVALIDO);
            }
            return ident.type;
        }
    }

    

   public Variable adicionaNovoTipo(TokenTable ts, Variable auxVar, String nome) {
        if (ts.contem(nome)) {
            Variable modelo = ts.getVar(nome);
            if (modelo.type.natives == Type.Nativos.REGISTRY) {
                Variable retorno = new Variable(auxVar.name, new Type(Type.Nativos.REGISTRY));
                retorno.setRegistry(modelo.registry);
                retorno.type = auxVar.type;
                return retorno;
            }
        }
        return new Variable(null, null);
    }
    
    public String Adequacao(AlgumaParser.IdentificadorContext base, String Nome){
        for (int i = 1; i < base.IDENT().size(); i++)
            Nome += "." + base.IDENT(i);
    return Nome;

    }
}