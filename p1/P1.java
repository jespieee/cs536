/**
 * P1
 *
 * This is a class whose sole purpose is to test the SymTab/Sym class.
 * For a list of SymTab and Sym operations, refer to their respective
 * file headers.
 *
 * This code tests every SymTab operation, including both correct and
 * bad calls to the operations that can throw an exception.
 * It produces output if a test fails, as well as for updates to the contents of 
 * the SymTab change to allow for visual confirmation.
 */

public class P1 {
    public static void main(String[] args) {

        Sym emoji = new Sym("emoji");
        Sym doubleSym = new Sym("double");
        Sym voidSym = new Sym("void");
        Sym boolSym = new Sym("bool");
        Sym intSym = new Sym("int");

        // This section contains the test output to match the expected output
        SymTab table = new SymTab();
        table.print();

        try {
            table.addDecl("aaa", boolSym);
            table.addDecl("bbb", intSym);
            table.addScope();
            table.addDecl("ccc", voidSym);
            table.addScope();
            table.addDecl("ddd", doubleSym);
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        table.print();

        for (int i = 0; i < 3; ++i) {
            try {
                table.removeScope();
                table.print();
            } catch (Exception e) {
                System.out.println("Unexpected exception thrown!");
            }
        }

        // SECTION END

        // This section contains exception and other edge case tests
        table.addScope();

        // table should contain an empty HashMap at index 0
        try {
            table.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println("removeScope on non-empty table failed!");
        }
        
        // table should now be completely empty

        // removeScope on empty list test
        try {
            table.removeScope();
        } catch (SymTabEmptyException e) {
            // this is the expected result, continue
        }

        table.addScope();
        // table should contain an empty HashMap at index 0
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
            // this is the expected result, continue
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        table.addScope();
        try {
            table.addDecl("smirk", emoji);
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        // addDecl with duplicate key test
        try {
            table.addDecl("smirk", emoji);
        } catch (SymDuplicateException e) {
            // this is the expected result, continue
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        // setup for lookupLocal/lookupGlobal on non-empty table test
        table.addScope();
        try {
            table.addDecl("smile", emoji);
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }
        try {
            if (emoji == table.lookupLocal("smirk")) {
                System.out.println("lookupLocal failed! smirk is not in the first entry of the table");
            }
            if (emoji != table.lookupGlobal("smirk")) {
                System.out.println("lookupGlobal failed! smirk is in the table");
            }
            if (emoji != table.lookupLocal("smile")) {
                System.out.println("lookupLocal failed! smile is the first entry in the table");
            }
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        // setup for lookupLocal/lookupGlobal on empty table test
        try {
            table.removeScope();
            table.removeScope();
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown!");
        }

        try {
            Sym placeholder1 = table.lookupLocal("smirk");
            Sym placeholder2 = table.lookupGlobal("smirk");
            System.out.println("lookupLocal and lookupGlobal failed to throw SymTabEmptyException!");
        } catch (SymTabEmptyException e) {
            // this is the expected result, continue
        }
    }
}
