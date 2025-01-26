run : P1.class
	java P1

P1.class : SymTab.class
	javac -cp . P1.java

SymTab.class : Sym.class SymDuplicateException.class SymTabEmptyException.class
	javac -cp . SymTab.java

Sym.class : 
	javac Sym.java

SymDuplicateException.class :
	javac SymDuplicateException.java

SymTabEmptyException.class :
	javac SymTabEmptyException.java

clean :
	rm *.class
