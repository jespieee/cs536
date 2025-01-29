/**
 * Sym
 *
 * This is a class that represents a single Symbol (Sym) in a SymbolTable (SymTab)
 * which provides the following operations:
 *     Sym(String type) -- constructs a new Sym with a type of 'type'
 *     getType()        -- retrieves the Sym's type
 *     toString()       -- retrieves the Sym's type
 */
public class Sym {

    private String type;

    /*
     * Declares a new Sym with a type of 'type'
     */
    public Sym(String type) {
        this.type = type;
    }

    /*
     * Retrieves the value of the Sym's type field
     * @returns the value of the Sym's type field
     */
    public String getType() {
        return this.type;
    }

    /*
     * Retrieves the value of the Sym's type field (this method will be changed later)
     * @returns the value of the Sym's type field
     */
    public String toString() {
        return this.type;
    }
    
}