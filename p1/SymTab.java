import java.util.*;

public class SymTab {

    private ArrayList<HashMap<String, Sym>> symbolTable;

    /*
     * Declares a new list with a single, empty HashMap entry
     */
    public SymTab() {
        this.symbolTable = new ArrayList<>();
        this.symbolTable.add(new HashMap<String, Sym>()); 
    }

    /*
     * Adds a new HashMap<String,Sym> entry to the list
     * Throws:
     *     SymTabEmptyException - table is empty
     *     SymDuplicateException - table already contains entry with key
     *     IllegalArgumentException - name or sym param is null
     */
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

        // otherwise, safe to add a new entry!
        this.symbolTable.get(0).put(name, sym);
    }

    /*
     * Adds a new, empty HashMap to the front of the list
     */
    public void addScope() {
        this.symbolTable.addFirst(new HashMap<String, Sym>());
    }

    /*
     * Looks at the first HashMap in the list for an entry with key of name
     * Throws -
     *     SymTabEmptyException - table's list is empty
     * Returns - 
     *     associated Sym of key name if found, otherwise null
     */
    public Sym lookupLocal(String name) throws SymTabEmptyException {
        if (this.symbolTable.isEmpty()) {
            throw new SymTabEmptyException();
        }

        if (this.symbolTable.get(0).containsKey(name)) {
            return this.symbolTable.get(0).get(name);
        }

        return null;
    }

    /*
     * Looks at all HashMaps in the list for an entry with key of name
     * Throws -
     *     SymTabEmptyException - table's list is empty
     * Returns - 
     *     the first associated Sym of key name if found, otherwise null
     */
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

    /*
     * Removes the list's first HashMap (front)
     * Throws -
     *     SymTabEmptyException - table's list is empty
     */
    public void removeScope() throws SymTabEmptyException {
        if (this.symbolTable.isEmpty()) {
            throw new SymTabEmptyException();
        }

        this.symbolTable.removeFirst();
    }

    /*
     * For use in debugging, prints the entire contents of the table
     */
    public void print() {
        System.out.print("\n*** SymTab *** \n");
        for (int i = 0; i < this.symbolTable.size(); ++i) {
            System.out.println(this.symbolTable.get(i).toString());
        }
        System.out.print("\n*** DONE *** \n");
    }

}