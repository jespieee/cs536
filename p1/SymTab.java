import java.util.*;

public class SymTab {

    private ArrayList<HashMap<String, Sym>> symbolTable;

    public SymTab() {
        this.symbolTable = new ArrayList<>();
        this.symbolTable.add(new HashMap<String, Sym>()); 
    }

    public void addDecl(String name, Sym sym) throws 
        SymDuplicateException, SymTabEmptyException {
        if (this.symbolTable.isEmpty()) {
            throw new SymTabEmptyException();
        }

        if (name == "" || sym == null) {
            throw new IllegalArgumentException();
        }

        if (this.symbolTable.get(0).containsKey(name)) {
            throw new SymDuplicateException();
        }

        this.symbolTable.get(0).put(name, sym);
    }

    public void addScope() {
        this.symbolTable.addFirst(new HashMap<String, Sym>());
    }

    public Sym lookupLocal(String name) throws SymTabEmptyException {
        if (this.symbolTable.isEmpty()) {
            throw new SymTabEmptyException();
        }

        if (this.symbolTable.get(0).containsKey(name)) {
            return this.symbolTable.get(0).get(name);
        }

        return null;
    }

    public Sym lookupGlobal(String name) throws SymTabEmptyException {
        if (this.symbolTable.isEmpty()) {
            throw new SymTabEmptyException();
        }
        
        for (int i = 0; i < this.symbolTable.size(); ++i) {
            if (this.symbolTable.get(i).containsKey(name)) {
                return this.symbolTable.get(i).get(name);
            }
        }

        return null;
    }

    public void removeScope() throws SymTabEmptyException {
        if (this.symbolTable.isEmpty()) {
            throw new SymTabEmptyException();
        }

        this.symbolTable.removeFirst();
    }

    public void print() {
        System.out.print("\n*** SymTab *** \n");
        for (int i = 0; i < this.symbolTable.size(); ++i) {
            System.out.println(this.symbolTable.get(i).toString());
        }
        System.out.print("\n*** DONE *** \n");
    }

}