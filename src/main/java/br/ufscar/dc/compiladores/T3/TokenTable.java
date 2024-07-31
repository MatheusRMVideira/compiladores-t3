package br.ufscar.dc.compiladores.T3;


import br.ufscar.dc.compiladores.T3.AlgumaParser;

import java.util.HashMap;

public class TokenTable {
    private final HashMap<String, Variable> table;

    public TokenTable() {
        table = new HashMap<>();
    }

    public Type getType(String name) {
        return table.get(name).type;
    }

    public Variable getVar(String name) {
        return table.get(name);
    }
    public void add(Variable v) {
        table.put(v.name, v);
    }
    public boolean contem(String name) {
        return table.containsKey(name);
    }

    void add(AlgumaParser.Declaracao_localContext v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}