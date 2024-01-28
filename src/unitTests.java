import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

public class unitTests {
    LinkedList<Token> myList = new LinkedList<>();
    String test1 = "sunsets are of different colors\n";
    String test2 = "the small fountain in front of \n the science library looks beautiful \r\n";
    String test3 = "9 hours is a long time \nit is 545pm and im working in the library \nI have had many " +
            "versions of LEXER1\n i spent .5 hours on unit tests and 1.5 hours on StringHandler";
//    String test4 = "i like looking at the sky :)";
    String decError = "i \nate 3.3.3 cheesecake bites";

    Lexer firstTest = new Lexer(test1);
    Lexer lineTest = new Lexer(test2);
    Lexer numbersTest = new Lexer(test3);
//    Lexer errorTest = new Lexer(test4);
    Lexer decimalError = new Lexer(decError);

    @Test
    public void wordsTest() throws Exception {
        myList = firstTest.Lex();
        assertEquals("SEPARATOR", myList.get(5).toString());
        assertEquals("WORD (sunsets)", myList.get(0).toString());
        assertEquals(7, myList.size());
        assertEquals("[WORD (sunsets), WORD (are), WORD (of), WORD (different), WORD (colors), SEPARATOR, SEPARATOR]", myList.toString());
    }

    @Test
    public void linesTest() throws Exception{
        myList = lineTest.Lex();
        assertEquals("WORD (fountain)", myList.get(2).toString());
        assertEquals("SEPARATOR", myList.get(6).toString());
        assertEquals("SEPARATOR", myList.get(12).toString());
        assertEquals("WORD (beautiful)", myList.get(11).toString());
    }

    @Test
    public void numbersTest() throws Exception{
        myList = numbersTest.Lex();
        assertEquals("NUMBER (9)", myList.get(0).toString());
        assertEquals("WORD (hours)", myList.get(1).toString());
        assertEquals("NUMBER (545)", myList.get(9).toString());
        assertEquals("WORD (pm)", myList.get(10).toString());
        assertEquals("WORD (LEXER1)", myList.get(24).toString());
        assertEquals("NUMBER (.5)", myList.get(28).toString());
        assertEquals("NUMBER (1.5)", myList.get(34).toString());
    }

    @Test
    public void errorTest() throws Exception{
//        try {
//            myList = errorTest.Lex();
//        }
//        catch(Exception exc){
//            assertEquals("INVALID CHARACTER at line 1 position 26 :'(", exc.getMessage());
//        }

        try{
            myList = decimalError.Lex();
        }
        catch (Exception ex){
            assertEquals("Too many decimals! - line 2 position 7 ^_^", ex.getMessage());

        }
//        assertThrows("Exception", errorTest.Lex());

    }

    @Test
    public void symbolsTest() throws Exception {

        String curTest = "coffee! is wonderful:) it = > of energy and < sleep schedule." +
                "it might make your grades >= its curr state.";
        String curtest2 = "no sleep makes ur grades *= 0 :{ it ! make sense to not sleep!?!";
        String awktest = "/a/{++cnt} END {print \"Count = \", cnt}";
        String straightCharactersYo = "-- <= == != %= /= += -= !~ && >> || and then " +
                "[] ( $ ~ + * - % | ,";
        //I GOT THE ABOVE TEST AWK CODE FROM ONLINE
        Lexer mylexer = new Lexer(curTest);
        myList = mylexer.Lex();
        assertEquals(Token.TokenType.WORD, myList.get(0).getTokenType());
        assertEquals(Token.TokenType.LOGICALNOT, myList.get(1).getTokenType());
        assertEquals(Token.TokenType.COLON, myList.get(4).getTokenType());
        assertEquals(Token.TokenType.RIGHTPARENTHESES, myList.get(5).getTokenType());
        assertEquals(Token.TokenType.EQUALS, myList.get(7).getTokenType());
        assertEquals(Token.TokenType.GREATERTHAN, myList.get(8).getTokenType());
        assertEquals(Token.TokenType.LESSTHAN, myList.get(12).getTokenType());
        assertEquals(Token.TokenType.GREATEREQUAL, myList.get(21).getTokenType());

        mylexer = new Lexer(curtest2);
        myList = mylexer.Lex();
        assertEquals(Token.TokenType.MULTIPLICATIONEQUALS, myList.get(5).getTokenType());
        assertEquals(Token.TokenType.COLON, myList.get(7).getTokenType());
        assertEquals(Token.TokenType.LEFTCURLYBRACE, myList.get(8).getTokenType());
        assertEquals(Token.TokenType.LOGICALNOT, myList.get(10).getTokenType());
        assertEquals(Token.TokenType.LOGICALNOT, myList.get(16).getTokenType());
        assertEquals(Token.TokenType.TERNARYOPERATOR, myList.get(17).getTokenType());
        assertEquals(Token.TokenType.LOGICALNOT, myList.get(18).getTokenType());

        mylexer = new Lexer(awktest);
        myList = mylexer.Lex();
        assertEquals(Token.TokenType.DIVISIONSYMBOL, myList.get(0).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(1).getTokenType());
        assertEquals(Token.TokenType.DIVISIONSYMBOL, myList.get(2).getTokenType());
        assertEquals(Token.TokenType.LEFTCURLYBRACE, myList.get(3).getTokenType());
        assertEquals(Token.TokenType.INCREMENT, myList.get(4).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(5).getTokenType());
        assertEquals(Token.TokenType.END, myList.get(7).getTokenType());
        assertEquals(Token.TokenType.PRINT, myList.get(9).getTokenType());
        assertEquals(Token.TokenType.STRINGLITERAL, myList.get(10).getTokenType());
        assertEquals(Token.TokenType.COMMA, myList.get(11).getTokenType());
        assertEquals(Token.TokenType.RIGHTCURLYBRACE, myList.get(13).getTokenType());


        mylexer = new Lexer(straightCharactersYo);
        myList = mylexer.Lex();
        assertEquals(Token.TokenType.DECREMENT, myList.get(0).getTokenType());
        assertEquals(Token.TokenType.LESSTHANEQUAL, myList.get(1).getTokenType());
        assertEquals(Token.TokenType.EQUALEQUAL, myList.get(2).getTokenType());
        assertEquals(Token.TokenType.NOTEQUAL, myList.get(3).getTokenType());
        assertEquals(Token.TokenType.MODULOEQUALS, myList.get(4).getTokenType());
        assertEquals(Token.TokenType.DIVISIONEQUALS, myList.get(5).getTokenType());
        assertEquals(Token.TokenType.PLUSEQUALS, myList.get(6).getTokenType());
        assertEquals(Token.TokenType.MINUESEQUALS, myList.get(7).getTokenType());
        assertEquals(Token.TokenType.NOTMATCH, myList.get(8).getTokenType());
        assertEquals(Token.TokenType.ANDAND, myList.get(9).getTokenType());
        assertEquals(Token.TokenType.SIGNEDRIGHTSHIFT, myList.get(10).getTokenType());
        assertEquals(Token.TokenType.LOGICALOR, myList.get(11).getTokenType());
        assertEquals(Token.TokenType.LEFTBRACKET, myList.get(14).getTokenType());
        assertEquals(Token.TokenType.RIGHTBRACKET, myList.get(15).getTokenType());
        assertEquals(Token.TokenType.LEFTPARENTHESES, myList.get(16).getTokenType());
        assertEquals(Token.TokenType.DOLLARSIGN, myList.get(17).getTokenType());
        assertEquals(Token.TokenType.TILDE, myList.get(18).getTokenType());
        assertEquals(Token.TokenType.PLUS, myList.get(19).getTokenType());
        assertEquals(Token.TokenType.MUTIPLICATIONSYMBOL, myList.get(20).getTokenType());
        assertEquals(Token.TokenType.MINUS, myList.get(21).getTokenType());
        assertEquals(Token.TokenType.MODULO, myList.get(22).getTokenType());
        assertEquals(Token.TokenType.PIPE, myList.get(23).getTokenType());
        assertEquals(Token.TokenType.COMMA, myList.get(24).getTokenType());

    }

    @Test
    public void StringLiteralAndKeywordTest() throws Exception {
//       AND THEN COMMENTS #
        String mytest = "END print cake \"if\" if while do break continue else return getline" +
                " delete function iff whilee eles end delet brake car for BEGIN begin " +
                "printf next in exit nextfile ";

        Lexer mylexer = new Lexer(mytest);
        myList = mylexer.Lex();
        assertEquals(Token.TokenType.FOR, myList.get(21).getTokenType());
        assertEquals(Token.TokenType.BEGIN, myList.get(22).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(23).getTokenType());
        assertEquals(Token.TokenType.PRINTF, myList.get(24).getTokenType());
        assertEquals(Token.TokenType.NEXT, myList.get(25).getTokenType());
        assertEquals(Token.TokenType.IN, myList.get(26).getTokenType());
        assertEquals(Token.TokenType.EXIT, myList.get(27).getTokenType());
        assertEquals(Token.TokenType.NEXTFILE, myList.get(28).getTokenType());

        assertEquals(Token.TokenType.END, myList.get(0).getTokenType());
        assertEquals(Token.TokenType.PRINT, myList.get(1).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(2).getTokenType());
        assertEquals("cake", myList.get(2).getValue());
        assertEquals(Token.TokenType.STRINGLITERAL, myList.get(3).getTokenType());
        assertEquals("if", myList.get(3).getValue());
        assertEquals(Token.TokenType.IF, myList.get(4).getTokenType());
        assertEquals(Token.TokenType.WHILE, myList.get(5).getTokenType());
        assertEquals(Token.TokenType.DO, myList.get(6).getTokenType());
        assertEquals(Token.TokenType.BREAK, myList.get(7).getTokenType());
        assertEquals(Token.TokenType.CONTINUE, myList.get(8).getTokenType());
        assertEquals(Token.TokenType.ELSE, myList.get(9).getTokenType());
        assertEquals(Token.TokenType.RETURN, myList.get(10).getTokenType());
        assertEquals(Token.TokenType.GETLINE, myList.get(11).getTokenType());
        assertEquals(Token.TokenType.DELETE, myList.get(12).getTokenType());
        assertEquals(Token.TokenType.FUNCTION, myList.get(13).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(14).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(15).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(16).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(17).getTokenType());
        assertEquals("end", myList.get(17).getValue());
        assertEquals(Token.TokenType.WORD, myList.get(18).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(19).getTokenType());
        assertEquals(Token.TokenType.WORD, myList.get(20).getTokenType());
    }

    @Test
    public void commentTest() throws Exception {
        String myComment = "i like monopoly # Somehow I always win \n #the orange properties " +
                "are the best";
        Lexer mylexer = new Lexer(myComment);
        myList = mylexer.Lex();

        assertEquals(5, myList.size());
        assertEquals(Token.TokenType.SEPARATOR, myList.get(3).getTokenType());
        assertEquals(Token.TokenType.SEPARATOR, myList.get(4).getTokenType());
        assertNotEquals("Somehow", myList.get(3).getValue());
        assertNotEquals("Somehow", myList.get(4).getValue());
        assertEquals("[WORD (i), WORD (like), WORD (monopoly), SEPARATOR, SEPARATOR]", myList.toString());



    }
}
