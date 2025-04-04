import java.util.LinkedList;

public class Sym {
	private String type;
	private SymTab symTable;
	private StructDeclNode struct;

	public Sym(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String toString() {
		return type;
	}

	public StructDeclNode getStruct() {
		return this.struct;
	}

	public void setStruct(StructDeclNode struct) {
		this.struct = struct;
	}

	public SymTab getSymTable(SymTab symTable) {
		return this.symTable;
	}

	public void setSymTable(SymTab symTable) {
		this.symTable = symTable;
	}
}

class FuncSym extends Sym {
	private LinkedList<String> paramTypes;
	private String returnType;

	public FuncSym(String returnType, LinkedList<String> paramTypes) {
		super("function");
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}

	public String toString() {
		String params = String.join(", ", paramTypes);
		if (params.equals("")) {
			params = "void";
		}
		return params + " -> " + returnType;
	}
}