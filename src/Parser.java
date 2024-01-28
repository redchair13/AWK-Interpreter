import java.rmi.server.ExportException;
import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    private TokenManager handler;

    public Parser(LinkedList<Token> tokens){
        this.handler = new TokenManager(tokens);
    }

    /**
     * very important when parsing - we want to make sure theres at least one semicolon or new line, but any number of
     * them are valid. returns false if it doesnt find a single Seperator.
     * @return
     */
    boolean acceptSeperators(){
        boolean foundSeperators = false;
        while(!(handler.MatchAndRemove(Token.TokenType.SEPARATOR).equals(Optional.empty()))){
            foundSeperators = true;
        }
        return foundSeperators;
    }

    ProgramNode Parse() throws Exception {
        ProgramNode program = new ProgramNode(new LinkedList<BlockNode>(), new LinkedList<BlockNode>(),
                new LinkedList<BlockNode>(), new LinkedList<FunctionDefinitionNode>());
        while(handler.MoreTokens()){
            if(!ParseFunction(program)){
                if(!ParseAction(program)){
                    throw new Exception("unknown characters (-_(-_(-_(-_-)_-)_-)_-)"); // angry mob of faces
                }
            }
            acceptSeperators(); // theres always a seperator at the end of the program
        }
        return program;
    }

    /**
     *tests if the next token is a keyword - function. if it is, then that means it is a function definition node.
     * the rest of the tokens must follow the grammer - has a name, right and left parentheses, and maybe some paramaters,
     * and then a block of statements, which we will parse. if it goes out of line, we will kill it with an exception
     * @param program
     * @return
     * @throws Exception if the code has a single thing thats out of place for the grammar
     */
    boolean ParseFunction(ProgramNode program) throws Exception {
        if(handler.MoreTokens()){
            LinkedList<String> params = new LinkedList<>();
            String name;
            LinkedList<StatementNode> statements;
            if(handler.MatchAndRemove(Token.TokenType.FUNCTION).isEmpty()){
                return false;
            }
            if((name = handler.MatchAndRemove(Token.TokenType.WORD).get().getValue()).isEmpty()){
                throw new Exception();
            }
            if(handler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES).isEmpty()) {
                throw new Exception();
            }

            while((handler.MoreTokens()) && handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isEmpty()) {
                acceptSeperators();
                params.add(parseParameters());
                acceptSeperators();
            }
            acceptSeperators();
            if(!(handler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTCURLYBRACE))){
                throw new Exception("no curly brace seen after function definition");
            }
            BlockNode thisBlock = parseBlock().get();
            statements = thisBlock.getStatements();

            program.addFunctionDefs(new FunctionDefinitionNode(name, params, statements));
            }
        return true;

        }

    /**
     * AWK supports 4 types of actions. the first token will decide whether its a BEGIN block, END block, or if its
     * just a normal block. if its a normal block, we will use ParseOperation to determine if it has a condition or not.
     * for all of them, after we determine what type of block it is, we just use parseBlock to get all of the statements.
     * @param program
     * @return
     */
    boolean ParseAction(ProgramNode program) throws Exception {
        if(handler.MoreTokens()){
            if(handler.MatchAndRemove(Token.TokenType.BEGIN).isPresent()){
                program.addBeginBlock(parseBlock().get());
            }
            else if(handler.MatchAndRemove(Token.TokenType.END).isPresent()){
                program.addEndBlock(parseBlock().get());
            }
            else{
//                program.addBlock(new BlockNode(ParseOperation().get(), parseBlock().getStatements()));
                Optional<Node> condition = ParseOperation();
                acceptSeperators();
                Optional<BlockNode> block = parseBlock();
                if(block.isPresent()){
                    program.addBlock(new BlockNode(condition, block.get().getStatements()));
                }
                else throw new Exception("No block present");
            }
        }
        return true;
    }

    /**
     * I use this so I can every single paramater by itself. since each paramater is seperated by a comma, I just
     * loop until I see a comma(or a right parantheses indicating that the paramaters are done), and return that singular
     * paramater.
     * @return
     */
    public String parseParameters() {
    StringBuilder param = new StringBuilder();
        while ((handler.MoreTokens()) && handler.MatchAndRemove(Token.TokenType.COMMA).isEmpty() &&
                (handler.Peek(0).get().getTokenType() != Token.TokenType.RIGHTPARENTHESES)) {
            acceptSeperators();
            Optional<Token> temp = handler.MatchAndRemove(handler.Peek(0).get().getTokenType());
            param.append(temp.get().getValue());
            acceptSeperators();
        }
        return param.toString();
    }

    /**
     * parseBlock will add every single statement into a linked list and add them into a block,
     * then return that block.
     * deals with both multi line blocks and single line blocks by checking for the first curly brace
     * @return
     */
    public Optional<BlockNode> parseBlock() throws Exception {
        BlockNode thisBlock = new BlockNode(Optional.empty(), new LinkedList<>());
        acceptSeperators();
        if(handler.MatchAndRemove(Token.TokenType.LEFTCURLYBRACE).isPresent()){
            while(handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.RIGHTCURLYBRACE).isEmpty()){
                acceptSeperators();
                thisBlock.addStatement(parseStatement());
                acceptSeperators();
            }
        }
        else{
            acceptSeperators();
            if(handler.MoreTokens()){
                thisBlock.addStatement(parseStatement());
                // parse statement is goign to call parse operation at its end
                // parse operation will call parse function call
                // as it goes back up parse operation, it will add concat since we removed the separator

                // the fix seems to be not to remove the separator for function calls - like print -
                // and instead peek, and then remove the separator here
                acceptSeperators();
            }
            else return Optional.empty();
        }
        acceptSeperators();
        return Optional.of(thisBlock);
    }

    /**
     * actions in awk can have a condition, if theyd like. we check to see if it has a condition in the form
     * of an expression. if it does, we will return it, else we will return Optional.empty
     * it will check the condition by checking the expression precedence chart Bottom up so order of operations is met
     * @return
     */
    public Optional<Node> ParseOperation() throws Exception {
        return parseAssignment();
    }

    /**
     * we use this when we are parsing expressions because there is different precedence for different operations.
     * we check for certain ones,and if they dont exist, we go to the next precedence by calling ParseLValue.
     * @return
     * @throws Exception if there are missing elements, like a bracket
     */
    Optional<Node> ParseBottomLevel() throws Exception {
        Optional<Token> temp;
        Optional<Node> tempNode;
        if((temp = handler.MatchAndRemove(Token.TokenType.STRINGLITERAL)).isPresent()){
            return Optional.of(new ConstantNode(temp.get().getValue()));
        }
        else if((temp = handler.MatchAndRemove(Token.TokenType.NUMBER)).isPresent()){
            return Optional.of(new ConstantNode(temp.get().getValue()));
        }
        else if((temp = handler.MatchAndRemove(Token.TokenType.BACKTICK)).isPresent()){
            return Optional.of(new PatternNode(temp.get().getValue()));
        }
        else if(handler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES).isPresent()){
            tempNode = ParseOperation();
            if(handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isPresent()){
                return tempNode;
            }
            else throw new Exception("no right parentheses found at line " + handler.Peek(0).get().getLineNumber() + " and char number " + handler.Peek(0).get().getCharPosition());
        }
        else if(handler.MatchAndRemove(Token.TokenType.LOGICALNOT).isPresent()){
            if((tempNode = ParseOperation()).isPresent()){
                return Optional.of(new OperationNode(Optional.empty(), OperationNode.possibleOps.NOT, tempNode));
            }
            else throw new Exception("no operation found");
        }
        else if(handler.MatchAndRemove(Token.TokenType.MINUS).isPresent()){
            if((tempNode = ParseOperation()).isPresent()){
                return Optional.of(new OperationNode(Optional.empty(), OperationNode.possibleOps.UNARYNEG, tempNode));
            }
            else throw new Exception("no operation found");
        }
        else if(handler.MatchAndRemove(Token.TokenType.PLUS).isPresent()){
            if((tempNode = ParseOperation()).isPresent()){
                return Optional.of(new OperationNode(Optional.empty(), OperationNode.possibleOps.UNARYPOS, tempNode));
            }
            else throw new Exception("no operation found");
        }
        else if(handler.MatchAndRemove(Token.TokenType.INCREMENT).isPresent()){
            if((tempNode = ParseOperation()).isPresent()){
                // were making all of our increment return assignment nodes now, and making operation
                // node NOT extend from statement node
                return Optional.of(new AssignmentNode(tempNode.get(), new OperationNode(Optional.empty(), OperationNode.possibleOps.PREINC, tempNode)));
            }
            else throw new Exception("no operation found");
        }
        else if(handler.MatchAndRemove(Token.TokenType.DECREMENT).isPresent()){
            if((tempNode = ParseOperation()).isPresent()){
                return Optional.of(new AssignmentNode(tempNode.get(), new OperationNode(Optional.empty(), OperationNode.possibleOps.PREDEC, tempNode)));
            }
            else throw new Exception("no operation found");
        }
        else{
            tempNode = parseFunctionCall();
            if(tempNode.isPresent()){
                return tempNode;
            }
            else return ParseLValue();
        }
//        else if(handler.Peek(0).get().getTokenType().equals(Token.TokenType.WORD)){
//            if(handler.Peek(1).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)){
//                return parseFunctionCall();
//            }
//        }
//        return ParseLValue();
    }

    /**
     * returns a node for the left value. checks if there is a dollar sign pointing to avariable, in which case it returns
     * a operation node for it. Does the same for parantheses. the purpose of this is to serve as a good recursive way for when
     * parseBottomLevel doesnt find a match. this is the true "bottom level" - the first section on the precedence chart
     * @return
     * @throws Exception if there is a missing bracket
     */
    Optional<Node> ParseLValue() throws Exception {
        if(handler.MatchAndRemove(Token.TokenType.DOLLARSIGN).isPresent()){
            Optional<Node> temp = ParseBottomLevel();
//            Optional<Node> temp = ParseOperation();
            OperationNode tempOp = new OperationNode(Optional.empty(), OperationNode.possibleOps.DOLLAR, temp);
            return Optional.of(tempOp);
        }
        else if(handler.Peek(0).get().getTokenType().equals(Token.TokenType.WORD)){
            String word = handler.MatchAndRemove(Token.TokenType.WORD).get().getValue();
            if(handler.MatchAndRemove(Token.TokenType.LEFTBRACKET).isPresent()){
                Optional<Node> temp = ParseOperation();
                if(handler.MatchAndRemove(Token.TokenType.RIGHTBRACKET).isPresent()){
                    if(temp.isPresent()){
                        VariableReferenceNode var = new VariableReferenceNode(word, temp);
                        return Optional.of(var);
                    }else throw new Exception("nothing in the brackets");
                }
                else throw new Exception("No right bracket found");
            }
            else{
                VariableReferenceNode var = new VariableReferenceNode(word, Optional.empty());
                return Optional.of(var);
            }
        }
        else{
            return Optional.empty();
        }
    }

    /**
     * calls up the precedence chart by first calling parseTernary - which in turn calls the rest.
     * since this is right associative, it will recursively call itself until there are no more assignment tokens
     * @return
     * @throws Exception if there are missing elements, like a bracket
     */
    Optional<Node> parseAssignment() throws Exception {
        Optional<Node> left = parseTernary();
        Optional<Node> right;
        while(handler.MoreTokens()){
            if (handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.EQUALS).isPresent()){
                right = parseAssignment();
                left = Optional.of(new AssignmentNode(left.get(), right.get()));
            }
            else if(handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.EXPONENTEQUALS).isPresent()){
                right = parseAssignment();
                if(right.isPresent()){
                    left = Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.EXPONENT, right)));
                }
                else throw new Exception("No expression after exponent equals");
            }
            else if(handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.MODULOEQUALS).isPresent()){
                right = parseAssignment();
                if(right.isPresent()){
                    left = Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.MODULO, right)));
                }
                else throw new Exception("No expression after modulo equals");
            }
            else if(handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.MULTIPLICATIONEQUALS).isPresent()){
                right = parseAssignment();
                if(right.isPresent()){
                    left = Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.MULTIPLY, right)));
                }
                else throw new Exception("No expression after multiplication equals");
            }
            else if(handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.DIVISIONEQUALS).isPresent()){
                right = parseAssignment();
                if(right.isPresent()){
                    left = Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.DIVIDE, right)));
                }
                else throw new Exception("No expression after division equals");
            }
            else if(handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.PLUSEQUALS).isPresent()){
                right = parseAssignment();
                if(right.isPresent()){
                    left = Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.ADD, right)));
                }
                else throw new Exception("No expression after plus equals");
            }
            else if(handler.MoreTokens() && handler.MatchAndRemove(Token.TokenType.MINUESEQUALS).isPresent()){
                right = parseAssignment();
                if(right.isPresent()){
                    left = Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.SUBTRACT, right)));
                }
                else throw new Exception("No expression after minus equals");
            }
            else return left;
        }
        return left;
    }

    /**
     * firstly, calls the next level up to keep order of Operations. it is associative, meaning we can
     * have multiple nested ternary expressions inside of one - with right association. thats why for each expression, it
     * calls itself recursively instead of calling the next level up.
     * @return
     * @throws Exception if there is nothing after the ? or colon
     */
    Optional<Node> parseTernary() throws Exception {
        Optional<Node> temp = parseLogicalOr();
        if(handler.MatchAndRemove(Token.TokenType.TERNARYOPERATOR).isPresent()){
            Optional<Node> temp2 = parseTernary();
            if(temp2.isPresent()){
                if(handler.MatchAndRemove(Token.TokenType.COLON).isPresent()){
                    Optional<Node> temp3 = parseTernary();
                    if(temp3.isPresent()){
                        return Optional.of(new TernaryNode(temp.get(), temp2.get(), temp3.get()));
                    }
                    else throw new Exception("No third value specified");
                }
                else throw new Exception("no third value");
            } else throw new Exception("no expression after ternary operator");
        }
        else return temp;
    }


    /**
     * firstly, calls the up the precedence chart to get the first value. if there is a logical or,
     * it recursively calls itself so that it can nest logical or expressions to the right,
     * then returns a new node with those values.
     * If there are no Ands it just returns the original value that came from above the chart
     * @return
     * @throws Exception if there is nothing after the ||
     */
    Optional<Node> parseLogicalOr() throws Exception {
        Optional<Node> left = parseLogicalAnd();
        while(handler.MoreTokens()){
            if(handler.MatchAndRemove(Token.TokenType.LOGICALOR).isEmpty()) {
                return left;
            }
                Optional<Node> right = parseLogicalAnd();
                if(right.isPresent()){
                    left = Optional.of(new OperationNode(left, OperationNode.possibleOps.OR, right));
                }
                else throw new Exception("No expression after or");
            }
        return left;
        }
    /**
     * firstly, calls the up the precedence chart to get the first value. if there is a logical and,
     * it recursively calls itself so that it can nest logical and expressions to the right, then returns
     * a new node with those values. If there are no Ands it just returns the original value that came from above the chart
     * @return
     * @throws Exception  if there is nothing after the AND
     */
    Optional<Node> parseLogicalAnd() throws Exception{
        Optional<Node> temp = parseArray();
        while(handler.MoreTokens()){
            if(handler.MatchAndRemove(Token.TokenType.ANDAND).isEmpty()) {
                return temp;
            }
            Optional<Node> temp2 = parseArray(); //
            if(temp2.isPresent()){
                temp = Optional.of(new OperationNode(temp, OperationNode.possibleOps.AND, temp2));
            }
            else throw new Exception("No expression after or");
        }
        return temp;
    }

    /**
     * calls up the chart, and checks for the IN keyword after. Keeps calling the next level up after the IN keyword to
     * get the next value while being left associative
     * @return
     * @throws Exception if there is nothing after the IN keyword
     */
    Optional<Node> parseArray() throws Exception{
        Optional<Node> temp = parseMatch();
        while(handler.MoreTokens()){
            if(handler.MatchAndRemove(Token.TokenType.IN).isPresent()){
                Optional<Node> right = ParseLValue();
                if(right.isPresent()){
                    temp = Optional.of(new OperationNode(temp, OperationNode.possibleOps.IN, right));
                }
                else throw new Exception("no array specified");
            }
            else return temp;
        }
        return temp;
    }

    /**
     * calls up the chart, and checks for the match keywords after. Calls the next level up after the match
     * keywords to get the next value. Not associative at all. if there are no matches it just returns what was
     * up the expression chart
     * @return
     * @throws Exception if there is nothing after the MATCH or NOTMATCH keywords
     */
    Optional<Node> parseMatch() throws Exception{
        Optional<Node> left = parseComparison();
        if(handler.MatchAndRemove(Token.TokenType.TILDE).isPresent()){
            Optional<Node> right = parseComparison();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.MATCH, right));
            }
            else throw new Exception("Nothing found after Backtick");
        }
        else if(handler.MatchAndRemove(Token.TokenType.NOTMATCH).isPresent()){
            Optional<Node> right = parseComparison();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.NOTMATCH, right));
            }
            else throw new Exception("Nothing found after Not-Match");
        }
        else return left;
    }

    /**
     * calls the next level up for the nfirst expression, then checks if there is a comparator operator.
     * if there is, it will call the next level again to get the second expression and return a new Operation Node
     * , or else it will just return what was up the expression chart
     * @return
     * @throws Exception if there is nothing after those comparator operators
     */
    Optional<Node> parseComparison() throws Exception{
        Optional<Node> left = parseConcat();
        if(handler.MatchAndRemove(Token.TokenType.LESSTHAN).isPresent()){
            Optional<Node> right = parseConcat();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.LESSTHAN, right));
            } else throw new Exception("no expression sighted after less than operator");
        }
        if(handler.MatchAndRemove(Token.TokenType.LESSTHANEQUAL).isPresent()){
            Optional<Node> right = parseConcat();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.LESSEQUAL, right));
            } else throw new Exception("no expression sighted after less equal operator");
        }
        if(handler.MatchAndRemove(Token.TokenType.NOTEQUAL).isPresent()){
            Optional<Node> right = parseConcat();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.NOTEQUALS, right));
            } else throw new Exception("no expression sighted after not equals operator");
        }
        if(handler.MatchAndRemove(Token.TokenType.EQUALEQUAL).isPresent()){
            Optional<Node> right = parseConcat();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.EQUALS, right));
            } else throw new Exception("no expression sighted after less than operator");
        }
        if(handler.MatchAndRemove(Token.TokenType.GREATERTHAN).isPresent()){
            Optional<Node> right = parseConcat();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.GREATERTHAN, right));
            } else throw new Exception("no expression sighted after greater than operator");
        }
        if(handler.MatchAndRemove(Token.TokenType.GREATEREQUAL).isPresent()){
            Optional<Node> right = parseConcat();
            if(right.isPresent()){
                return Optional.of(new OperationNode(left, OperationNode.possibleOps.GREATEREQUAL, right));
            } else throw new Exception("no expression sighted after greater equal operator");
        }
        else return left;
    }

    /**
     * parses the next level to get the first value. will recursively call itself to make a new Operation node with
     * concatenation, aligned to the right. if there is no conccatenation it will just return what was up the chart.
     * @return
     * @throws Exception only throws an exception because of the methods above on the expression chart
     */
    Optional<Node> parseConcat() throws Exception{
        Optional<Node> left = parseAddSubtract();
        while(handler.MoreTokens()){
            Optional<Node> right = parseAddSubtract();
            if(right.isPresent()){
                left = Optional.of(new OperationNode(left, OperationNode.possibleOps.CONCATENATION, right));
            }
            else return left;
        }
        return left;
    }

    /**
     * calls ups the expression tree to get the first value, then keeps calling the next level recursively
     * if there are more pluses or minuses so it can make new Operation nodes - shifted to the left
     * @return a new Operation Node with Plus or minus, or whatever was up the expression tree if there was no + or -
     * @throws Exception throws an exception if there is nothing after add or subtract
     */
    Optional<Node> parseAddSubtract() throws Exception{
        Optional<Node> left = parseMultiplyDivideMod();
        while(handler.MoreTokens()){
            if(handler.MatchAndRemove(Token.TokenType.PLUS).isPresent()){
                Optional<Node> right = parseMultiplyDivideMod();
                if(right.isPresent()){
                    left = Optional.of(new OperationNode(left, OperationNode.possibleOps.ADD, right));
                }
                else throw new Exception("no expression seen after plus");
            }
            else if(handler.MatchAndRemove(Token.TokenType.MINUS).isPresent()){
                Optional<Node> right = parseMultiplyDivideMod();
                if(right.isPresent()){
                    left = Optional.of(new OperationNode(left, OperationNode.possibleOps.SUBTRACT, right));
                }
                else throw new Exception("no expression seen after minus");
            }
            else return left;
        }
        return left;
    }

    /**
     * calls ups the expression tree to get the first value, then keeps calling the next level recursively
     * if there are more OPS so it can make new Operation nodes - shifted to the left
     * @return a new Operation Node with Multiply, Divide, or Mod, or whatever was up the expression tree if there was no
     * * or / or %
     * @throws Exception throws an exception if there is nothing after * or / or %
     */
    Optional<Node> parseMultiplyDivideMod() throws Exception{
        Optional<Node> left = parseExponents();
        while(handler.MoreTokens()){ // YOU CAN TAKE THIS OUT
            if(handler.MatchAndRemove(Token.TokenType.MUTIPLICATIONSYMBOL).isPresent()){
                Optional<Node> right = parseExponents();
                if(right.isPresent()){
                    left = Optional.of(new OperationNode(left, OperationNode.possibleOps.MULTIPLY, right));
                }
                else throw new Exception("no expression seen after multiplication symbol");
            }
            else if(handler.MatchAndRemove(Token.TokenType.DIVISIONSYMBOL).isPresent()){
                Optional<Node> right = parseExponents();
                if(right.isPresent()){
                    left = Optional.of(new OperationNode(left, OperationNode.possibleOps.DIVIDE, right));
                }
                else throw new Exception("no expression seen after division symbol");
            }
            else if(handler.MatchAndRemove(Token.TokenType.MODULO).isPresent()){
                Optional<Node> right = parseExponents();
                if(right.isPresent()){
                    left = Optional.of(new OperationNode(left, OperationNode.possibleOps.MODULO, right));
                }
                else throw new Exception("no expression seen after modulus symbol");
            }
            else return left;
        }
        return left;
    }

    /**
     * calls ups the expression tree to get the first value, then keeps calling itself recursively
     * if there are more exponents so it can make new Operation nodes - shifted to the right
     * @return a new Operation Node with exponents, or whatever was up the expression tree if there was no ^
     * @throws Exception throws an exception if there is nothing after exponent
     */
    Optional<Node> parseExponents() throws Exception{
        Optional<Node> left = parsePost();
        while(handler.MoreTokens()){
            if(handler.MatchAndRemove(Token.TokenType.EXPONENT).isPresent()){
                Optional<Node> right = parseExponents();
                if(right.isPresent()){
                    left = Optional.of(new OperationNode(left, OperationNode.possibleOps.EXPONENT, right));
                } else throw new Exception("no expression sighted after exponent symbol");
            }
            else return left;
        }
        return left;
    }

    /**
     * calls ParseBottom - the final level - to parse it, and then checks if there is a postinc or postdec. if there is, it will return a new OPNode
     * with them, or else it will return the final level of our expression chart.
     */
    Optional<Node> parsePost() throws Exception{
        Optional<Node> left = ParseBottomLevel();
        if(handler.MatchAndRemove(Token.TokenType.INCREMENT).isPresent()){
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.POSTINC, Optional.empty())));
//            return Optional.of(new OperationNode(left, OperationNode.possibleOps.POSTINC, Optional.empty()));
        }
        if(handler.MatchAndRemove(Token.TokenType.DECREMENT).isPresent()){
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left, OperationNode.possibleOps.POSTDEC, Optional.empty())));
        }
        else return left;
    }


    public Optional<StatementNode> parseStatement() throws Exception {
        Optional<StatementNode> myStatement;

        myStatement = parseContinue();
        if(myStatement.isPresent()){
            return myStatement;
        }
        myStatement = parseBreak();
        if(myStatement.isPresent()){
            return myStatement;
        }
        myStatement = parseIf();
        if(myStatement.isPresent()){
            return myStatement;
        }
        myStatement = parseFor();
        if(myStatement.isPresent()){
            return myStatement;
        }
        myStatement = parseDelete();
        if(myStatement.isPresent()){
            return myStatement;
        }
        myStatement = parseWhile();
        if(myStatement.isPresent()){
            return myStatement;
        }
        myStatement = parseDoWhile();
        if(myStatement.isPresent()){
            return myStatement;
        }
        myStatement = parseReturn();
        if(myStatement.isPresent()){
            return myStatement;
        }
        Optional<Node> opStatement = ParseOperation();
        if(opStatement.isEmpty()){
            return Optional.empty();
        }
        if(opStatement.get() instanceof AssignmentNode){
            AssignmentNode temp = (AssignmentNode) opStatement.get();
            return Optional.of(temp);
        }
        if(opStatement.get() instanceof functionCallNode){
            functionCallNode temp = (functionCallNode) opStatement.get();
            return Optional.of(temp);
        }
        else throw new Exception("invalid expression - not a statement");
    }

    public Optional<StatementNode> parseContinue() throws Exception {
        if(handler.MatchAndRemove(Token.TokenType.CONTINUE).isPresent()){
            if(acceptSeperators() || handler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTCURLYBRACE)){
                return Optional.of(new continueNode());
            }
            else throw new Exception("tokens seen between CONTINUE and next separator");
            // In AWK, if there is code seen after a CONTINUE, but before a separator, it will throw an exception
            // it wont throw an exception, however, if there is code after it but after a separator is seen
        }
        else return Optional.empty();
    }

    public Optional<StatementNode> parseBreak() throws Exception {
        if(handler.MatchAndRemove(Token.TokenType.BREAK).isPresent()){
            if(acceptSeperators() || handler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTCURLYBRACE)){
                return Optional.of(new breakNode());
            }
            else throw new Exception("tokens seen between BREAK and next separator");
            // In AWK, if there is code seen after a BREAK, but before a separator, it will throw an exception
            // it wont throw an exception, however, if there is code after it but after a separator is seen
        }
        else return Optional.empty();
    }

    /**
     * recursively calls itself to handle else if's and else's. in the recursive call, it will check if there is another
     * if - if so, it will parse it. if not, it will simply parseBlock because it is an "else" instead of an "else if".
     * recursion stops at the "else" - when there is no more if's
     * @return
     * @throws Exception
     */
    public Optional<StatementNode> parseIf() throws Exception{
        if(handler.MatchAndRemove(Token.TokenType.IF).isPresent()){
            if(handler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES).isEmpty()){
                throw new Exception("no left parantheses found after IF");
            }
            Optional<Node> condition = ParseOperation();
            if(handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isEmpty()){
                throw new Exception("no Right Parantheses found for if statement");
            }
            BlockNode statements = parseBlock().get();
            ifNode currIf = new ifNode(condition, statements, Optional.empty());
            if(handler.MatchAndRemove(Token.TokenType.ELSE).isPresent()){
                Optional<StatementNode> nextIf = parseIf();
                if(nextIf.isPresent()){
                    currIf.setNext(nextIf);
                }
                else{
                    currIf.setNext(Optional.of(new ifNode(Optional.empty(), parseBlock().get(), Optional.empty())));
                }
            }
            return Optional.of(currIf);
        }
        return Optional.empty();
    }

    /**
     * in AWK, there are two different for loops. therefore, we check if the second token is the "IN" keyword - if so,
     * we know that it is a "ForEach" node.
     * @return
     * @throws Exception
     */
    public Optional<StatementNode> parseFor() throws Exception{
        if(handler.MatchAndRemove(Token.TokenType.FOR).isPresent()){
            boolean foundIn = false;
            if(handler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES).isPresent()){
                if(handler.Peek(1).get().getTokenType() == Token.TokenType.IN){
                    foundIn = true;
                }
                if(foundIn){
                    Optional<Node> condition = ParseOperation();
                    if(handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isEmpty()){
                        throw new Exception("no right paren found");
                    }
                    return Optional.of(new forEachNode(condition, parseBlock().get()));

                }
                else{
                    Optional<Node> statement1 = ParseOperation();
                    if(handler.MatchAndRemove(Token.TokenType.SEPARATOR).isPresent()){
                        Optional<Node> statement2 = ParseOperation();
                        if(handler.MatchAndRemove(Token.TokenType.SEPARATOR).isPresent()){
                            Optional<Node> statement3 = ParseOperation();
                            if(handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isPresent()){
                                return Optional.of(new forNode(statement1, statement2, statement3, parseBlock().get()));
                            }
                            else throw new Exception("no paranthesis after third expression in for loop");
                        }
                        else throw new Exception("no semicolon after second expression in for loop");
                    }
                    else throw new Exception("no semicolon after first expression in for loop");
                }
                }
            else throw new Exception("no parentheses seen after FOR keyword");
        }
        else return Optional.empty();
    }

    /**
     * DELETE in AWK has to have a value after it - thats why we call parseLValue after.
     * @return
     * @throws Exception
     */
    public Optional<StatementNode> parseDelete() throws Exception{
        // i have multiple questions ^_^
        if(handler.MatchAndRemove(Token.TokenType.DELETE).isPresent()){
            Optional<Node> array = ParseLValue();
            if(array.isPresent()){
                return Optional.of(new deleteNode(array));
            }
            else throw new Exception("Nothing seen after DELETE keyword");
        }
        return Optional.empty();
    }

    /**
     * the condition in the while must be an expression, while any statement can be in the block. throws exceptions
     * when the input violates the syntax.
     * @return
     * @throws Exception
     */
    public Optional<StatementNode> parseWhile() throws Exception{
        if(handler.MatchAndRemove(Token.TokenType.WHILE).isPresent()){
            if(handler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES).isEmpty()){
                throw new Exception("no left parantheses found for while statement");
            }
            Optional<Node> condition = ParseOperation();
            if(handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isEmpty()){
                throw new Exception("no right parantheses found for while statement");
            }
            if(condition.isPresent()){
                return Optional.of(new whileNode(condition, parseBlock().get()));
            }
            else throw new Exception("no condition inside while :-(");
        }
        return Optional.empty();
    }

    /**
     * makes sure that anything that is not part of AWK do whil syntax will throw an exception. anything can be in a
     * block, while the "while" condition must be a expression
     * @return
     * @throws Exception
     */
    public Optional<StatementNode> parseDoWhile() throws Exception{
        if(handler.MatchAndRemove(Token.TokenType.DO).isPresent()){
            BlockNode block = parseBlock().get();
            acceptSeperators();
            if(handler.MatchAndRemove(Token.TokenType.WHILE).isPresent()){
                if(handler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES).isEmpty()){
                    throw new Exception("no left parantheses found for do-while statement");
                }
                Optional<Node> condition = ParseOperation();
                if(handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isEmpty()){
                    throw new Exception("no right parantheses found for while statement");
                }
                if(condition.isPresent()){
                    return Optional.of(new doWhileNode(block, condition));
                }
                else throw new Exception("condition for do while is empty");
            }
            else throw new Exception("No while after do");
        }
        return Optional.empty();
    }

    /**
     * in AWK, return can have something after, or nothing. thats why we call parse operation - it can be empty
     * @return
     * @throws Exception
     */
    public Optional<StatementNode> parseReturn() throws Exception{
        if(handler.MatchAndRemove(Token.TokenType.RETURN).isPresent()){
            return Optional.of(new returnNode(ParseOperation()));
        }
        return Optional.empty();
    }

    /**
     * makes sure it checks for a right parantheses, since there can theoretically be an unlimited amount of inputs
     * therefore, it makes sure that they are separated by a comma, and there is a right parantheses at the very end.
     * makes sure no paramaters also work by immediatly checking for right parantheses
     * @return
     * @throws Exception
     */
    public Optional<Node> parseFunctionCall() throws Exception{
        if(handler.MatchAndRemove(Token.TokenType.GETLINE).isPresent()){
            Optional<Node> temp = ParseOperation();
            functionCallNode tempFunction = new functionCallNode("GETLINE", new LinkedList<>());
            if(temp.isPresent()){
                tempFunction.addParam(temp.get());
            }
            return Optional.of(tempFunction);
        }
        if(handler.MatchAndRemove(Token.TokenType.PRINT).isPresent()){
            functionCallNode temp = new functionCallNode("PRINT", new LinkedList<>());
                while(!handler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)){
                Optional<Node> tempOp = ParseOperation();
                if(tempOp.isEmpty()){
                    break;
                }
                temp.addParam(tempOp.get());
                handler.MatchAndRemove(Token.TokenType.COMMA);
            }
            return Optional.of(temp);
        }
        if(handler.MatchAndRemove(Token.TokenType.PRINTF).isPresent()){
            functionCallNode temp = new functionCallNode("PRINTF", new LinkedList<>());
            while(!handler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)){
                Optional<Node> tempOp = ParseOperation();
                if(tempOp.isEmpty()){
                    break;
                }
                temp.addParam(tempOp.get());
                handler.MatchAndRemove(Token.TokenType.COMMA);
            }
            return Optional.of(temp);
        }
        if(handler.MatchAndRemove(Token.TokenType.EXIT).isPresent()){
            functionCallNode temp = new functionCallNode("EXIT", new LinkedList<>());
            while(!handler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)){
                Optional<Node> tempOp = ParseOperation();
                if(tempOp.isEmpty()){
                    break;
                }
                temp.addParam(tempOp.get());
                if(handler.MatchAndRemove(Token.TokenType.COMMA).isPresent()){
                    throw new Exception("invalid syntax");
                }
            }
            return Optional.of(temp);
        }
        if(handler.MatchAndRemove(Token.TokenType.EXIT).isPresent()){
            functionCallNode temp = new functionCallNode("EXIT", new LinkedList<>());
            while(!handler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)){
                Optional<Node> tempOp = ParseOperation();
                if(tempOp.isEmpty()){
                    break;
                }
                temp.addParam(tempOp.get());
                if(handler.MatchAndRemove(Token.TokenType.COMMA).isPresent()){
                    throw new Exception("invalid syntax");
                }
            }
            return Optional.of(temp);
        }
        if(handler.MatchAndRemove(Token.TokenType.NEXTFILE).isPresent()){
            return Optional.of(new functionCallNode("NEXTFILE", new LinkedList<>()));
        }
        if(handler.MatchAndRemove(Token.TokenType.NEXT).isPresent()){
            return Optional.of(new functionCallNode("NEXT", new LinkedList<>()));
        }



        if (!handler.Peek(0).get().getTokenType().equals(Token.TokenType.WORD)) {
            return Optional.empty();
        }
        if (!(handler.Peek(1).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES))) {
            return Optional.empty();
        }
        Optional<Token> name = handler.MatchAndRemove(Token.TokenType.WORD);
        handler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
        functionCallNode functionCall = new functionCallNode(name.get().getValue(), new LinkedList<>());

        while ((handler.MoreTokens()) && (handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isEmpty())) {
            Optional<Node> tempParam = ParseOperation();
            if (tempParam.isPresent()) {
                functionCall.addParam(tempParam.get());
            } else if (handler.MatchAndRemove(Token.TokenType.COMMA).isEmpty() &&
                    handler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isEmpty()) {
                throw new Exception("arguments not separated by comma, or no right parantheses found");
            }
        }
        return Optional.of(functionCall);
    }
}
