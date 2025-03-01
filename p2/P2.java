import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the bach scanner.
 * This version is set up to test all tokens, but more code is needed to test 
 * other aspects of the scanner (e.g., input that causes errors, character 
 * numbers, values associated with tokens)
 */
public class P2 {
    public static void main(String[] args) throws IOException {
                                           // exception may be thrown by yylex
        // test all tokens
        testAllTokens();
        CharNum.num = 1;
    
        // ADD CALLS TO OTHER TEST METHODS HERE
        testAllErrors();
        testCharAndLineNum();

    }

    /**
     * testCharAndLineNum
     *
     * Open and read from file charLine.in
     * For each token read, write the corresponding -
     * 
     * charNum
     * lineNum
     * 
     * to charLine.out. 
     * The goal of this method is to ensure the scanner properly
     * recognizes character and line numbers.
     * 
     * This means that using diff charLine.in charLine.out is not
     * meant to be the way to validate the scanner's behavior, unlike in
     * the testAllTokens method - and the charLine.out file should
     * be used instead.
     */
    private static void testCharAndLineNum() throws IOException {
		// open input and output files
		FileReader inFile = null;
		PrintWriter outFile = null;
		try {
			inFile = new FileReader("charLine.in");
			outFile = new PrintWriter(new FileWriter("charLine.out"));
		} catch (FileNotFoundException ex) {
			System.err.println("File charLine.in not found.");
			System.exit(-1);
		} catch (IOException ex) {
			System.err.println("charLine.out cannot be opened.");
			System.exit(-1);
		}
		// create and call the scanner
		Yylex scanner = new Yylex(inFile);
		Symbol token = scanner.next_token();
		while (token.sym != sym.EOF) {
			//print char number, line number, and the symbol number
			outFile.println(((TokenVal)token.value).charNum + "(" 
					+ ((TokenVal)token.value).lineNum + ")    sym # : " + token.sym);
			token = scanner.next_token();
		}
		outFile.close();

	}

    /**
     * testAllErrors
     *
     * Open and read from file errorTokens.in
     * For each token read, write the corresponding string to errorTokens.out
     * The goal of this method is to ensure the scanner properly prints
     * expected error messages for illegal cases such as -
     * 
     * illegal char
     * unterminated strings
     * integers that are too large
     * 
     * This means that using diff errorTokens.in errorTokens.out is not
     * meant to be the way to validate the scanner's behavior, unlike in
     * the testAllTokens method - and the terminal output should be
     * used instead.
     */
    private static void testAllErrors() throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("errorTokens.in");
            outFile = new PrintWriter(new FileWriter("errorTokens.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File errorTokens.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("errorTokens.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.BOOLEAN:
                outFile.println("boolean"); 
                break;
            case sym.INTEGER:
                outFile.println("integer");
                break;
            case sym.VOID:
                outFile.println("void");
                break;
            case sym.STRUCT:
                outFile.println("struct"); 
                break;
            case sym.IF:
                outFile.println("if");
                break;
            case sym.ELSE:
                outFile.println("else");
                break;
            case sym.WHILE:
                outFile.println("while");
                break;								
            case sym.INPUT:
                outFile.println("input"); 
                break;
            case sym.DISPLAY:
                outFile.println("disp");
                break;				
            case sym.RETURN:
                outFile.println("return");
                break;
            case sym.TRUE:
                outFile.println("TRUE"); 
                break;
            case sym.FALSE:
                outFile.println("FALSE"); 
                break;
            case sym.ID:
                outFile.println(((IdTokenVal)token.value).idVal);
                break;
            case sym.INTLIT:  
                outFile.println(((IntLitTokenVal)token.value).intVal);
                break;
            case sym.STRINGLIT: 
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;    
            case sym.LCURLY:
                outFile.println("{");
                break;
            case sym.RCURLY:
                outFile.println("}");
                break;
            case sym.LPAREN:
                outFile.println("(");
                break;
            case sym.RPAREN:
                outFile.println(")");
                break;
            case sym.LSQUARE:
                outFile.println("[");
                break;
            case sym.RSQUARE:
                outFile.println("]");
                break;
            case sym.COLON:
                outFile.println(":");
                break;
            case sym.COMMA:
                outFile.println(",");
                break;
            case sym.DOT:
                outFile.println(".");
                break;
            case sym.READOP:
                outFile.println("->");
                break;	
            case sym.WRITEOP:
                outFile.println("<-");
                break;			
            case sym.PLUSPLUS:
                outFile.println("++");
                break;
            case sym.MINUSMINUS:
                outFile.println("--");
                break;	
            case sym.PLUS:
                outFile.println("+");
                break;
            case sym.MINUS:
                outFile.println("-");
                break;
            case sym.TIMES:
                outFile.println("*");
                break;
            case sym.DIVIDE:
                outFile.println("/");
                break;
            case sym.NOT:
                outFile.println("^");
                break;
            case sym.AND:
                outFile.println("&");
                break;
            case sym.OR:
                outFile.println("|");
                break;
            case sym.EQUALS:
                outFile.println("==");
                break;
            case sym.NOTEQ:
                outFile.println("^=");
                break;
            case sym.LESS:
                outFile.println("<");
                break;
            case sym.GREATER:
                outFile.println(">");
                break;
            case sym.LESSEQ:
                outFile.println("<=");
                break;
            case sym.GREATEREQ:
                outFile.println(">=");
                break;
            case sym.ASSIGN:
                outFile.println("=");
                break;
            default:
                outFile.println("!!! UNKNOWN TOKEN !!!");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }

    /**
     * testAllTokens
     *
     * Open and read from file allTokens.in
     * For each token read, write the corresponding string to allTokens.out
     * If the input file contains all tokens, one per line, we can verify
     * correctness of the scanner by comparing the input and output files
     * (e.g., using a 'diff' command).
     */
    private static void testAllTokens() throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("allTokens.in");
            outFile = new PrintWriter(new FileWriter("allTokens.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File allTokens.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("allTokens.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.BOOLEAN:
                outFile.println("boolean"); 
                break;
            case sym.INTEGER:
                outFile.println("integer");
                break;
            case sym.VOID:
                outFile.println("void");
                break;
            case sym.STRUCT:
                outFile.println("struct"); 
                break;
            case sym.IF:
                outFile.println("if");
                break;
            case sym.ELSE:
                outFile.println("else");
                break;
            case sym.WHILE:
                outFile.println("while");
                break;								
            case sym.INPUT:
                outFile.println("input"); 
                break;
            case sym.DISPLAY:
                outFile.println("disp");
                break;				
            case sym.RETURN:
                outFile.println("return");
                break;
            case sym.TRUE:
                outFile.println("TRUE"); 
                break;
            case sym.FALSE:
                outFile.println("FALSE"); 
                break;
            case sym.ID:
                outFile.println(((IdTokenVal)token.value).idVal);
                break;
            case sym.INTLIT:  
                outFile.println(((IntLitTokenVal)token.value).intVal);
                break;
            case sym.STRINGLIT: 
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;    
            case sym.LCURLY:
                outFile.println("{");
                break;
            case sym.RCURLY:
                outFile.println("}");
                break;
            case sym.LPAREN:
                outFile.println("(");
                break;
            case sym.RPAREN:
                outFile.println(")");
                break;
            case sym.LSQUARE:
                outFile.println("[");
                break;
            case sym.RSQUARE:
                outFile.println("]");
                break;
            case sym.COLON:
                outFile.println(":");
                break;
            case sym.COMMA:
                outFile.println(",");
                break;
            case sym.DOT:
                outFile.println(".");
                break;
            case sym.READOP:
                outFile.println("->");
                break;	
            case sym.WRITEOP:
                outFile.println("<-");
                break;			
            case sym.PLUSPLUS:
                outFile.println("++");
                break;
            case sym.MINUSMINUS:
                outFile.println("--");
                break;	
            case sym.PLUS:
                outFile.println("+");
                break;
            case sym.MINUS:
                outFile.println("-");
                break;
            case sym.TIMES:
                outFile.println("*");
                break;
            case sym.DIVIDE:
                outFile.println("/");
                break;
            case sym.NOT:
                outFile.println("^");
                break;
            case sym.AND:
                outFile.println("&");
                break;
            case sym.OR:
                outFile.println("|");
                break;
            case sym.EQUALS:
                outFile.println("==");
                break;
            case sym.NOTEQ:
                outFile.println("^=");
                break;
            case sym.LESS:
                outFile.println("<");
                break;
            case sym.GREATER:
                outFile.println(">");
                break;
            case sym.LESSEQ:
                outFile.println("<=");
                break;
            case sym.GREATEREQ:
                outFile.println(">=");
                break;
            case sym.ASSIGN:
                outFile.println("=");
                break;
            default:
                outFile.println("!!! UNKNOWN TOKEN !!!");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }
}
