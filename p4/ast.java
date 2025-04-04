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

    public void nameAnalyze() {
        SymTab symTable = new SymTab();
        myDeclList.nameAnalyze(symTable);
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

    public SymTab nameAnalyzeFnBody(SymTab symTable) {
        HashMap<String, Integer> occurance = new HashMap<>();
        for (DeclNode declNode : myDecls) {
            IdNode idNode = declNode.getIdNode();
            String name = idNode.toString();
            occurance.put(name, occurance.getOrDefault(name, 0) + 1);
            if (occurance.get(name) == 1 && symTable.lookupLocal(name) == null) {
                // first time in body and not in formlist
                declNode.nameAnalyze(symTable);
            }
            if (occurance.get(name) > 1) {
                // occurance twice in the body
                ErrMsg.fatal(idNode.getLineNum(), idNode.getCharNum(),
                        "Identifier multiply-declared");
            }

        }
        return symTable;
    }

    public SymTab nameAnalyze(SymTab symTable) {
        for (DeclNode declNode : myDecls) {
            declNode.nameAnalyze(symTable);
        }
        return symTable;
    }

    // structdecl
    public SymTab nameAnalyze(SymTab symTable, SymTab structSymTable) {
        for (DeclNode declNode : myDecls) {
            VarDeclNode node = (VarDeclNode) declNode;
            if (node.getSize() == VarDeclNode.NON_STRUCT) {
                node.nameAnalyze(structSymTable);
            } else {
                node.nameAnalyzeStruct(symTable, structSymTable);
            }
        }
        return symTable;
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

    public void nameAnalyze(SymTab symTable) {
        for (StmtNode stmtNode : myStmts) {
            stmtNode.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        for (ExpNode expNode : myExps) {
            expNode.nameAnalyze(symTable);
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

    public SymTab nameAnalyze(SymTab symTable) {
        for (FormalDeclNode formalDeclNode : myFormals) {
            formalDeclNode.nameAnalyze(symTable);
        }
        return symTable;
    }

    public LinkedList<String> getTypeList() {
        LinkedList<String> paramTypes = new LinkedList<>();
        for (FormalDeclNode formalDeclNode : myFormals) {
            paramTypes.add(formalDeclNode.getTypeString());
        }
        return paramTypes;
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

    public void nameAnalyze(SymTab symTable) {
        myDeclList.nameAnalyzeFnBody(symTable);
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
    abstract public SymTab nameAnalyze(SymTab symtable);
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

    public int getSize() {
        return mySize;
    }

    public IdNode getIdNode() {
        return this.myId;
    }

    // Var declaration (regular)
    public SymTab nameAnalyze(SymTab symTable) {
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                    "Non-function declared void");
            return symTable;
        }
        if (myType instanceof StructNode) {
            boolean result = this.nameAnalyzeStructName(symTable);
            Sym structSym = null;

            try {
                structSym = symTable.lookupGlobal(((StructNode) myType).getId().toString());
            } catch (SymTabEmptyException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }

            if (structSym == null || result == false) {
                return symTable;
            }
            this.nameAnalyzeVarName(symTable);

            Sym mySym = null;
            try {
                mySym = symTable.lookupGlobal(myId.toString());
            } catch (SymTabEmptyException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }

            myId.setStruct(structSym.getStruct(), mySym);
            return symTable;
        }
        this.nameAnalyzeVarName(symTable);
        return symTable;
    }

    public void nameAnalyzeVarName(SymTab symTable) {
        Sym newSym = new Sym(this.myType.toString());
        // myId.setSym(newSym);
        try {
            symTable.addDecl(this.myId.toString(), newSym);
        } catch (SymDuplicateException e) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                    "Identifier multiply-declared");
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
        return;
    }

    // Struct declaration
    public void nameAnalyzeStruct(SymTab symTable, SymTab symTableStruct) {
        this.nameAnalyzeVarName(symTableStruct);
        this.nameAnalyzeStructName(symTable);
        if (myType instanceof StructNode) {
            Sym structSym = null, mySym = null;

            try {
                structSym = symTable.lookupGlobal(((StructNode) myType).getId().toString());
                mySym = symTableStruct.lookupGlobal(myId.toString());
            } catch (SymTabEmptyException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }

            if (structSym != null && mySym != null) {
                myId.setStruct(structSym.getStruct(), mySym);
            }
        }
    }

    public boolean nameAnalyzeStructName(SymTab symTable) {
        IdNode structId = ((StructNode) this.myType).getId();
        Sym sym = null;

        try {
            sym = symTable.lookupGlobal(structId.toString());
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        if (sym == null || !sym.getType().equals("struct-decl")) {
            ErrMsg.fatal(structId.getLineNum(), structId.getCharNum(),
                    "Name of struct type invalid");
            return false;
        }

        return true;
    }

    // 3 children
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NON_STRUCT if this is not a struct type

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

    public SymTab nameAnalyze(SymTab symTable) {
        LinkedList<String> paramTypes = myFormalsList.getTypeList();
        try {
            symTable.addDecl(this.myId.toString(), new FuncSym(myType.toString(), paramTypes));
        } catch (SymDuplicateException e) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                    "Identifier multiply-declared");
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        symTable.addScope();
        myFormalsList.nameAnalyze(symTable);
        myBody.nameAnalyze(symTable);

        try {
            symTable.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        }

        return symTable;
    }

    public IdNode getIdNode() {
        return this.myId;
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

    public SymTab nameAnalyze(SymTab symTable) {
        try {
            symTable.addDecl(this.myId.toString(), new Sym(this.myType.toString()));
        } catch (SymDuplicateException e) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                    "Identifier multiply-declared");
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        return symTable;
    }

    public String getTypeString() {
        return myType.toString();
    }

    public IdNode getIdNode() {
        return this.myId;
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

    public SymTab nameAnalyze(SymTab symTable) {
        Sym newSym = new Sym("struct-decl");

        try {
            symTable.addDecl(this.myId.toString(), newSym);
        } catch (SymDuplicateException e) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                    "Identifier multiply-declared");
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        mySymTable = new SymTab();
        myId.setStruct(this, newSym);
        myDeclList.nameAnalyze(symTable, mySymTable);
        return symTable;
    }

    public SymTab getSymTable() {
        return mySymTable;
    }

    // 2 children
    private IdNode myId;
    private DeclListNode myDeclList;
    private SymTab mySymTable;
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
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
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

    public IdNode getId() {
        return myId;
    }

    public String toString() {
        return myId.toString();
    }
}

// **********************************************************************
// **** StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    public abstract void nameAnalyze(SymTab symTable);
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

    public void nameAnalyze(SymTab symTable) {
        myAssign.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
        symTable.addScope();
        myDeclList.nameAnalyze(symTable);
        myStmtList.nameAnalyze(symTable);
        try {
            symTable.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
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

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
        symTable.addScope();
        myThenDeclList.nameAnalyze(symTable);
        myThenStmtList.nameAnalyze(symTable);
        try {
            symTable.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        symTable.addScope();
        myElseDeclList.nameAnalyze(symTable);
        myElseStmtList.nameAnalyze(symTable);

        try {
            symTable.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
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

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
        symTable.addScope();
        myDeclList.nameAnalyze(symTable);
        myStmtList.nameAnalyze(symTable);
        try {
            symTable.removeScope();
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
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

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        myCall.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        if (myExp != null) {
            myExp.nameAnalyze(symTable);
        }
    }

    // 1 child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// **** ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    public abstract void nameAnalyze(SymTab symTable);
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("TRUE");
    }

    public void nameAnalyze(SymTab symTable) {
    }

    private int myLineNum;
    private int myCharNum;
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

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (sym != null) {
            p.print("{" + sym.toString() + "}");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym sym;
    private StructDeclNode struct;

    public void setSym(Sym sym) {
        this.sym = sym;
    }

    public Sym getSym() {
        return sym;
    }

    public void setStruct(StructDeclNode struct, Sym sym) {
        this.struct = struct;
        sym.setStruct(struct);
    }

    public StructDeclNode getStruct() {
        return struct;
    }

    public int getLineNum() {
        return myLineNum;
    }

    public int getCharNum() {
        return myCharNum;
    }

    public String toString() {
        return myStrVal;
    }

    public void nameAnalyze(SymTab symTable) {
        try {
            this.sym = symTable.lookupGlobal(myStrVal);
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum,
                    "Identifier undeclared");
        } else {
            this.struct = sym.getStruct();
        }

        return;
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

    public void nameAnalyze(SymTab symTable) {
    }

    private int myLineNum;
    private int myCharNum;
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

    public void nameAnalyze(SymTab symTable) {
    }

    private int myLineNum;
    private int myCharNum;
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

    public void nameAnalyze(SymTab symTable) {
        myLoc.nameAnalyze(symTable);
        StructDeclNode lhs = this.getLStruct(symTable);
        if (lhs == null) {
            return;
        }
        SymTab leftTable = lhs.getSymTable();
        Sym foundItem = null;
        try {
            foundItem = leftTable.lookupGlobal(myId.toString());
        } catch (SymTabEmptyException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        if (foundItem == null) {
            ErrMsg.fatal(((IdNode) myId).getLineNum(),
                    ((IdNode) myId).getCharNum(), "Name of struct field invalid");
        } else {
            myId.setSym(foundItem);
        }

    }

    private StructDeclNode getLStruct(SymTab symTable) {
        if (myLoc instanceof IdNode) {
            Sym lookUpSym = null;

            try {
                lookUpSym = symTable.lookupGlobal(((IdNode) myLoc).toString());
            } catch (SymTabEmptyException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }

            if (lookUpSym == null) {
                return null;
            }

            if (lookUpSym.getStruct() == null) {
                ErrMsg.fatal(((IdNode) myLoc).getLineNum(),
                        ((IdNode) myLoc).getCharNum(),
                        "Colon-access of non-struct type");
                return null;
            }
            return ((IdNode) myLoc).getStruct();
        } else {
            StructDeclNode lhs = ((StructAccessExpNode) myLoc).getLStruct(symTable);
            if (lhs == null) {
                return null;
            }
            SymTab leftTable = lhs.getSymTable();
            Sym foundItem = null;
            try {
                foundItem = leftTable.lookupGlobal(((StructAccessExpNode) myLoc).myId.toString());
            } catch (SymTabEmptyException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }

            if (foundItem == null) {
                return null;
            } else {
                return foundItem.getStruct();
            }
        }
    }

    // 2 children
    private ExpNode myLoc;
    private IdNode myId;
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

    public void nameAnalyze(SymTab symTable) {
        myLhs.nameAnalyze(symTable);
        myExp.nameAnalyze(symTable);
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

    public void nameAnalyze(SymTab symTable) {
        myId.nameAnalyze(symTable);
        myExpList.nameAnalyze(symTable);
    }

    // 2 children
    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalyze(SymTab symTable) {
        myExp.nameAnalyze(symTable);
    }

    // 1 child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void nameAnalyze(SymTab symTable) {
        myExp1.nameAnalyze(symTable);
        myExp2.nameAnalyze(symTable);
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
