public class Token {
    enum TokenType{
        WORD,NUMBER,SEPARATOR,

        WHILE,IF,DO,FOR,BREAK,CONTINUE,ELSE,RETURN,
        BEGIN,END,PRINT,PRINTF,NEXT,IN,DELETE,GETLINE,EXIT,NEXTFILE,FUNCTION,

        STRINGLITERAL,BACKTICK,

        GREATEREQUAL,INCREMENT,DECREMENT,LESSTHANEQUAL,EQUALEQUAL,NOTEQUAL,EXPONENTEQUALS,MODULOEQUALS,
        MULTIPLICATIONEQUALS,DIVISIONEQUALS,PLUSEQUALS,MINUESEQUALS,NOTMATCH,ANDAND,SIGNEDRIGHTSHIFT,LOGICALOR,

        LEFTCURLYBRACE, RIGHTCURLYBRACE, LEFTBRACKET, RIGHTBRACKET,LEFTPARENTHESES,RIGHTPARENTHESES,
        DOLLARSIGN,TILDE,EQUALS,LESSTHAN,GREATERTHAN,LOGICALNOT,PLUS,EXPONENT,MINUS,TERNARYOPERATOR,
        COLON,MUTIPLICATIONSYMBOL,DIVISIONSYMBOL,MODULO,CARRIAGERETURN,PIPE,COMMA
    }

    private TokenType type;
    private String data;
    private int lineNumber;
    private int charPosition;

    /**
     * constructor for making a Token that does not have any data in it(for now, were going to use this
     * for separator tokens
     * @param type
     * @param lineNum
     * @param charPos
     */
    public Token(TokenType type, int lineNum, int charPos){
        this.type = type;
        this.lineNumber = lineNum;
        this.charPosition = charPos;
    }

    /**
     * constructor for tokens that have data inside them, like words and numbers
     * @param type
     * @param lineNum
     * @param charPos
     * @param value
     */
    public Token(TokenType type, int lineNum, int charPos, String value){
        this.type = type;
        this.lineNumber = lineNum;
        this.charPosition = charPos;
        this.data = value;
    }

    /**
     * prints out the information of a Token, and prints out the data if it has any
     * @return
     */
    @Override
    public String toString() {
        if(data != null){
            return type.toString() + " (" + data + ")";
        }
        else{
            return type.toString();
        }
    }

    //use in my testing to check the tokenType
    public Token.TokenType getTokenType(){
        return type;
    }

    //use in my testing to check if the value is what is expected
    public String getValue(){
        return data;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getCharPosition() {
        return charPosition;
    }
}
