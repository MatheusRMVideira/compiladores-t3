package br.ufscar.dc.compiladores.T3;

import br.ufscar.dc.compiladores.T3.AlgumaParser;

import java.util.ArrayList;

public class Variable {
    
    public String name;
    public Type type;
    public Procedure procedure = null;
    public Registry registry = null;
    public Pointer pointer = null;
    public Function function = null;
    
    public Variable(){
        this.name = "";
        this.type = null;
    }
    
    public Variable(String name, Type type) {
        this.name = name;
        this.type = type;

        if (nonEmptyType(type)){
            Verify(type);
        }
    }

     public Type getNestedPointerType() {
        return pointer.getNestedType();
    }
      public Registry getRegistry() {
        return registry;
    }
    
    public class Pointer {
        private final Type pointer;
        
        public Pointer(Type p) {
            this.pointer = p;
        }
        public Type getType() {
            return pointer.getType();
        }
        public Type getNestedType() {
            return pointer.getNestedType();
        }
    }
        
    public static boolean nonEmptyType(Type type) {
        return (type != null && type.natives != null);
        
    }
    
    public class Registry {
        private final ArrayList<Variable> registry = new ArrayList<>();
        
        public Variable getVariable(String nome) {
            for (Variable v : registry)
                if (v.name.equals(nome))
                    return v;

            return null;
        }
        
        
        
        public ArrayList<Variable> getAll() {
            return registry;
        }
        
        public void addRegistry(ArrayList<Variable> aux) {
            registry.addAll(aux);
        }
        
        
    }
    
    public final void Verify(Type type){
         switch(type.natives){
                case POINTER:
                    pointer = new Pointer(type.apontado);
                    break;
                case REGISTRY:
                    registry = new Registry();
                    break;
                case PROCEDURE:
                    procedure = new Procedure();
                    break;
                case FUNCTION:
                    function = new Function();
                    break;
                    
            }
        
    }
    
    public class Procedure {
        private ArrayList<Variable> local;
        private ArrayList<Variable> parameters;
        
        public void setLocal(ArrayList<Variable> local) {
            this.local = local;
        }
        
        public void setParameters(ArrayList<Variable> parameters) {
            this.parameters = parameters;
        }
        
        public ArrayList<Variable> getParameters() {
            return parameters;
        }
        
        public ArrayList<Variable> getLocals() {
            return local;
        }
    }

    
    public class Function {
        private ArrayList<Variable> local;
        private ArrayList<Variable> parameters;
        private Type returnType;
          
        public void setReturnType(Type returnType) {
            this.returnType = returnType;
        }
        
        public void setLocal(ArrayList<Variable> local) {
            this.local = local;
        }
        
        public void setParameters(ArrayList<Variable> parameters) {
            this.parameters = parameters;
        }
        
        public Type getReturnType() {
            return returnType;
        }
        
        public ArrayList<Variable> getParameters() {
            return parameters;
        }
        
        public ArrayList<Variable> getLocal() {
            return local;
        }

        Iterable<AlgumaParser.Declaracao_localContext> getLocals() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
    
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
    
    public Procedure getProcedure() {
        return procedure;
    }
    
    public Function getFunction() {
        return function;
    }
}
