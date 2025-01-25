public class P1 {
    public static void main() {
        Sym number = new Sym("number");
        Sym letter = new Sym("letter");
        Sym emoji = new Sym("emoji");

        SymTab table = new SymTab();

        // table should contain an empty HashMap at index 0
        try {
            table.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println("removeScope on non-empty table failed!");
        }

        // removeScope on empty list test
        try {
            table.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println("Exception properly thrown, removeScope called when table list is empty");
        }

        table.addScope();

        // addDecl null param test
        try {
            table.addDecl(null, emoji);
        } catch (IllegalArgumentException e) {
            System.out.println("Exception properly thrown, addDecl called with a null param");
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }
        
        // setup for addDecl on empty list test
        try {
            table.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println("removeScope on non-empty table failed!");
        }

        // addDecl on empty list test
        try {
            table.addDecl("smirk", emoji);
        } catch (SymTabEmptyException e) {
            System.out.println("Exception properly thrown, addDecl called when table list is empty");
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        // addDecl with duplicate key test
        try {
            table.addDecl("smirk", emoji);
        } catch (SymDuplicateException e) {
            System.out.println("Exception properly thrown, addDecl called with existing key");
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        // setup for lookupLocal/lookupGlobal test
        table.addScope();
        try {
            table.addDecl("smile", emoji);
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        try {
            if (emoji == table.lookupLocal("smirk")) {
                // TODO
            }
        } catch (SymTabEmptyException e) {

        }
    }
}
