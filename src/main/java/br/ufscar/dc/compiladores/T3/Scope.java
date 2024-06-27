package br.ufscar.dc.compiladores.T3;

import java.util.LinkedList;
import java.util.List;

public class Scope {
    
    private final LinkedList<TokenTable> scopeTokenTable;
    
    public Scope() {
        scopeTokenTable = new LinkedList<>();
        createNewScope();
    }
    
    public final void createNewScope() {
        scopeTokenTable.push(new TokenTable());
    }
    
    public List<TokenTable> runScope() {
        return scopeTokenTable;
    }
    
    public void leaveScope() {
        scopeTokenTable.pop();
    }
    
    public TokenTable peekScope() {
        return scopeTokenTable.peek();
    }
}