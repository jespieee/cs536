/**
 * SymTab
 *
 * This is a class that represents a SymbolTable (SymTab), which contains
 * a list of HashMap<String, Sym> objects. It provides the following operations:
 *     no-arg constructor                  -- creates a list with one hashmap entry
 *     void addDecl(String name, Sym sym)  -- attempts to add a new entry to the first hashmap
 *                                            entry in the list
 *     void addScope()                     -- adds a new empty Hashmap to the front of the list
 *     Sym lookupLocal(String name)        -- attempts to retrieve the first Sym mapped to the
 *                                            key of 'name' from the first hashmap entry
 *     Sym lookupGlobal(String name)       -- attempts to retrieve the first Sym mapped to the
 *                                            key of 'name' from any hashmap entry
 *     void removeScope()                  -- attempts to remove the first hashmap entry from the
 *                                            list
 *     void print()                        -- prints the contents of the list, purely for debugging
 */
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
     * @param name the name of the key entry to be added to the list
     * @param sym the name of the Sym entry to be added to the list
     * @throws
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
     * @param name the key to identify in the first HashMap in the list
     * @throws SymTabEmptyException - table's list is empty
     * @returns associated Sym of key name if found, otherwise null
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
     * @param name the key to identify in the list
     * @throws SymTabEmptyException - table's list is empty
     * @returns the first associated Sym of key name if found, otherwise null
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
     * @throws SymTabEmptyException - table's list is empty
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