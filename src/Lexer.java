import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class Lexer {

    private int lineNum;
    private int charPosition;
    private StringHandler handler; // any access to our file will be made through this StringHandler
    private HashMap<String, Token.TokenType> keywords;
    private HashMap<String, Token.TokenType> twoCharacterSymbols;
    private HashMap<String, Token.TokenType> singleCharacterSymbols;

    public Lexer(String data) {
        this.handler = new StringHandler(data);
        this.lineNum = 1;
        this.charPosition = -1; // sets to -1 so that when we peek at the next, it will be the 0th character
        keywords = new HashMap<>();
        twoCharacterSymbols = new HashMap<>();
        singleCharacterSymbols = new HashMap<>();
        populateWithKeyWords();
        populateWithSymbols();
    }

    /**
     * the method peeks into the next character while the file is not done, then decides what to do with it.
     * @return
     * @throws Exception
     */
    public LinkedList<Token> Lex() throws Exception{
        LinkedList<Token> tokens = new LinkedList<Token>();
        do{
            char next = handler.peek(1);
            if(next == ' ' || next == '\t'){
                charPosition++;
                handler.Swallow(1);
            }
            else if(next == '\n' || next == ';'){
                Token temp = new Token(Token.TokenType.SEPARATOR, lineNum, charPosition);
                tokens.add(temp);
                lineNum++;
                charPosition = -1;
                handler.Swallow(1);
            }
            else if(next == '\r'){
                handler.Swallow(1);
            }
            else if(Character.isLetter(next)){
                char temp = handler.GetChar();
                charPosition++;
                tokens.add(processWord(temp, charPosition));
            }
            else if(Character.isDigit(next)){
                char temp = handler.GetChar();
                charPosition++;
                tokens.add(processDigit(temp, charPosition));
            }
            else if(next == '.'){
                String temp = String.valueOf(handler.GetChar());
                charPosition++;
                tokens.add(processDecimal(temp, charPosition));
            }
            else if(next == '#'){
                while(!handler.IsDone() && handler.peek(1) != '\n'){
                    handler.GetChar();
//                    charPosition++;
                }
            }
            else if(next == '"'){
                String temp = String.valueOf(handler.GetChar());
                charPosition++;
                tokens.add(handleStringLiteral(charPosition));
            }
            else if(next == '`'){
                char temp = handler.GetChar();
                charPosition++;
                tokens.add(handlePattern(charPosition));
            }
            else{
                Token symbol = processSymbol();
                if(symbol == null){
                    //what i could also do here is make a new token and print the exact char position error is at
                    throw new Exception("INVALID CHARACTER at line " + lineNum + " position " + (charPosition + 1) + " :'(");
                }
                else{
                    tokens.add(symbol);
                }
            }
        }
        while(!handler.IsDone());
        tokens.add(new Token(Token.TokenType.SEPARATOR, lineNum, charPosition));
        return tokens;

    }

    /**
     *peeks into the next character while the file is not done, and utilizes StringBuilder to append each time
     * the next character is valid. Uses getChar instead of peek to ensure that the StringBuilder index is updated
     * properly.
     * @param temp the first letter in the word
     * @param position the position the word is at
     * @return returns the token of the word
     */
    public Token processWord(char temp, int position){
        StringBuilder myStr = new StringBuilder(String.valueOf(temp));
        while(!handler.IsDone() && (Character.isLetter(handler.peek(1)) || Character.isDigit(handler.peek(1))
        || handler.peek(1) == '_')){
            myStr.append(handler.GetChar());
            charPosition++;
        }
        if(keywords.containsKey(myStr.toString())){
            Token.TokenType tempToken = keywords.get(myStr.toString());
            return new Token(tempToken, lineNum, position);
        }
        return new Token(Token.TokenType.WORD, lineNum, position, myStr.toString());
    }

    /**
     * the idea is the same as processToken, except that it also checks if the input is a period. if it is,
     * then it calls processDecimal, the idea is to switch to the next state.
     * @param digit
     * @param position
     * @return
     */
    public Token processDigit(char digit, int position) throws Exception {
        StringBuilder myStr = new StringBuilder(String.valueOf(digit));
        while(!handler.IsDone() && (Character.isDigit(handler.peek(1)) || handler.peek(1) == '.')){
            char temp = handler.GetChar();
            charPosition++;
            myStr.append(temp);
            if(temp == '.'){
                return processDecimal(myStr.toString(), position);
            }

        }
        return new Token(Token.TokenType.NUMBER, lineNum, position, myStr.toString());
    }


    /**
     * the idea is similar to the other process methods. the point of this method is so that it
     * does NOT accept any more periods. if another period is seen, it exits and goes back to the original
     * loop/state
     * @param curStr
     * @param position
     * @return
     */
    public Token processDecimal(String curStr, int position) throws Exception {
        StringBuilder myStr = new StringBuilder(curStr);
        while(!handler.IsDone() && Character.isDigit(handler.peek(1))){
            myStr.append(handler.GetChar());
            charPosition++;
        }
        if(!handler.IsDone() && handler.peek(1) == '.'){
            throw new Exception("Too many decimals! - line " + lineNum + " position " + (charPosition + 1) + " ^_^");
        }
        return new Token(Token.TokenType.NUMBER, lineNum, position, myStr.toString());
    }

    /**
     * looks at the next two characters and looks them up in the two character symbol
     * hashmap for a match. if there is a match it makes a new token of that tokentype and
     * returns it, else looks in the one chracter hashmap. if there is still no match
     * it returns null so that the Lex method knows to throw an exception
     * @return
     * @throws Exception
     */
    public Token processSymbol() throws Exception{
        StringBuilder myStr = new StringBuilder();
        String temp = handler.PeekString(2);
        if(temp != null) {
            if (twoCharacterSymbols.containsKey(temp)) {
                Token.TokenType tempTokentype = twoCharacterSymbols.get(temp);
                handler.Swallow(2);
                charPosition += 2;
                return new Token(tempTokentype, lineNum, charPosition);
            }
        }
        if(singleCharacterSymbols.containsKey(handler.PeekString(1))){
            Token.TokenType tempTokentype = singleCharacterSymbols.get(handler.PeekString(1));
            handler.Swallow(1);
            charPosition++;
            return new Token(tempTokentype, lineNum, charPosition);
        }
        else
            return null;
    }

    /**
     * keeps adding characters to the stringbuilder until it reaches the next quotation.
     * if there is a escaped quotation - marked by a backslash - it checks if there is a
     * quotation there so it could add that to the stringbuilder without quitting the loop.
     * @param position the position before the first character
     * @return
     * @throws Exception
     */
    private Token handleStringLiteral(int position) throws Exception {
        StringBuilder myStr = new StringBuilder();
        while(!handler.IsDone() && !(handler.peek(1) == '"')){
            char temp = handler.GetChar();
            if(temp == '\\'){
                if(handler.peek(1) == '"') {
                    handler.Swallow(1);
                    myStr.append('"');
                }
            }
            else
                myStr.append(temp);
        }
//        myStr.append('"');
        charPosition++;
        handler.Swallow(1);
        return new Token(Token.TokenType.STRINGLITERAL, lineNum, position, myStr.toString());
    }

    /**
     * does the exact same thing as handleStringLiteral, except it works for backtics
     * - represented by ` - instead. this is used for AWK's REGEX implementation(or
     * u can say our version of AWK's regex implementation;) )
     * @param position
     * @return
     * @throws Exception
     */
    private Token handlePattern(int position) throws Exception {
        StringBuilder myStr = new StringBuilder();
        while(!handler.IsDone() && !(handler.peek(1) == '`')){
            char temp = handler.GetChar();
            if(temp == '\\'){
                if(handler.peek(1) == '`') {
                    handler.Swallow(1);
                    myStr.append('`');
                }
            }
            else
                myStr.append(temp);
        }
//        myStr.append('`');
        charPosition++;
        handler.Swallow(1);
        return new Token(Token.TokenType.BACKTICK, lineNum, position, myStr.toString());
    }

    /**
     * fills up the proper linked list with the keywords. we use this so we can compare
     * any string we get into the hashmap and check if its a keyword easily.
     */
    public void populateWithKeyWords(){
        keywords.put("while", Token.TokenType.WHILE);
        keywords.put("if", Token.TokenType.IF);
        keywords.put("do", Token.TokenType.DO);
        keywords.put("for", Token.TokenType.FOR);
        keywords.put("break", Token.TokenType.BREAK);
        keywords.put("continue", Token.TokenType.CONTINUE);
        keywords.put("else", Token.TokenType.ELSE);
        keywords.put("return", Token.TokenType.RETURN);
        keywords.put("BEGIN", Token.TokenType.BEGIN);
        keywords.put("END", Token.TokenType.END);
        keywords.put("print", Token.TokenType.PRINT);
        keywords.put("printf", Token.TokenType.PRINTF);
        keywords.put("next", Token.TokenType.NEXT);
        keywords.put("in", Token.TokenType.IN);
        keywords.put("delete", Token.TokenType.DELETE);
        keywords.put("getline", Token.TokenType.GETLINE);
        keywords.put("exit", Token.TokenType.EXIT);
        keywords.put("nextfile", Token.TokenType.NEXTFILE);
        keywords.put("function", Token.TokenType.FUNCTION);
//        Token.TokenType[] enums = Token.TokenType.values();
//        for(int i = 0; i < enums.length; i++){
//            if(enums[i].toString().equals("WORD") || enums[i].toString().equals("NUMBER") ||
//                    enums[i].toString().equals("SEPARATOR") || !(enums[i].toString().matches("[a-zA-Z]*"))){
//            }
//            else{
//                this.keywords.put(enums[i].toString(), enums[i]);
//            }
//        }
    }

    /**
     * same idea as the populateWithKeywords(). we have two hashmaps for symbols
     * so we are not confused easily when characters are the same. for example,
     * > is itself a symbol, but >= is also a symbol. when we have two separate hashmaps
     * its easier to compare
     */
    public void populateWithSymbols() {
        twoCharacterSymbols.put(">=", Token.TokenType.GREATEREQUAL);
        twoCharacterSymbols.put("++", Token.TokenType.INCREMENT);
        twoCharacterSymbols.put("--", Token.TokenType.DECREMENT);
        twoCharacterSymbols.put("<=", Token.TokenType.LESSTHANEQUAL);
        twoCharacterSymbols.put("==", Token.TokenType.EQUALEQUAL);
        twoCharacterSymbols.put("!=", Token.TokenType.NOTEQUAL);
        twoCharacterSymbols.put("^=", Token.TokenType.EXPONENTEQUALS);
        twoCharacterSymbols.put("%=", Token.TokenType.MODULOEQUALS);
        twoCharacterSymbols.put("*=", Token.TokenType.MULTIPLICATIONEQUALS);
        twoCharacterSymbols.put("/=", Token.TokenType.DIVISIONEQUALS);
        twoCharacterSymbols.put("+=", Token.TokenType.PLUSEQUALS);
        twoCharacterSymbols.put("-=", Token.TokenType.MINUESEQUALS);
        twoCharacterSymbols.put("!~", Token.TokenType.NOTMATCH);
        twoCharacterSymbols.put("&&", Token.TokenType.ANDAND);
        twoCharacterSymbols.put(">>", Token.TokenType.SIGNEDRIGHTSHIFT);
        twoCharacterSymbols.put("||", Token.TokenType.LOGICALOR);


        singleCharacterSymbols.put("{", Token.TokenType.LEFTCURLYBRACE);
        singleCharacterSymbols.put("}", Token.TokenType.RIGHTCURLYBRACE);
        singleCharacterSymbols.put("[", Token.TokenType.LEFTBRACKET);
        singleCharacterSymbols.put("]", Token.TokenType.RIGHTBRACKET);
        singleCharacterSymbols.put("(", Token.TokenType.LEFTPARENTHESES);
        singleCharacterSymbols.put(")", Token.TokenType.RIGHTPARENTHESES);
        singleCharacterSymbols.put("$", Token.TokenType.DOLLARSIGN);
        singleCharacterSymbols.put("~", Token.TokenType.TILDE);
        singleCharacterSymbols.put("=", Token.TokenType.EQUALS);
        singleCharacterSymbols.put("<", Token.TokenType.LESSTHAN);
        singleCharacterSymbols.put(">", Token.TokenType.GREATERTHAN);
        singleCharacterSymbols.put("!", Token.TokenType.LOGICALNOT);
        singleCharacterSymbols.put("+", Token.TokenType.PLUS);
        singleCharacterSymbols.put("^", Token.TokenType.EXPONENT);
        singleCharacterSymbols.put("-", Token.TokenType.MINUS);
        singleCharacterSymbols.put("?", Token.TokenType.TERNARYOPERATOR);
        singleCharacterSymbols.put(":", Token.TokenType.COLON);
        singleCharacterSymbols.put("*", Token.TokenType.MUTIPLICATIONSYMBOL);
        singleCharacterSymbols.put("/", Token.TokenType.DIVISIONSYMBOL);
        singleCharacterSymbols.put("%", Token.TokenType.MODULO);
        singleCharacterSymbols.put(";", Token.TokenType.SEPARATOR);
        singleCharacterSymbols.put("\n", Token.TokenType.CARRIAGERETURN);
        singleCharacterSymbols.put("|", Token.TokenType.PIPE);
        singleCharacterSymbols.put(",", Token.TokenType.COMMA);
    }
}
