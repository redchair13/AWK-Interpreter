import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;


import java.util.LinkedList;
import java.util.Optional;

// Test your functionDefinition and acceptSeperators
public class parserUnitTests {
    String myString = "function add(\n5\n,\n\n\n 5){}"; // the \n are because I want to make sure my acceptSeperators works
    Lexer myLexer = new Lexer(myString);
    LinkedList<Token> myTokens; // this will get changed when I run .Parse(), so I make a copy
    LinkedList<Token> tokensNotParsed; // I will test my TokenManager itself on the same tokens
    Parser myParser;
    ProgramNode myProgram;

    {
        try {
            myTokens = myLexer.Lex();
            tokensNotParsed = new LinkedList<>(myTokens);
            myParser = new Parser(myTokens);
            myProgram = myParser.Parse();
        } catch (Exception c) {
            System.out.println("hello");
        }
    }


    @Test
    public void tokenHandlerTest() throws Exception {
        //tokenhandler has Peek, matchAndRemove, and moreTokens methods for now
        TokenManager myManager = new TokenManager(tokensNotParsed);
        assertEquals((Token.TokenType.FUNCTION), myManager.Peek(0).get().getTokenType());
        assertEquals((Token.TokenType.WORD), myManager.Peek(1).get().getTokenType());
        assertEquals((Token.TokenType.SEPARATOR), myManager.Peek(3).get().getTokenType());
        assertEquals((Token.TokenType.NUMBER), myManager.Peek(4).get().getTokenType());
        assertEquals((Token.TokenType.SEPARATOR), myManager.Peek(5).get().getTokenType());
        assertEquals((Token.TokenType.COMMA), myManager.Peek(6).get().getTokenType());
        assertEquals(true, myManager.MoreTokens());
        assertEquals(Token.TokenType.FUNCTION, myManager.MatchAndRemove(Token.TokenType.FUNCTION).get().getTokenType());
        assertEquals(Token.TokenType.WORD, myManager.MatchAndRemove(Token.TokenType.WORD).get().getTokenType());
        assertEquals(Token.TokenType.LEFTPARENTHESES, myManager.MatchAndRemove(Token.TokenType.LEFTPARENTHESES).get().getTokenType());
        assertEquals(Token.TokenType.SEPARATOR, myManager.MatchAndRemove(Token.TokenType.SEPARATOR).get().getTokenType());
        assertEquals(Token.TokenType.NUMBER, myManager.MatchAndRemove(Token.TokenType.NUMBER).get().getTokenType());
        assertEquals(Token.TokenType.SEPARATOR, myManager.MatchAndRemove(Token.TokenType.SEPARATOR).get().getTokenType());
        assertEquals(Token.TokenType.COMMA, myManager.MatchAndRemove(Token.TokenType.COMMA).get().getTokenType());
        assertEquals(Token.TokenType.SEPARATOR, myManager.MatchAndRemove(Token.TokenType.SEPARATOR).get().getTokenType());
        assertEquals(Token.TokenType.SEPARATOR, myManager.MatchAndRemove(Token.TokenType.SEPARATOR).get().getTokenType());
        assertEquals(Token.TokenType.SEPARATOR, myManager.MatchAndRemove(Token.TokenType.SEPARATOR).get().getTokenType());
        assertEquals(Token.TokenType.NUMBER, myManager.MatchAndRemove(Token.TokenType.NUMBER).get().getTokenType());
        assertEquals(Token.TokenType.RIGHTPARENTHESES, myManager.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).get().getTokenType());
        assertEquals(Token.TokenType.LEFTCURLYBRACE, myManager.MatchAndRemove(Token.TokenType.LEFTCURLYBRACE).get().getTokenType());
        assertEquals(Token.TokenType.RIGHTCURLYBRACE, myManager.MatchAndRemove(Token.TokenType.RIGHTCURLYBRACE).get().getTokenType());
        assertTrue(myManager.MoreTokens());
        assertEquals(Token.TokenType.SEPARATOR, myManager.MatchAndRemove(Token.TokenType.SEPARATOR).get().getTokenType());
        assertEquals(Optional.empty(), myManager.MatchAndRemove(Token.TokenType.COMMA));
        assertFalse(myManager.MoreTokens());
    }

    @Test
    public void testFunctionDef() {
        assertEquals(0, myProgram.getBeginblocks().size());
        assertEquals(0, myProgram.getEndblocks().size());
        assertEquals(0, myProgram.getOtherblocks().size());
        assertEquals(1, myProgram.getFunctionDefinitions().size());
        assertEquals("function add [5, 5] []", myProgram.getFunctionDefinitions().get(0).toString());
    }


    @Test
    public void emptyFunctionParams() throws Exception {
        String myString = "function add(){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals("function add [] []", myProgram.getFunctionDefinitions().get(0).toString());

    }

    @Test
    public void preIncTest() throws Exception {
        String myString = "BEGIN{FS = \" \"} (++a){\n ret = ret + NF;\n}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().toString(), "[(a = PREINC a)[ret = ret ADD NF ]]");
    }

    @Test
    public void dollarTest() throws Exception {
        String myString = "(++$b){ret = ret + NF;}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().toString(), "[(DOLLAR b = PREINC DOLLAR b)[ret = ret ADD NF ]]");
    }

    @Test
    public void parenthesesWithAnExpressionTest() throws Exception {
        String myString = "((++b)){ret = ret + NF;}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().toString(), "[(b = PREINC b)[ret = ret ADD NF ]]");
    }

    @Test
    public void numberTest() throws Exception {
        String myString = "-5{ret = ret + NF;}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(UNARYNEG 5)[ret = ret ADD NF ]");
    }

    @Test
    public void anotherTest() throws Exception {
        String myString = "-e[++b]{ret = ret + NF;}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(UNARYNEG e [b = PREINC b])[ret = ret ADD NF ]");
    }

    @Test
    public void dollarSign() throws Exception {
        String myString = "$7{ret = ret + NF;}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(DOLLAR 7)[ret = ret ADD NF ]");
    }

    @Test
    public void errorTest() throws Exception {
        String myString = "(++a";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        try {
            ProgramNode myProgram = myParser.Parse();
            System.out.println(myProgram.toString());
        } catch (Exception e) {
            assertEquals("no right parentheses found at line 1 and char number 3", e.getMessage());
        }
    }

    @Test
    public void assignmentTest() throws Exception {
        String myString = "(left1 ^= expr1){}(lvalue2 %= expr2){}(lvalue3 *= expr3){}(lvalue4 /= expr4){}(lvalue5 += expr5)" +
                "{}(lvalue6 -= expr6){}(lvalue7 = expr7){}(a += (b - c)){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(left1 = left1 EXPONENT expr1 )[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(lvalue2 = lvalue2 MODULO expr2 )[]");
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "(lvalue3 = lvalue3 MULTIPLY expr3 )[]");
        assertEquals(myProgram.getOtherblocks().get(3).toString(), "(lvalue4 = lvalue4 DIVIDE expr4 )[]");
        assertEquals(myProgram.getOtherblocks().get(4).toString(), "(lvalue5 = lvalue5 ADD expr5 )[]");
        assertEquals(myProgram.getOtherblocks().get(5).toString(), "(lvalue6 = lvalue6 SUBTRACT expr6 )[]");
        assertEquals(myProgram.getOtherblocks().get(6).toString(), "(lvalue7 = expr7)[]");
        assertEquals(myProgram.getOtherblocks().get(7).toString(), "(a = a ADD b SUBTRACT c  )[]");
    }

    @Test
    public void ternaryTest() throws Exception {
        String myString = "(a = a ? b : c){}(a ? in1 ? in2 : in3 : b){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(a = a ? b : c)[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(a ? in1 ? in2 : in3 : b)[]");
        assertEquals(2, myProgram.getOtherblocks().size());
    }

    @Test
    public void andOrTest() throws Exception {
        String myString = "(sun || moon){} (cc || library || home || cafe){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(sun OR moon )[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(cc OR library  OR home  OR cafe )[]");
        //you can see these are left associative - look at the spacing between the words
    }

    @Test
    public void arraysAndMatch() throws Exception {
        String myString = "(num in myNumbers){} (char in MyString in myBook){} " +
                "(char ~ char){}(char2 !~ myChar2){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(num IN myNumbers )[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(char IN MyString  IN myBook )[]");
        // as you can tell by the spacing, this is also left associative
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "(char MATCH char )[]");
        assertEquals(myProgram.getOtherblocks().get(3).toString(), "(char2 NOTMATCH myChar2 )[]");
    }

    @Test
    public void comparisonTest() throws Exception {
        String myString = "(left1 < expr1){}(lvalue2 <= expr2){}(lvalue3 != expr3){}(lvalue4 == expr4){}(lvalue5 > expr5)" +
                "{}(lvalue6 >= expr6){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(left1 LESSTHAN expr1 )[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(lvalue2 LESSEQUAL expr2 )[]");
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "(lvalue3 NOTEQUALS expr3 )[]");
        assertEquals(myProgram.getOtherblocks().get(3).toString(), "(lvalue4 EQUALS expr4 )[]");
        assertEquals(myProgram.getOtherblocks().get(4).toString(), "(lvalue5 GREATERTHAN expr5 )[]");
        assertEquals(myProgram.getOtherblocks().get(5).toString(), "(lvalue6 GREATEREQUAL expr6 )[]");
    }

    @Test
    public void stringConcatTest() throws Exception {
        String myString = "(steins gate){} (a = a b c d e f){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(steins CONCATENATION gate )[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(a = a CONCATENATION b  CONCATENATION c  CONCATENATION d  " +
                "CONCATENATION e  CONCATENATION f )[]");
        // again, you can tell by the double spacing that it is left oriented.
        //I arranged it like this in my toString
    }

    @Test
    public void additionAndMultiplicationEtcTest() throws Exception {
        String myString = "(charHealth + charDamage){} (a + b + c + g){}" +
                "(forest - numTrees){} (classroom - marker1 - m2 - m3 - m4){}" +
                "num1 * num2{} num3 * num4 * num5 * num6{}" +
                "dNum1 / dNum2{} dNum3 / dNum4 / dNum5 / dNum6{}" +
                "mod1 % mod2{} mod3 % mod4 % mod5{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(charHealth ADD charDamage )[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(a ADD b  ADD c  ADD g )[]");
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "(forest SUBTRACT numTrees )[]");
        assertEquals(myProgram.getOtherblocks().get(3).toString(), "(classroom SUBTRACT marker1  SUBTRACT m2 " +
                " SUBTRACT m3  SUBTRACT m4 )[]");
        assertEquals(myProgram.getOtherblocks().get(4).toString(), "(num1 MULTIPLY num2 )[]");
        assertEquals(myProgram.getOtherblocks().get(5).toString(), "(num3 MULTIPLY num4  MULTIPLY num5  MULTIPLY num6 )[]");
        assertEquals(myProgram.getOtherblocks().get(6).toString(), "(dNum1 DIVIDE dNum2 )[]");
        assertEquals(myProgram.getOtherblocks().get(7).toString(), "(dNum3 DIVIDE dNum4  DIVIDE dNum5  DIVIDE dNum6 )[]");
        assertEquals(myProgram.getOtherblocks().get(8).toString(), "(mod1 MODULO mod2 )[]");
        assertEquals(myProgram.getOtherblocks().get(9).toString(), "(mod3 MODULO mod4  MODULO mod5 )[]");
        // ALL of the double spaces indicate that it is left oriented
    }

    @Test
    public void exponentTest() throws Exception {
        String myString = "(a ^ b){} (c ^ d ^ e ^ f){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(a EXPONENT b )[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(c EXPONENT d EXPONENT e EXPONENT f   )[]");
    }

    @Test
    public void postIncTest() throws Exception {
        String myString = "(a++){} (booooo--){}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(a = a POSTINC)[]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "(booooo = booooo POSTDEC)[]");
    }

    @Test
    public void continueAndBreak() throws Exception {
        String myString = "BEGIN{a += 5\ncontinue\n}{a /= 5\nbreak\n}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getBeginblocks().get(0).toString(), "[a = a ADD 5 , CONTINUE\n]");
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "[a = a DIVIDE 5 , BREAK\n]");
    }

    @Test
    public void parseIf() throws Exception {
        String myString = "{if(a < 5){ paper = garbage} \n else if(a > 5){ paper = shredder} }" +
                "{if(tires == flat) \n buy(newTire)}" +
                "{if(laptop == dead) {laptop += charger} else laptop = full\n}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "[IF (a LESSTHAN 5 ) {[paper = garbage] }else IF (a GREATERTHAN 5 ) [paper = shredder]\n\n]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "[IF (tires EQUALS flat ) [buy([newTire])]\n]");
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "[IF (laptop EQUALS dead ) {[laptop = laptop ADD charger ] }else ELSE {[laptop = full]}\n\n]");
    }

    @Test
    public void parseFor() throws Exception {
        String myString = "{for(i = 0; i < 5; i++){ paper = garbage} \n }" +
                "{for(tire in tires) \n buy(newTire)}" +
                "{for(door in doors) {for(j = 0; j < 4; j++){sprayPaint(door[j]) } \n}} " +
                "{for(i < 0; ; ){ i -= 3}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "[for(i = 0;i LESSTHAN 5 ;i = i POSTINC)[paper = garbage]\n]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "[for(tire IN tires ) [buy([newTire])]\n]");
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "[for(door IN doors ) [for(j = 0;j LESSTHAN 4 ;j = j POSTINC)[sprayPaint([door [j]])]\n]\n]");
        assertEquals(myProgram.getOtherblocks().get(3).toString(), "[for(i LESSTHAN 0 ;;)[i = i SUBTRACT 3 ]\n]");
    }

    @Test
    public void parseDelete() throws Exception {
        String myString = "{if(i < 5){delete a} }" +
                "{if(a[i] == 5){delete a[i]}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "[IF (i LESSTHAN 5 ) [DELETE a]\n]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "[IF (a [i] EQUALS 5 ) [DELETE a [i]]\n]");
    }

    @Test
    public void whileAndDoWhileTest() throws Exception {
        String myString = "{while(i < 5){ i++}}" +
                "{while(cat < mouse){ if(cat < 5){ mouse--} else cat++}}" +
                "{do{interpret()\n parse()\n} while(moreTokens())}" +
                "{do{throwaway()\n do{cleanUP() }while(i < 5)\n} while(i < 10)}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "[while (i LESSTHAN 5 ) [i = i POSTINC]\n]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "[while (cat LESSTHAN mouse ) [IF (cat LESSTHAN 5 ) {[mouse = mouse POSTDEC] }else ELSE {[cat = cat POSTINC]}\n\n]\n]");
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "[do { [interpret(), parse()]} while( moreTokens())\n]");
        assertEquals(myProgram.getOtherblocks().get(3).toString(), "[do { [throwaway(), do { [cleanUP()]} while( i LESSTHAN 5 )\n" +
                "]} while( i LESSTHAN 10 )\n" +
                "]");
    }

    @Test
    public void returnTest() throws Exception {
        String myString = "{for(; i < 5; i++) { integers[i] = i; \n} return integers;}" +
                "{if(a[i] == 5){return a[i]}}" +
                "{if(a < 5){ paper = garbage} \n else return}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "[for(;i LESSTHAN 5 ;i = i POSTINC)[integers [i] = i]\n" +
                ", RETURN integers\n]");
        assertEquals(myProgram.getOtherblocks().get(1).toString(), "[IF (a [i] EQUALS 5 ) [RETURN a [i]\n" +
                "]\n" +
                "]");
        assertEquals(myProgram.getOtherblocks().get(2).toString(), "[IF (a LESSTHAN 5 ) {[paper = garbage] }else ELSE {[RETURN \n" +
                "]}\n" +
                "\n" +
                "]");
    }

    @Test
    public void assignmentAndFunctionCall() throws Exception {
        String myString = "BEGIN{a ^= 5}{a /= 5\nwatchAnime()\n}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getBeginblocks().get(0).toString(), "[a = a EXPONENT 5 ]");
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "[a = a DIVIDE 5 , watchAnime()]");
    }

    @Test
    public void errorsTest() throws Exception {
        String myString = "{for(i = 0; i < 25; i++}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        try {
            Parser myParser = new Parser(myTokens);
            ProgramNode myProgram = myParser.Parse();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "no paranthesis after third expression in for loop");
        }

        myString = "delete ";
        myLexer = new Lexer(myString);
        myTokens = myLexer.Lex();
        try {
            Parser myParser = new Parser(myTokens);
            ProgramNode myProgram = myParser.Parse();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Nothing seen after DELETE keyword");
        }

        myString = "while() ";
        myLexer = new Lexer(myString);
        myTokens = myLexer.Lex();
        try {
            Parser myParser = new Parser(myTokens);
            ProgramNode myProgram = myParser.Parse();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "no condition inside while :-(");
        }

        myString = "do{ i++ } ";
        myLexer = new Lexer(myString);
        myTokens = myLexer.Lex();
        try {
            Parser myParser = new Parser(myTokens);
            ProgramNode myProgram = myParser.Parse();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "No while after do");
        }

        myString = "BEGIN{parseLValue(a bunch of tokens}";
        myLexer = new Lexer(myString);
        myTokens = myLexer.Lex();
        try {
            Parser myParser = new Parser(myTokens);
            ProgramNode myProgram = myParser.Parse();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "arguments not separated by comma, or no right parantheses found");
        }
    }

//    @Test
//    public void testFunctions() throws Exception {
//        String myString = "{if(i < 5) print(5) ;}";
//        // got this test from online a long time ago i dont have the source i commented it out
//                //" {if($2==$3){print $1\",\"$2\",\"$3} else {print \"No Duplicates\"}} ";  //"{\n" +
////                "            if ( $1 == \"foo\" ) {\n" +
////                "                if ( $2 == \"foo\" ) {\n" +
////                "                    print \"uno\"\n" +
////                "                } else {\n" +
////                "                    print \"one\"\n" +
////                "                }\n" +
////                "            } else if ($1 == \"bar\" ) {\n" +
////                "                print \"two\"\n" +
////                "            } else {\n" +
////                "                print \"three\"\n" +
////                "            }\n" +
////                "        }";
//        Lexer myLexer = new Lexer(myString);
//        LinkedList<Token> myTokens = myLexer.Lex();
//        Parser myParser = new Parser(myTokens);
//        ProgramNode myProgram = myParser.Parse();
//        System.out.println(myProgram);
//    }
}
