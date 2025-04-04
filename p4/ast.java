import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a bach program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and identifiers contain line and character 
// number information; for string literals and identifiers, they also 
// contain a string; for integer literals, they also contain an integer 
// value.
//
// Here are all the different kinds of AST nodes and what kinds of 
// children they have.  All of these kinds of AST nodes are subclasses
// of "ASTnode".  Indentation indicates further subclassing:
//
//     Subclass              Children
//     --------              --------
//     ProgramNode           DeclListNode
//     DeclListNode          linked list of DeclNode
//     DeclNode:
//       VarDeclNode         TypeNode, IdNode, int
//       FuncDeclNode        TypeNode, IdNode, FormalsListNode, FuncBodyNode
//       FormalDeclNode      TypeNode, IdNode
//       StructDeclNode      IdNode, DeclListNode
//
//     StmtListNode          linked list of StmtNode
//     ExpListNode           linked list of ExpNode
//     FormalsListNode       linked list of FormalDeclNode
//     FuncBodyNode          DeclListNode, StmtListNode
//
//     TypeNode:
//       BooleanNode         --- none ---
//       IntegerNode         --- none ---
//       VoidNode            --- none ---
//       StructNode          IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignExpNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       TrueNode            --- none ---
//       FalseNode           --- none ---
//       IdNode              --- none ---
//       IntLitNode          --- none ---
//       StrLitNode          --- none ---
//       StructAccessNode    ExpNode, IdNode
//       AssignExpNode       ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         EqualsNode
//         NotEqNode
//         LessNode
//         LessEqNode
//         GreaterNode
//         GreaterEqNode
//         AndNode
//         OrNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, 
// or internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        BooleanNode,  IntegerNode,  VoidNode,    IdNode,  
//        TrueNode,     FalseNode,    IntLitNode,  StrLitNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, StmtListNode, ExpListNode, FormalsListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,      FuncDeclNode,  FormalDeclNode,
//        StructDeclNode,  FuncBodyNode,     StructNode,    AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode,  IfStmtNode,    IfElseStmtNode,
//        WhileStmtNode,   ReadStmtNode,     WriteStmtNode, CallStmtNode,
//        ReturnStmtNode,  StructAccessNode, AssignExpNode, CallExpNode,
//        UnaryExpNode,    UnaryMinusNode,   NotNode,       BinaryExpNode,   
//        PlusNode,        MinusNode,        TimesNode,     DivideNode,
//        EqualsNode,      NotEqNode,        LessNode,      LessEqNode,
//        GreaterNode,     GreaterEqNode,    AndNode,       OrNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************F

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++)
            p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, DeclListNode, StmtListNode, ExpListNode,
// FormalsListNode, FuncBodyNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myDeclList.addAndAnalyzeDecl(symTable);
    }

    // 1 child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public List<Sym> addAndAnalyzeDecl(SymTab symTable) throws SymTabEmptyException {
        List<Sym> result = new ArrayList<>();
        for (DeclNode myDecl : myDecls) {
            result.add(myDecl.addAndAnalyzeDecl(symTable));
        }
        return result;
    }

    // list of children (DeclNodes)
    private List<DeclNode> myDecls;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        for (StmtNode myStmt : myStmts) {
            myStmt.nameAnalyze(symTable);
        }
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    public void checkDefined(SymTab symTable) throws SymTabEmptyException {
        for (ExpNode myExp : myExps) {
            myExp.checkDefinedAndGetSym(symTable);
        }
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    public List<Sym> addAndAnalyzeDecl(SymTab symTable) throws SymTabEmptyException {
        List<Sym> result = new ArrayList<>();
        for (FormalDeclNode fdNode : myFormals) {
            result.add(fdNode.addAndAnalyzeDecl(symTable));
        }
        return result;
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FuncBodyNode extends ASTnode {
    public FuncBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myDeclList.addAndAnalyzeDecl(symTable);
        myStmtList.nameAnalyze(symTable);
    }

    // 2 children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

// **********************************************************************
// ***** DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    abstract public Sym addAndAnalyzeDecl(SymTab symTable) throws SymTabEmptyException;
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(".");
    }

    public Sym addAndAnalyzeDecl(SymTab symTable) throws SymTabEmptyException {
        sym = symTable.lookupLocal(myId.toString());

        if (sym != null && sym.getCategory().equals(Category.FORMAL)) {
            return sym;
        }

        if (mySize == NON_STRUCT) {
            myId.addAndAnalyzeDecl(symTable, myType.toString(), Category.REG);
        } else {
            myId.addAndAnalyzeDecl(symTable, myType.toString(), Category.STRUCT_VAR);
        }

        return sym;
    }

    // 3 children
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NON_STRUCT if this is not a struct type
    private Sym sym;

    public static int NON_STRUCT = -1;
}

class FuncDeclNode extends DeclNode {
    public FuncDeclNode(TypeNode type,
            IdNode id,
            FormalsListNode formalList,
            FuncBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("[");
        myFormalsList.unparse(p, 0);
        p.println("] [");
        myBody.unparse(p, indent + 4);
        p.println("]\n");
    }

    public Sym addAndAnalyzeDecl(SymTab symTable) throws SymTabEmptyException {
        Sym sym = myId.addAndAnalyzeDecl(symTable, myType.toString(), Category.FUNC);

        if (sym.getCategory().equals(Category.UNDEFINED))
            return sym;

        FunctionSym myIdSym = (FunctionSym) sym;

        symTable.addScope();
        myIdSym.setFormalList(myFormalsList.addAndAnalyzeDecl(symTable));
        myBody.nameAnalyze(symTable);
        symTable.removeScope();

        return myIdSym;
    }

    // 4 children
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FuncBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    public Sym addAndAnalyzeDecl(SymTab symTable) throws SymTabEmptyException {
        return myId.addAndAnalyzeDecl(symTable, myType.toString(), Category.FORMAL);
    }

    // 2 children
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
        p.println(" [");
        myDeclList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("]\n");
    }

    public Sym addAndAnalyzeDecl(SymTab symTable) throws SymTabEmptyException {
        Sym sym = myId.addAndAnalyzeDecl(symTable, "struct", Category.STRUCT_DECL);

        if (sym.getCategory().equals(Category.UNDEFINED))
            return sym;

        SymTab structSymTable = new SymTab();
        myDeclList.addAndAnalyzeDecl(structSymTable);
        StructDeclSym structVarSym = (StructDeclSym) sym;
        structVarSym.setSymTable(structSymTable);
        return sym;
    }

    // 2 children
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// **** TypeNode and its subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
}

class BooleanNode extends TypeNode {
    public BooleanNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("boolean");
    }

    @Override
    public String toString() {
        return "boolean";
    }
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }

    @Override
    public String toString() {
        return "integer";
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    @Override
    public String toString() {
        return "void";
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        myId.unparse(p, 0);
    }

    // 1 child
    private IdNode myId;

    @Override
    public String toString() {
        return myId.toString();
    }
}

// **********************************************************************
// **** StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalyze(SymTab symTable) throws SymTabEmptyException;
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(".");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myAssign.checkDefinedAndGetSym(symTable);
    }

    // 1 child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++.");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
    }

    // 1 child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--.");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
    }

    // 1 child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
        symTable.addScope();
        myDeclList.addAndAnalyzeDecl(symTable);
        myStmtList.nameAnalyze(symTable);
        symTable.removeScope();
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
            StmtListNode slist1, DeclListNode dlist2,
            StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
        symTable.addScope();
        myThenDeclList.addAndAnalyzeDecl(symTable);
        myThenStmtList.nameAnalyze(symTable);
        symTable.removeScope();
        symTable.addScope();
        myElseDeclList.addAndAnalyzeDecl(symTable);
        myElseStmtList.nameAnalyze(symTable);
        symTable.removeScope();
    }

    // 5 children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
        symTable.addScope();
        myDeclList.addAndAnalyzeDecl(symTable);
        myStmtList.nameAnalyze(symTable);
        symTable.removeScope();
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("input -> ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
    }

    // 1 child (actually can only be an IdNode or a StructAccessNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("disp <- (");
        myExp.unparse(p, 0);
        p.println(").");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
    }

    // 1 child
    private ExpNode myExp;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(".");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        myCall.checkDefinedAndGetSym(symTable);
    }

    // 1 child
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(".");
    }

    @Override
    public void nameAnalyze(SymTab symTable) throws SymTabEmptyException {
        if (myExp != null) {
            myExp.checkDefinedAndGetSym(symTable);
        }
    }

    // 1 child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// **** ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    protected int myLineNum;
    protected int myCharNum;

    abstract public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("TRUE");
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        return new UndefinedSym();
    }
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("FALSE");
    }

    public void nameAnalyze(SymTab symTable) {
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        return new UndefinedSym();
    }
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (sym == null || declaration) {
            return;
        } else {
            p.print("{" + sym + "}");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym sym;
    private boolean declaration = false;

    public Sym addAndAnalyzeDecl(SymTab symTable, String type, Category category) {
        declaration = true;
        boolean success = true;

        if (!category.equals(Category.FUNC) && type.equals("void")) {
            ErrMsg.fatal(myLineNum, myCharNum, "Non-function declared void");
            success = false;
        }

        boolean foundSym = false;

        try {
            foundSym = symTable.lookupGlobal(type) == null ? false : true;
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        if (category.equals(Category.STRUCT_VAR) && !foundSym) {
            ErrMsg.fatal(myLineNum, myCharNum, "Name of struct field invalid");
            success = false;
        }

        switch (category) {
            case REG:
                sym = new Sym(type);
                break;
            case FUNC:
                sym = new FunctionSym(type);
                break;
            case FORMAL:
                sym = new FormalSym(type);
                break;
            case STRUCT_VAR:
                sym = new StructVarSym(type);
                break;
            case STRUCT_DECL:
                sym = new StructDeclSym(type);
                break;
            default:
                sym = new UndefinedSym();
                break;
        }

        try {
            symTable.addDecl(myStrVal, sym);
        } catch (Exception e) {
            ErrMsg.fatal(myLineNum, myCharNum, "Identifier multiply-declared");
            success = false;
        }

        return success ? sym : new UndefinedSym();
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        declaration = false;
        sym = symTable.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Identifier undeclared");
            sym = new UndefinedSym();
        }
        return sym;
    }

    @Override
    public String toString() {
        return myStrVal;
    }

}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        return new UndefinedSym();
    }

    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        return new UndefinedSym();
    }

    private String myStrVal;
}

class StructAccessExpNode extends ExpNode {
    public StructAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
    }

    // **** unparse ****
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print("):");
        myId.unparse(p, 0);
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        myLocSym = myLoc.checkDefinedAndGetSym(symTable);
        if (!myLocSym.getCategory().equals(Category.STRUCT_VAR)) {
            ErrMsg.fatal(myLoc.myLineNum, myLoc.myCharNum, "Dot-access of non-struct type");
            return new UndefinedSym();
        }

        StructDeclSym structDeclSym = (StructDeclSym) symTable.lookupGlobal(myLocSym.getType());

        myIdSym = structDeclSym.getSymTable().lookupGlobal(myId.toString());
        if (myIdSym == null) {
            ErrMsg.fatal(myLoc.myLineNum, myLoc.myCharNum, "Name of struct field invalid");
            myIdSym = new UndefinedSym();
        }
        return myIdSym;
    }

    // 2 children
    private ExpNode myLoc;
    private IdNode myId;
    private Sym myLocSym;
    private Sym myIdSym;
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)
            p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)
            p.print(")");
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        myExp.checkDefinedAndGetSym(symTable);
        return myLhs.checkDefinedAndGetSym(symTable);
    }

    // 2 children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        myExpList.checkDefined(symTable);
        return myId.checkDefinedAndGetSym(symTable);
    }

    // 2 children
    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        return myExp.checkDefinedAndGetSym(symTable);
    }

    // 1 child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    @Override
    public Sym checkDefinedAndGetSym(SymTab symTable) throws SymTabEmptyException {
        myExp2.checkDefinedAndGetSym(symTable);
        return myExp1.checkDefinedAndGetSym(symTable);
    }

    // 2 children
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// **** Subclasses of UnaryExpNode
// **********************************************************************

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(^");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// **** Subclasses of BinaryExpNode
// **********************************************************************

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" & ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" | ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqNode extends BinaryExpNode {
    public NotEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" ^= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
