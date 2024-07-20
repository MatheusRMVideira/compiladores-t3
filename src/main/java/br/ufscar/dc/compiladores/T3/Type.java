package br.ufscar.dc.compiladores.T3;

import java.util.ArrayList;

public class Type {
       enum Nativos{INTEIRO,
        REAL,
        LITERAL,
        LOGICO,
           POINTER,
        ENDERECO,
           REGISTRY,
           PROCEDURE,
           FUNCTION,
        INVALIDO}

    public static ArrayList<String> Criados = new ArrayList<>();
    
    public Nativos natives = null;
    public String criados = null;   
    public Type apontado = null;
    
    public Type(){
        natives = null;
        criados = null;
    }
    public Type(Nativos tipo) {
        natives = tipo;
    }
    
    
    public Type(String tipo) {
        criados = tipo;
    }
    
    public Type(Type filho) {
        natives = Nativos.POINTER;
        apontado = filho;
    }
    
    public Type getType() {
        if (apontado != null) 
            return apontado.getType();        
        return this;
    }
    public boolean emptyType(){
           return (this != null && this.natives != null);
    } 
    
    
    public static String getType(String type) {
        String existe = Criados.stream()
                .filter(str -> str.trim().contains(type))
                .findAny()
                .orElse("");                    
        if(!"".equals(existe))    
            return existe;
        else
            return null; 
    }
       public Type getNestedType() {
        if (apontado == null) 
            return this;
        
        Type type = apontado;
        while (type.apontado != null)
            type = type.getNestedType();
        
        return type;
    }
   
    
   public Type validaTipo(Type type) {
        if (this.natives == Nativos.POINTER && type.natives == Nativos.ENDERECO)
            return new Type(Nativos.POINTER);
        else if ((this.natives == Nativos.REAL && (type.natives == Nativos.REAL || type.natives == Nativos.INTEIRO))
                    || (this.natives == Nativos.INTEIRO && (type.natives == Nativos.REAL|| type.natives == Nativos.INTEIRO)))
            return new Type(Nativos.REAL);
        if (this.natives == Nativos.LITERAL && type.natives == Nativos.LITERAL)
            return new Type(Nativos.LITERAL);
        if (this.natives == Nativos.LOGICO && type.natives == Nativos.LOGICO)
            return new Type(Nativos.LOGICO);
        if (this.natives == Nativos.REGISTRY && type.natives == Nativos.REGISTRY)
            return new Type(Nativos.REGISTRY);
        return new Type(Nativos.INVALIDO);
    }
    
    public Type verificaEquivalenciaTipo(Type type) {

        if (this.natives == Nativos.ENDERECO && type.natives == Nativos.POINTER)
            return new Type(Nativos.ENDERECO);
        if (this.natives == Nativos.REGISTRY && type.natives == Nativos.REGISTRY)
            return new Type(Nativos.REGISTRY);
        if (this.natives == Nativos.REAL && type.natives == Nativos.REAL)
            return validaTipo(type);
        return new Type(Nativos.INVALIDO);
    }
    
    public static void adicionaNovoTipo(String tipo) {
        Criados.add(tipo);
    }
    
    public String getFormat() {
        if (natives != null) {
            if(natives == Nativos.INTEIRO)
                return "int";
            if(natives == Nativos.REAL)
                return "float";
            if(natives == Nativos.LITERAL)
                return "char";
        }

        return criados;
    }
    
    public String getFormatSpec() {
        if (natives != null) {
            if(natives == Nativos.INTEIRO)
                return "%d";
            if(natives == Nativos.REAL)
                return "%f";
            if(natives == Nativos.LITERAL)
                return "%s";
        }
        return "";
    }
}
