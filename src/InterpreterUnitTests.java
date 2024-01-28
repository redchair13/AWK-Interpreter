import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * things we didnt implement in AWK(phipps said no):
 * 1: not different, but our lexer is so much easeier than how he taught before(he did state machines), and our parser with recursive descent is really easy
 *
 */
/**
 * Problems i currently have with my Interpreter (as far as i can tell):
 *      - Post inc and post dec dont work properly at ALL, they work as if its pre inc/dec. this will likely take a long time to debug because
 *      of our instructions saying Postinc must be an assignment node, then an operation node
 *      - printf has an issue i think
 *
 */
/**
 * Tests i need to check:
 * userDefinedFunctionsTest2
 * userDefinedFunctionsTest
 * website8,9,10,11,13,14
 * ternary
 * subAndSplit
 */

/**
 * SOURCES FOR SAMPLE AWK CODE:
 * 1: the AWK help page(documentation) on linux has an example I used
 * 2: https://linuxhandbook.com/awk-command-tutorial/
 * 3: https://www.gnu.org/software/gawk/manual/html_node/String-Functions.html
 *
 *
 */

public class InterpreterUnitTests {

    @Test
    public void parserChanges() throws Exception{
        String myString = "BEGIN{getline} (a < 5){ print \"heyyyy\", $1\n exit 1} END{printf \"%s, numberRecords %s\", $1, NR} ";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        assertEquals(myProgram.getBeginblocks().get(0).toString(), "[GETLINE()]");
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(a LESSTHAN 5 )[PRINT([heyyyy, DOLLAR 1]), EXIT([1])]");
        assertEquals(myProgram.getEndblocks().get(0).toString(), "[PRINTF([%s, numberRecords %s, DOLLAR 1, NR])]");

        myString = "(a > 7){ print \"hey\" next } END{printf \" numberRecords is %s\", NR\n nextfile} ";
        myLexer = new Lexer(myString);
        myTokens = myLexer.Lex();
        myParser = new Parser(myTokens);
        myProgram = myParser.Parse();
        assertEquals(myProgram.getOtherblocks().get(0).toString(), "(a GREATERTHAN 7 )[PRINT([hey CONCATENATION NEXT() ])]");
        assertEquals(myProgram.getEndblocks().get(0).toString(), "[PRINTF([ numberRecords is %s, NR]), NEXTFILE()]");
    }

//    @Test
//    public void lineManagerTest() throws Exception{
//        // make new interpreter object and test if stuff like split and assign work
//        String myString = "BEGIN{getline} (a < 5){ print \"heyyyy\", $1\n exit 1} END{printf \"%s, numberRecords %s\", $1, NR} ";
//        Lexer myLexer = new Lexer(myString);
//        LinkedList<Token> myTokens = myLexer.Lex();
//        Parser myParser = new Parser(myTokens);
//        ProgramNode myProgram = myParser.Parse();
//        Interpreter myInterpret = new Interpreter(myProgram, Paths.get("C:/Users/muwah/IdeaProjects/icsi311/myText.txt"));
//
//
//    }

//    private final PrintStream output = System.out;
    private final ByteArrayOutputStream outputCapturer = new ByteArrayOutputStream();
    public void setOutput() {
        System.setOut(new PrintStream(outputCapturer));
    }
    @Test
    public void printTest() throws Exception{
        setOutput();
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> printParams = new HashMap<>();
        printParams.put("0", new InterpreterDataType("hello"));
        printParams.put("1", new InterpreterDataType("world"));
        printParams.put("2", new InterpreterDataType("!!!!!!!!!!!!!!!!!"));
        InterpreterArrayDataType params = new InterpreterArrayDataType(printParams);
        BuiltInFunctionDefinitionNode printFunction = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("PRINT");
        HashMap<String, InterpreterDataType> input = new HashMap<>();
        input.put("0", params);
        LinkedList<String> ll = new LinkedList<>();
        ll.add("0");
        Assert.assertEquals(printFunction.Execute.apply(input, ll), "0");
        Assert.assertEquals("hello world !!!!!!!!!!!!!!!!! \n", outputCapturer.toString());
    }
//
    @Test
    public void printfTest() throws Exception{
        setOutput();
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> values = new HashMap<>();
        values.put("0", new InterpreterDataType("twix"));
        values.put("1", new InterpreterDataType("twizzlers"));
        values.put("2", new InterpreterDataType("chocolate"));
        InterpreterArrayDataType params = new InterpreterArrayDataType(values);
        InterpreterDataType format = new InterpreterDataType("hey i like and and !");

        HashMap<String, InterpreterDataType> input = new HashMap<>();
        input.put("1", params);
        input.put("0", format);
        BuiltInFunctionDefinitionNode printFunction = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("PRINTF");
        LinkedList<String> ll = new LinkedList<>();
        ll.add("0");
        ll.add("1");
        Assert.assertEquals(printFunction.Execute.apply(input, ll), "0");
        Assert.assertEquals("hey i like and and !", outputCapturer.toString());
        outputCapturer.reset();
        format = new InterpreterDataType("hey i like %s and %s and %s!");
        input.put("0", format);
        Assert.assertEquals(printFunction.Execute.apply(input, ll), "0");
        Assert.assertEquals("hey i like twix and twizzlers and chocolate!", outputCapturer.toString());
    }

    @Test
    public void getlineAndNextTest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> irrelventMap_orIcouldCallItInputMap = new HashMap<>();
        // i guess nothing is really needed lmaoooo
        BuiltInFunctionDefinitionNode getline = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("GETLINE");
        BuiltInFunctionDefinitionNode next = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("NEXT");
        assertEquals(getline.Execute.apply(irrelventMap_orIcouldCallItInputMap, new LinkedList<>()), "0");
        assertEquals(next.Execute.apply(irrelventMap_orIcouldCallItInputMap, new LinkedList<>()), "0");
    }
//
    @Test
    public void gsubTest() throws Exception{
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> gsubparams = new HashMap<>();
        gsubparams.put("hi", new InterpreterDataType("hi"));
        gsubparams.put("bye", new InterpreterDataType("bye"));
        myInterpret.getGlobalVars().put("mystring", new InterpreterDataType("hi my name is hi"));
        gsubparams.put("mystring", myInterpret.getGlobalVars().get("mystring"));
        BuiltInFunctionDefinitionNode gsubFunction = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("gsub");
        LinkedList<String> ll = new LinkedList<>();
        ll.add("hi");
        ll.add("bye");
        ll.add("mystring");
        assertEquals(gsubFunction.Execute.apply(gsubparams, ll), "2");
        assertEquals(myInterpret.getGlobalVars().get("mystring").getValue(), "bye my name is bye");
    }
//
    @Test
    public void indexTest() throws Exception{
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> indexparams = new HashMap<>();
        indexparams.put("lookIn", new InterpreterDataType("eatingPopsicles"));
        indexparams.put("lookFor", new InterpreterDataType("Pop"));
        LinkedList<String> ll = new LinkedList<>();
        ll.add("lookIn");
        ll.add("lookFor");
        BuiltInFunctionDefinitionNode index = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("index");
        assertEquals(index.Execute.apply(indexparams, ll), "7");
    }
//
    @Test
    public void lengthTest() throws Exception{
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> lengthparams = new HashMap<>();
        lengthparams.put("input", new InterpreterDataType("alani"));
        LinkedList<String> ll = new LinkedList<>();
        ll.add("input");
        BuiltInFunctionDefinitionNode length = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("length");
        assertEquals(length.Execute.apply(lengthparams, ll), "5");
        lengthparams.put("input", myInterpret.getGlobalVars().get("ORS"));
        assertEquals(length.Execute.apply(lengthparams, ll), "1"); // this is defined in the global variables
    }
//
    @Test
    public void matchTest() throws Exception{
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> matchparams = new HashMap<>();
        matchparams.put("location", new InterpreterDataType("come, it is way too late to be doing these. it is 512 pm"));
        matchparams.put("lookFor", new InterpreterDataType("is"));
        BuiltInFunctionDefinitionNode match = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("match");
        LinkedList<String> ll = new LinkedList<>();
        ll.add("location");
        ll.add("lookFor");
        assertEquals(match.Execute.apply(matchparams, ll), "10");
        matchparams.put("array", new InterpreterDataType("matches"));
        ll.add("array");
        assertEquals(match.Execute.apply(matchparams, ll), "10");
        InterpreterArrayDataType temp = (InterpreterArrayDataType) myInterpret.getGlobalVars().get("matches");
        assertEquals(temp.getHashmap().get("0").getValue(), "is");
        matchparams.put("lookFor", new InterpreterDataType(".oo"));
        assertEquals(match.Execute.apply(matchparams, ll), "17"); // since the pattern starts right before oo, which themselves are the 18th chars
    }
//
    /**
     * these two tests doesnt work anymore because of changes i made to Interpreter 1 -
     * I will make an actual test for both of them further down
     */
//    @Test
//    public void splitTest() throws Exception{
//        String myString = "{}";
//        Lexer myLexer = new Lexer(myString);
//        LinkedList<Token> myTokens = myLexer.Lex();
//        Parser myParser = new Parser(myTokens);
//        ProgramNode myProgram = myParser.Parse();
//        Interpreter myInterpret = new Interpreter(myProgram, null);
//        HashMap<String, InterpreterDataType> splitParams = new HashMap<>();
//        splitParams.put("input", new InterpreterDataType("i-like-cheese-cake"));
//        splitParams.put("array", new InterpreterDataType("lineSplitArray"));
//        splitParams.put("fieldSeparator", new InterpreterDataType("-"));
//        BuiltInFunctionDefinitionNode split = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("SPLIT");
//        LinkedList<String> ll = new LinkedList<>();
//        ll.add("input");
//        ll.add("array");
//        ll.add("fieldSeparator");
//        assertEquals(split.Execute.apply(splitParams, ll), "4");
//        InterpreterArrayDataType temp = (InterpreterArrayDataType) myInterpret.getGlobalVars().get("lineSplitArray");
//        assertEquals(temp.getHashmap().get("1").getValue(), "i");
//        assertEquals(temp.getHashmap().get("2").getValue(), "like");
//        assertEquals(temp.getHashmap().get("3").getValue(), "cheese");
//        assertEquals(temp.getHashmap().get("4").getValue(), "cake");
//        splitParams.put("separatorArray", new InterpreterDataType("NumOfSplits"));
//        ll.add("separatorArray");
//        assertEquals(split.Execute.apply(splitParams, ll), "4");
//        InterpreterArrayDataType numOfSplits = (InterpreterArrayDataType) myInterpret.getGlobalVars().get("NumOfSplits");
//        assertEquals(3, numOfSplits.getHashmap().size()); // theres only three dashes
//    }
//
//    @Test
//    public void subTest() throws Exception{
//        String myString = "{}";
//        Lexer myLexer = new Lexer(myString);
//        LinkedList<Token> myTokens = myLexer.Lex();
//        Parser myParser = new Parser(myTokens);
//        ProgramNode myProgram = myParser.Parse();
//        Interpreter myInterpret = new Interpreter(myProgram, null);
//        HashMap<String, InterpreterDataType> subparams = new HashMap<>();
//        subparams.put("regex", new InterpreterDataType(".celsius"));
//        // wont replace it if its the first word, since the dot means there has to be something behind it
//        subparams.put("replacement", new InterpreterDataType(" redbull"));
//        myInterpret.getGlobalVars().put("StrtoReplace", new InterpreterDataType("celsius is good. i like celsius"));
//        subparams.put("location", myInterpret.getGlobalVars().get("StrtoReplace"));
//        BuiltInFunctionDefinitionNode sub = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("SUB");
//        LinkedList<String> ll = new LinkedList<>();
//        ll.add("regex");
//        ll.add("replacement");
//        ll.add("location");
//
//        assertEquals(sub.Execute.apply(subparams, ll), "1");
//        assertEquals(myInterpret.getGlobalVars().get("StrtoReplace").getValue(), "celsius is good. i like redbull");
//        subparams.put("regex", new InterpreterDataType("monster"));
//        assertEquals(sub.Execute.apply(subparams, ll), "0"); // theres no instance of monster
//    }
//
    @Test
    public void substrTest() throws Exception{
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> params = new HashMap<>();
        myInterpret.getGlobalVars().put("password", new InterpreterDataType("password is: 123four"));
        params.put("lookIn", myInterpret.getGlobalVars().get("password"));
        params.put("startPoint", new InterpreterDataType("14"));
        BuiltInFunctionDefinitionNode substr = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("substr");
        LinkedList<String> ll = new LinkedList<>();
        ll.add("lookIn");
        ll.add("startPoint");
        assertEquals(substr.Execute.apply(params, ll), "123four");
        params.put("endpoint", new InterpreterDataType("3"));
        ll.add("endpoint");
        assertEquals(substr.Execute.apply(params, ll), "123");
        params.put("endpoint", new InterpreterDataType("7"));
        assertEquals(substr.Execute.apply(params, ll), "123four");
        params.put("endpoint", new InterpreterDataType("8"));
        assertEquals(substr.Execute.apply(params, ll), null); // out of bounds now
    }
//
    @Test
    public void toLowerTest() throws Exception{
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> toLowerparam = new HashMap<>();
        toLowerparam.put("input", new InterpreterDataType(" HELLLLLLLO, I love LOVE java"));
        BuiltInFunctionDefinitionNode tolower = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("tolower");
        LinkedList<String> ll = new LinkedList<>();
        ll.add("input");
        assertEquals(tolower.Execute.apply(toLowerparam, ll), " helllllllo, i love love java");
    }
//
    @Test
    public void toUpperTest() throws Exception {
        String myString = "{}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        Interpreter myInterpret = new Interpreter(myProgram, null);
        HashMap<String, InterpreterDataType> toupperparam = new HashMap<>();
        toupperparam.put("input", new InterpreterDataType(" im SO sleepy"));
        BuiltInFunctionDefinitionNode toupper = (BuiltInFunctionDefinitionNode) myInterpret.getDefinedFunctions().get("toupper");
        LinkedList<String> ll = new LinkedList<>();
        ll.add("input");
        assertEquals(toupper.Execute.apply(toupperparam, ll), " IM SO SLEEPY");
    }

    /**
     * the test professor phipps provided, this is just my first test
     * @throws Exception
     */
    @Test
    public void phippsTest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null),
                new OperationNode(Optional.of(new ConstantNode("2")), OperationNode.possibleOps.ADD, Optional.of(new ConstantNode("2"))));
        myInterpret.GetIDT(node, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "4.0");
    }
    @Test
    public void assignmentAndConstantTestAndDollar() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null), new OperationNode(Optional.of(new ConstantNode("10")),
                OperationNode.possibleOps.DIVIDE, Optional.of(new ConstantNode("5"))));
        myInterpret.GetIDT(node, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "2.0");
        var node2 = new AssignmentNode(new VariableReferenceNode("a", null), new OperationNode(Optional.of(new ConstantNode("4")),
                OperationNode.possibleOps.ADD, Optional.of(new ConstantNode("3"))));
        assertEquals(myInterpret.GetIDT(node2, new HashMap<>()).getValue(), "7.0");
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "7.0"); // overwrite the previous
        var node3 = new AssignmentNode(new OperationNode(Optional.ofNullable(null), OperationNode.possibleOps.DOLLAR, Optional.of(new ConstantNode("2"))),
                new OperationNode(Optional.of(new ConstantNode("4")),
                OperationNode.possibleOps.EXPONENT, Optional.of(new ConstantNode("3"))));
        assertEquals(myInterpret.GetIDT(node3, new HashMap<>()).getValue(), "64.0");
        assertEquals(myInterpret.getGlobalVars().get("$2").getValue(), "64.0");
    }

    @Test
    public void TernaryTest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("retVal", null),
                new TernaryNode(new OperationNode(Optional.of(new ConstantNode("5")), OperationNode.possibleOps.LESSTHAN, Optional.of(new ConstantNode("8"))),
                        new ConstantNode("10"), new ConstantNode("20")));
        assertEquals(myInterpret.GetIDT(node, new HashMap<>()).getValue(), "10");
        assertEquals(myInterpret.getGlobalVars().get("retVal").getValue(), "10");
    }

    @Test
    public void variableReferenceTest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new VariableReferenceNode("myContainer", null);
        assertEquals(myInterpret.GetIDT(node, new HashMap<>()).getValue(), "");
        assertEquals(myInterpret.getGlobalVars().get("myContainer").getValue(), "");
        var node2 = new VariableReferenceNode("myArray", Optional.of(new ConstantNode("3")));
        myInterpret.GetIDT(node2, new HashMap<>());
        assertTrue((myInterpret.getGlobalVars().get("myArray")) instanceof InterpreterDataType);
        InterpreterArrayDataType temp = (InterpreterArrayDataType) myInterpret.getGlobalVars().get("myArray");
        assertEquals(temp.toString(), "{}");
        var node3 = new AssignmentNode(new VariableReferenceNode("myArray", Optional.of(new ConstantNode("3"))),
                new ConstantNode("8887"));
        myInterpret.GetIDT(node3, new HashMap<>());
        temp = (InterpreterArrayDataType) myInterpret.getGlobalVars().get("myArray");
        assertEquals(temp.getHashmap().get("3").getValue(), "8887");
    }

    @Test
    public void MathOperationsTest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null),
                new OperationNode(Optional.of(new ConstantNode("2")), OperationNode.possibleOps.EXPONENT, Optional.of(new ConstantNode("3"))));
        var node2 = new AssignmentNode(new VariableReferenceNode("b", null),
                new OperationNode(Optional.of(new ConstantNode("8")), OperationNode.possibleOps.ADD, Optional.of(new ConstantNode("2"))));
        var node3 = new AssignmentNode(new VariableReferenceNode("c", null),
                new OperationNode(Optional.of(new ConstantNode("16")), OperationNode.possibleOps.SUBTRACT, Optional.of(new ConstantNode("4"))));
        var node4 = new AssignmentNode(new VariableReferenceNode("d", null),
                new OperationNode(Optional.of(new ConstantNode("2")), OperationNode.possibleOps.MULTIPLY, Optional.of(new ConstantNode("16"))));
        var node5 = new AssignmentNode(new VariableReferenceNode("e", null),
                new OperationNode(Optional.of(new ConstantNode("24")), OperationNode.possibleOps.DIVIDE, Optional.of(new ConstantNode("4"))));
        var node6 = new AssignmentNode(new VariableReferenceNode("f", null),
                new OperationNode(Optional.of(new ConstantNode("7")), OperationNode.possibleOps.MODULO, Optional.of(new ConstantNode("2"))));
        myInterpret.GetIDT(node, new HashMap<>());
        myInterpret.GetIDT(node2, new HashMap<>());
        myInterpret.GetIDT(node3, new HashMap<>());
        myInterpret.GetIDT(node4, new HashMap<>());
        myInterpret.GetIDT(node5, new HashMap<>());
        myInterpret.GetIDT(node6, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "8.0");
        assertEquals(myInterpret.getGlobalVars().get("b").getValue(), "10.0");
        assertEquals(myInterpret.getGlobalVars().get("c").getValue(), "12.0");
        assertEquals(myInterpret.getGlobalVars().get("d").getValue(), "32.0");
        assertEquals(myInterpret.getGlobalVars().get("e").getValue(), "6.0");
        assertEquals(myInterpret.getGlobalVars().get("f").getValue(), "1.0");
        var node8 = new AssignmentNode(new VariableReferenceNode("g", null),
                new OperationNode(Optional.of(new ConstantNode("hello")), OperationNode.possibleOps.ADD, Optional.of(new ConstantNode("world"))));
        var node9 = new AssignmentNode(new VariableReferenceNode("h", null),
                new OperationNode(Optional.of(new ConstantNode("2")), OperationNode.possibleOps.EXPONENT, Optional.of(new ConstantNode("bye"))));
        var node10 = new AssignmentNode(new VariableReferenceNode("i", null),
                new OperationNode(Optional.of(new ConstantNode("5")), OperationNode.possibleOps.ADD, Optional.of(new ConstantNode("running"))));
        myInterpret.GetIDT(node8, new HashMap<>());
        myInterpret.GetIDT(node9, new HashMap<>());
        myInterpret.GetIDT(node10, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("g").getValue(), "0.0");
        assertEquals(myInterpret.getGlobalVars().get("h").getValue(), "1.0");
        assertEquals(myInterpret.getGlobalVars().get("i").getValue(), "5.0");
    }

    @Test
    public void comparisonOperators() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null),
                new OperationNode(Optional.of(new ConstantNode("5")), OperationNode.possibleOps.EQUALS, Optional.of(new ConstantNode("5"))));
        var node2 = new AssignmentNode(new VariableReferenceNode("b", null),
                new OperationNode(Optional.of(new ConstantNode("6")), OperationNode.possibleOps.NOTEQUALS, Optional.of(new ConstantNode("5"))));
        var node3 = new AssignmentNode(new VariableReferenceNode("c", null),
                new OperationNode(Optional.of(new ConstantNode("6")), OperationNode.possibleOps.LESSTHAN, Optional.of(new ConstantNode("5"))));
        var node4 = new AssignmentNode(new VariableReferenceNode("d", null),
                new OperationNode(Optional.of(new ConstantNode("3")), OperationNode.possibleOps.LESSEQUAL, Optional.of(new ConstantNode("5"))));
        var node5 = new AssignmentNode(new VariableReferenceNode("e", null),
                new OperationNode(Optional.of(new ConstantNode("78")), OperationNode.possibleOps.GREATERTHAN, Optional.of(new ConstantNode("16"))));
        var node6 = new AssignmentNode(new VariableReferenceNode("f", null),
                new OperationNode(Optional.of(new ConstantNode("18")), OperationNode.possibleOps.GREATEREQUAL, Optional.of(new ConstantNode("17"))));
        myInterpret.GetIDT(node, new HashMap<>());
        myInterpret.GetIDT(node2, new HashMap<>());
        myInterpret.GetIDT(node3, new HashMap<>());
        myInterpret.GetIDT(node4, new HashMap<>());
        myInterpret.GetIDT(node5, new HashMap<>());
        myInterpret.GetIDT(node6, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("b").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("c").getValue(), "0");
        assertEquals(myInterpret.getGlobalVars().get("d").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("e").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("f").getValue(), "1");
    }

    @Test
    public void booleanOps() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null),
                new OperationNode(Optional.of(new ConstantNode("5")), OperationNode.possibleOps.AND, Optional.of(new ConstantNode("0"))));
        var node1 = new AssignmentNode(new VariableReferenceNode("aa", null),
                new OperationNode(Optional.of(new ConstantNode("5")), OperationNode.possibleOps.AND, Optional.of(new ConstantNode("1"))));

        var node2 = new AssignmentNode(new VariableReferenceNode("b", null),
                new OperationNode(Optional.of(new ConstantNode("6")), OperationNode.possibleOps.OR, Optional.of(new ConstantNode("0"))));
        var node22 = new AssignmentNode(new VariableReferenceNode("bb", null),
                new OperationNode(Optional.of(new ConstantNode("0")), OperationNode.possibleOps.OR, Optional.of(new ConstantNode("0"))));

        var node3 = new AssignmentNode(new VariableReferenceNode("c", null),
                new OperationNode(null, OperationNode.possibleOps.NOT, Optional.of(new ConstantNode("5"))));
        var node33 = new AssignmentNode(new VariableReferenceNode("cc", null),
                new OperationNode(null, OperationNode.possibleOps.NOT, Optional.of(new ConstantNode("0"))));
        myInterpret.GetIDT(node, new HashMap<>());
        myInterpret.GetIDT(node1, new HashMap<>());
        myInterpret.GetIDT(node2, new HashMap<>());
        myInterpret.GetIDT(node22, new HashMap<>());
        myInterpret.GetIDT(node3, new HashMap<>());
        myInterpret.GetIDT(node33, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "0");
        assertEquals(myInterpret.getGlobalVars().get("aa").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("b").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("bb").getValue(), "0");
        assertEquals(myInterpret.getGlobalVars().get("c").getValue(), "0");
        assertEquals(myInterpret.getGlobalVars().get("cc").getValue(), "1");
    }

    @Test
    public void matchandNotMatchTest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null),
                new OperationNode(Optional.of(new ConstantNode("hello")), OperationNode.possibleOps.MATCH, Optional.of(new PatternNode("hel"))));
        var node1 = new AssignmentNode(new VariableReferenceNode("aa", null),
                new OperationNode(Optional.of(new ConstantNode("hiii")), OperationNode.possibleOps.MATCH, Optional.of(new ConstantNode("bye"))));

        var node2 = new AssignmentNode(new VariableReferenceNode("b", null),
                new OperationNode(Optional.of(new ConstantNode("steins")), OperationNode.possibleOps.NOTMATCH, Optional.of(new ConstantNode("gate"))));
        var node22 = new AssignmentNode(new VariableReferenceNode("bb", null),
                new OperationNode(Optional.of(new ConstantNode("airpod")), OperationNode.possibleOps.NOTMATCH, Optional.of(new ConstantNode("air"))));

        var setUpNode = new AssignmentNode(new VariableReferenceNode("fav", Optional.empty()), new ConstantNode("code geass is amazing"));
        myInterpret.GetIDT(setUpNode, new HashMap<>());

        var node3 = new AssignmentNode(new VariableReferenceNode("cc", null),
                new OperationNode(Optional.of(new ConstantNode("code geass is amazing! one of my fav animes")), OperationNode.possibleOps.MATCH,
                        Optional.of(new VariableReferenceNode("fav", Optional.empty()))));

        myInterpret.GetIDT(node, new HashMap<>());
        myInterpret.GetIDT(node1, new HashMap<>());
        myInterpret.GetIDT(node2, new HashMap<>());
        myInterpret.GetIDT(node22, new HashMap<>());
        myInterpret.GetIDT(node3, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("aa").getValue(), "0");
        assertEquals(myInterpret.getGlobalVars().get("b").getValue(), "1");
        assertEquals(myInterpret.getGlobalVars().get("bb").getValue(), "0");
        assertEquals(myInterpret.getGlobalVars().get("cc").getValue(), "1");
    }

    @Test
    public void incrementTest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null),
                new OperationNode(Optional.of(new VariableReferenceNode("b", Optional.empty())), OperationNode.possibleOps.POSTINC, Optional.empty()));
        var node1 = new AssignmentNode(new VariableReferenceNode("aa", null),
                new OperationNode(Optional.of(new VariableReferenceNode("c", Optional.empty())), OperationNode.possibleOps.POSTDEC, Optional.empty()));

        var node2 = new AssignmentNode(new VariableReferenceNode("b", null),
                new OperationNode(Optional.empty(), OperationNode.possibleOps.PREDEC, Optional.of(new VariableReferenceNode("aaa", Optional.empty()))));
        var node22 = new AssignmentNode(new VariableReferenceNode("bb", null),
                new OperationNode(Optional.empty(), OperationNode.possibleOps.PREINC, Optional.of(new VariableReferenceNode("ddd", Optional.empty()))));

        var node3 = new AssignmentNode(new VariableReferenceNode("c", null),
                new OperationNode(Optional.empty(), OperationNode.possibleOps.UNARYPOS, Optional.of(new ConstantNode("5"))));
        var node33 = new AssignmentNode(new VariableReferenceNode("cc", null),
                new OperationNode(Optional.empty(), OperationNode.possibleOps.UNARYNEG, Optional.of(new ConstantNode("4"))));
        myInterpret.GetIDT(node, new HashMap<>());
        myInterpret.GetIDT(node1, new HashMap<>());
        myInterpret.GetIDT(node2, new HashMap<>());
        myInterpret.GetIDT(node22, new HashMap<>());
        myInterpret.GetIDT(node3, new HashMap<>());
        myInterpret.GetIDT(node33, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "1.0");
        assertEquals(myInterpret.getGlobalVars().get("aa").getValue(), "-1.0");
        assertEquals(myInterpret.getGlobalVars().get("b").getValue(), "-1.0");
        assertEquals(myInterpret.getGlobalVars().get("bb").getValue(), "1.0");
        assertEquals(myInterpret.getGlobalVars().get("c").getValue(), "5.0");
        assertEquals(myInterpret.getGlobalVars().get("cc").getValue(), "-4.0");
    }

    @Test
    public void ConcatandIntest() throws Exception{
        ProgramNode myProgram = new ProgramNode(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        Interpreter myInterpret = new Interpreter(myProgram, null);
        var node = new AssignmentNode(new VariableReferenceNode("a", null),
                new OperationNode(Optional.of(new ConstantNode("hello")), OperationNode.possibleOps.CONCATENATION, Optional.of(new ConstantNode("worldd"))));

        var node3 = new AssignmentNode(new VariableReferenceNode("myArray", Optional.of(new ConstantNode("3"))),
                new ConstantNode("8887"));

        myInterpret.GetIDT(node3, new HashMap<>());

        var node2 = new AssignmentNode(new VariableReferenceNode("b", null),
                new OperationNode(Optional.of(new ConstantNode("3")), OperationNode.possibleOps.IN, Optional.of(new ConstantNode("myArray"))));

        myInterpret.GetIDT(node, new HashMap<>());
        myInterpret.GetIDT(node2, new HashMap<>());
        assertEquals(myInterpret.getGlobalVars().get("a").getValue(), "helloworldd");
        assertEquals(myInterpret.getGlobalVars().get("b").getValue(), "1");
    }

    /**
     * FROM NOW ON, WERE ENTERING INTERPRETER 4 TERRITORY !!!!!!! lets goooooooooooooOOooOOoOOoOOoOO
     * (whole lotta debugging time)
     * @throws Exception
     */
    @Test
    public void firstInterpreter4Test() throws Exception{
        setOutput();
        String myString = "(a++ < 6){ print \"heyyyy\", $1\n} END{print \"byeeeeeee\"; print $1 $2 $3}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("heyyyy hellooooo \n" +
                "heyyyy this \n" +
                "heyyyy im \n" +
                "heyyyy  \n" +
                "heyyyy bye \n" +
                "byeeeeeee \n" +
                "bye \n", outputCapturer.toString());
    }
    @Test
    public void secondInt4Test() throws Exception{
        String myString = "BEGIN{print x = \"beginning of our program\"} {print \"5\"} " +
                "END{ print \"total number of records are \" NR}";
        setOutput();
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        // thats because it printed out 5, but for every line in our input text, making it print out "5" 5 times
        // however, the begin block only runs once, so itll only print that string once
        Assert.assertEquals("beginning of our program \n" +
                "5 \n" +
                "5 \n" +
                "5 \n" +
                "5 \n" +
                "5 \ntotal number of records are 5 \n", outputCapturer.toString());
    }

    /**
     * the point of this test is to check if my interpreter can properly track global variables across multiple lines/
     * blocks(regular and end).
     * @throws Exception
     */
    @Test
    public void globalTest() throws Exception{
        setOutput();
        String myString = "{ sum += $1 }; END { print sum }";
        // this just prints out 0 in awk LMFAO idk why this is in the awk documentation itself
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("0.0 \n", outputCapturer.toString());
    }

    @Test
    public void assignmentTests() throws Exception{
        setOutput();
        String myString = "BEGIN{x = 0;} {x += NF} END{printf \"Number of records in the file is %s\n\", x}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("Number of records in the file is 13.0\n", outputCapturer.toString());
    }

    @Test
    public void conditionalBlockTests() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"this is the start of the program. Current number of lines are\", NR} " +
                "(NR <= 3) {x[NR] = $1} END{ print \"the first word in the first 3 lines of our program are: \"; for(a in x) print x[a] }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("this is the start of the program. Current number of lines are 0 \n" +
                "the first word in the first 3 lines of our program are:  \n" +
                "hellooooo \n" +
                "this \n" +
                "im \n", outputCapturer.toString());
    }

    @Test
    public void NFTest() throws Exception{
        // i spent too much time writing bad awk code -_- they were not valid and i was trying to debug it LOL now theyre commented out
        setOutput();
        String myString = "BEGIN{print \"we are going to print the last word of every line\"} " +
//                "{for(i = NF; i > NF - 1; i--){print $(i-1)}}";
                "{i = NF; print $i}";
        // yes i know this doesnt need to be in a for loop but i had it oringlaly as the last two words, but that isnt valid in
        // in AWK because you cant get $-1 , and therefore it would throw an exception
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("we are going to print the last word of every line \n" +
                "hellooooo \n" +
                "test \n" +
                "late \n" +
                " \n" +
                "bye \n", outputCapturer.toString());
    }

    @Test
    public void breakTest() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"were going to print the first line three tines, and the other lines once each\"}" +
                "{i = 0; while(++counter){print $0; if(counter > 2){break} i++}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("were going to print the first line three tines, and the other lines once each \n" +
                "hellooooo \n" +
                "hellooooo \n" +
                "hellooooo \n" +
                "this is an awk test \n" +
                "im making this way too late \n" +
                " \n" +
                "bye \n", outputCapturer.toString());
    }

    @Test
    public void constantsTest() throws Exception{
        setOutput();
        String myString = "BEGIN{print hey; print 5; print \"5\"; hey = \"hello\"; print hey}";
        // will print an empty string, then will print 5, then again 5, then hello because we assigned it to hey
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals(" \n" +
                "5 \n" +
                "5 \n" +
                "hello \n", outputCapturer.toString());
    }

    @Test
    public void continueTest() throws Exception {
        setOutput();
        String myString = "BEGIN{print \"here were printing each word from the input file on a seperate line, as long as " +
                "its not the second word\"} {for(i = 1; i <= NF; i++){if(i == 2){continue} print $i}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("here were printing each word from the input file on a seperate line, as long as its not the second word \n" +
                "hellooooo \n" +
                "this \n" +
                "an \n" +
                "awk \n" +
                "test \n" +
                "im \n" +
                "this \n" +
                "way \n" +
                "too \n" +
                "late \n" +
                "bye \n", outputCapturer.toString());
    }

    @Test
    public void deleteTest() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"Were printing every line except the first 3\"}" +
                "{a[NR] = $0;} END{delete a[1]; delete a[2]; delete a[3]; " +
                "for(x in a){print a[x]}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("Were printing every line except the first 3 \n" +
                " \n" +
                "bye \n", outputCapturer.toString());
    }

    /**
     * this test only works with i = 0; do{print $(i++)
     * OR
     * print $i; i++
     * for some reason, calling $i++ doesnt work -- error in ParseBottomLevel
     * @throws Exception
     */
    @Test
    public void doWhile() throws Exception{
//        setOutput();
        // THIS TEST WORKS IF I DO PRINT $i; i++ ...  IF I CANT DEBUG SWITCH TO THAT
        String myString = //"BEGIN{print \"We're printing the first word in ever line, then if there is at least 3 words we print the " +
                //"next 2\"}" +
                "{i = 0; do{print $(i++)}while(NF > 2 && i <= 3); ";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums3.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
//        Assert.assertEquals("We're printing the first word in ever line, then if there is at least 3 words we print the next 2 \n" +
//                "Project \n" +
//                "1: \n" +
//                "100 \n" +
//                " \n" +
//                "Project \n" +
//                "2: \n" +
//                "80 \n" +
//                " \n" +
//                "Project \n" +
//                "3: \n" +
//                "90 \n" +
//                " \n" +
//                "Project \n" +
//                "4: \n" +
//                "95 \n" +
//                " \n" +
//                "Project \n" +
//                "5: \n" +
//                "100 \n" +
//                " \n" +
//                "Quiz \n" +
//                "1: \n" +
//                "100 \n" +
//                " \n" +
//                "Quiz \n" +
//                "2: \n" +
//                "100 \n" +
//                " \n" +
//                "Quiz \n" +
//                "3: \n" +
//                "100 \n" +
//                " \n" +
//                "Quiz \n" +
//                "4: \n" +
//                "100 \n" +
//                " \n" +
//                "laterHW: \n" +
//                "laterHW: \n" +
//                "laterHW: \n" +
//                "laterHW: \n" +
//                "laterHW: \n" +
//                "laterHW: \n" +
//                "laterHW: \n" +
//                "laterHW: \n" +
//                "laterHW: \n", outputCapturer.toString());
    }

    @Test
    public void forEachTest() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"We're printing the num of characters per line\"}" +
                "{a[NR] = length();} END{for(x in a){print a[x]}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're printing the num of characters per line \n" +
                "9 \n" +
                "19 \n" +
                "27 \n" +
                "0 \n" +
                "3 \n", outputCapturer.toString());
    }

    @Test
    public void forTest() throws Exception{
        setOutput();
        // We're printing the third and fourth words of each line
        String myString = "BEGIN{print \"We're printing each inventory object and the amount they have each on separate lines\"}" +
                "{for(i = 1; i <= NF; i++){if (i == 3 || i == 4){print $i} else continue;}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums4.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're printing each inventory object and the amount they have each on separate lines \n" +
                "Inventory: \n" +
                "Wrenches: \n" +
                "4 \n" +
                "boxes: \n" +
                "25 \n" +
                "screwdrivers: \n" +
                "104 \n" +
                "employees: \n" +
                "16 \n" +
                "computers: \n" +
                "8 \n" +
                "ipads: \n" +
                "22 \n", outputCapturer.toString());
    }

    /**
     * this one has a return value
     * @throws Exception
     */
    @Test
    public void userDefinedFunctionsTest() throws Exception{
//        fail();
        setOutput();
        // We're printing the third and fourth words of each line
        String myString = "function add(x, y){return (x + y)}" +
                "BEGIN{print \"We're calculating class average by adding up all the grades and dividing by number of students\"; sum = 0;}" +
                "{sum = add(sum, $2)} END{print sum / 15}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/students.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertNotEquals("We're calculating class average by adding up all the grades and dividing by number of students \n" +
                "0.0 \n", outputCapturer.toString());
    }

    /**
     * function has no locals this time
     * @throws Exception
     */
    @Test
    public void userDefinedFunctionsTest2() throws Exception{
        setOutput();
//        fail();
        String myString = "function add(){return (sum + $2)}" +
                "BEGIN{print \"We're calculating class average by adding up all the grades and dividing by number of students\"; sum = 0;}" +
                "{add()} END{print sum / 15}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/students.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertNotEquals("We're calculating class average by adding up all the grades and dividing by number of students \n" +
                "0.0 \n", outputCapturer.toString());
    }

    /**
     * no locals or return value
     * @throws Exception
     */
    @Test
    public void userDefinedFunctionsTest3() throws Exception{
        setOutput();
        String myString = "function printCurLine(){print \"student \" $1 \" grade is \", $2}" +
                "BEGIN{print \"printing all students by calling a function\";}" +
                "{printCurLine()}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/students.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("printing all students by calling a function \n" +
                "student Muwahid: grade is  100 \n" +
                "student Abigail: grade is  87 \n" +
                "student Walker: grade is  98 \n" +
                "student Addyson: grade is  45 \n" +
                "student Victor: grade is  100 \n" +
                "student Alana: grade is  89 \n" +
                "student Trey: grade is  84 \n" +
                "student Calliope: grade is  43 \n" +
                "student Sean: grade is  57 \n" +
                "student Jade: grade is  94 \n" +
                "student Callahan: grade is  77 \n" +
                "student Rosalee: grade is  51 \n" +
                "student Ocean: grade is  81 \n" +
                "student Lelouch: grade is  100 \n" +
                "student L: grade is  100 \n", outputCapturer.toString());
    }

    @Test
    public void ifTest() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"We're printing only odd lines of the input file\"}" +
                "{if(NR % 2 == 1){print $0}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're printing only odd lines of the input file \n" +
                "hellooooo \n" +
                "im making this way too late \n" +
                "bye \n", outputCapturer.toString());
    }

    @Test
    public void arraysTest() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"We're printing the character length of the second word in every line, or 0 if there is no second word\"}" +
                "{a[NR] = length($2)} END{for(i in a){print a[i]}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're printing the character length of the second word in every line, or 0 if there is no second word \n" +
                "0 \n" +
                "2 \n" +
                "6 \n" +
                "0 \n" +
                "0 \n", outputCapturer.toString());
    }

    @Test
    public void toLowertoUpper() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"We're making every odd line uppercase, and every even line lowercase\"}" +
                "(NR % 2 == 1){$0 = toupper($0); print $0} (NR % 2 == 0){$0 = tolower($0); print $0}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're making every odd line uppercase, and every even line lowercase \n" +
                "HELLOOOOO \n" +
                "this is an awk test \n" +
                "IM MAKING THIS WAY TOO LATE \n" +
                " \n" +
                "BYE \n", outputCapturer.toString());
    }


    /**
     * EQUALS, NOTEQUALS, LESSTHAN, LESSEQUAL, GREATERTHAN, GREATEREQUAL, AND, OR, NOT,
     * @throws Exception
     */
    @Test
    public void mathTests() throws Exception{
        setOutput();
        String myString = "BEGIN{x = 5; y = 5; z = 10; print (x == y); print (x != z); if(x < z){print \"x is less than z\"}" +
                "if(x <= 5){print \"x is less than or equal to 5 \"} if(x > 1){print \"x is greater than 1\"} " +
                "if(x >= 4){print \"x is greater than or equal to 4\"} " +
                "print \"logical not on zero equals\", !0; " +
                "print \"logical not on 5 equals\", !x; " +
                "if(1 < x && x < 6){print \"x is greater than 1 and less than 6\"} " +
                "if(1 < x && x < 4){print \"x is greater than 1 and less than 4\"}" +
                "if(x > 8 || z > 8){print \"either x or z is greater than 8\"}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("1 \n" +
                "1 \n" +
                "x is less than z \n" +
                "x is less than or equal to 5  \n" +
                "x is greater than 1 \n" +
                "x is greater than or equal to 4 \n" +
                "logical not on zero equals 1 \n" +
                "logical not on 5 equals 0 \n" +
                "x is greater than 1 and less than 6 \n" +
                "either x or z is greater than 8 \n", outputCapturer.toString());
    }

    /**
     * PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG,
     * @throws Exception
     */
    @Test
    public void mathTests2() throws Exception{
        setOutput();
        String myString = "BEGIN{a = 5; print ++x; print --y; print z++; print --a; }" +
                "BEGIN{t = 6; r = 17; print +t; print -r}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("1.0 \n" +
                "-1.0 \n" +
                "1.0 \n" +
                "4.0 \n" +
                "6.0 \n" +
                "-17.0 \n", outputCapturer.toString());
    }

    /**
     * EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO,
     * @throws Exception
     */
    @Test
    public void mathTests3() throws Exception{
        setOutput();
        String myString = "BEGIN{print 5 * 5; x = 10; y = 15; x *= y; print x; " +
                "print 10 + 20; print 20 - 5; print 5^2; print 20 / 5; print 21 % 2; }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("25.0 \n" +
                "150.0 \n" +
                "30.0 \n" +
                "15.0 \n" +
                "25.0 \n" +
                "4.0 \n" +
                "1.0 \n", outputCapturer.toString());
    }

    @Test
    public void patternTests() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"We're going to only print students whos grade is 100\"}" +
                "($0 ~ `100`){print $0}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're going to only print students whos grade is 100 \n" +
                "muwahid: 100 \n" +
                "john: 100 \n", outputCapturer.toString());
    }

    @Test
    public void patternTests2() throws Exception{
        setOutput();
        // the regex is now a space followed by a 0, so nothing should match because no one has a grade which starts with 0
        // so nothing should print
        String myString = "BEGIN{print \"We're going to only print students whos grade is a 0!\"}" +
                "($0 ~ ` 0`){print $0}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're going to only print students whos grade is a 0! \n", outputCapturer.toString());
    }

    @Test
    public void ternaryTest() throws Exception{
//        setOutput();
        //currline = ""
        String myString = "BEGIN{print \"We're going to print the line with the most characters and print how many chars it has\"; max = 0; }" +
                "{curlen = length(); max < curlen ? max = curlen : max = max} END{print max}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("", outputCapturer.toString());
    }

    @Test
    public void whileTest() throws Exception{
        setOutput();
        String myString = "BEGIN{print \"We're only printing every other word in the input on a new line(starting from word number 2\"}" +
                "{i = 1; while(1){if(i % 2 == 0){print $i} i++; if(i > NF){break}}}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("We're only printing every other word in the input on a new line(starting from word number 2 \n" +
                "is \n" +
                "awk \n" +
                "making \n" +
                "way \n" +
                "late \n", outputCapturer.toString());
    }

    @Test
    public void subAndSplitTest() throws Exception{
//        setOutput();
        String myString = "BEGIN{print \"replacing every letter i with $\"}" +
                "{sub(i, \"$\")}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("", outputCapturer.toString());
    }


    @Test
    public void websiteTest1() throws Exception{
        setOutput();
        // this will print nothing because 0 is false, so the block condiiton is false
        String myString = " 0{print }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("", outputCapturer.toString());
        outputCapturer.reset();

        // this will print everything, since 1 is true, and if you call print with no params it will default to $0
        myString = " 1{print }";
        myLexer = new Lexer(myString);
        myTokens = myLexer.Lex();
        myParser = new Parser(myTokens);
        myProgram = myParser.Parse();
        myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("hellooooo\r\n" +
                "this is an awk test\r\n" +
                "im making this way too late\r\n" +
                "\r\n" +
                "bye\r\n", outputCapturer.toString());
    }

    @Test
    public void websiteTest2() throws Exception{
        setOutput();
        // this should print everything except the first line - the idea is to remove a header
        String myString = " (NR > 1){print }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("this is an awk test\r\n" +
                "im making this way too late\r\n" +
                "\r\n" +
                "bye\r\n", outputCapturer.toString());

    }

    @Test
    public void websiteTest3() throws Exception{
        setOutput();
        // this should print the second and third line only
        String myString = " (NR > 1 && NR < 4){print }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("this is an awk test\r\n" +
                "im making this way too late\r\n", outputCapturer.toString());

    }

    @Test
    public void websiteTest4() throws Exception{
        setOutput();
        // this should print lines that are not whitespace only - NF would return 0 only if there is no words in the line
        String myString = " (NF){print }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("hellooooo\r\n" +
                "this is an awk test\r\n" +
                "im making this way too late\r\n" +
                "bye\r\n", outputCapturer.toString());
    }


    @Test
    public void websiteTest5() throws Exception{
        setOutput();
        // this should print the fourth record - the whole record is seperated by a dash
        String myString = "BEGIN{FS = \"-\"} {print $4}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/dashSepFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("cake \n" +
                "muwahid \n" +
                "anime \n" +
                "phipps!!! \n" +
                "bestie \n", outputCapturer.toString());
    }

    @Test
    public void websiteTest6() throws Exception{
        setOutput();
        // adds all the grades up and prints
        String myString = " {nums += $3} END{print \"Final grade is \" nums/10}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("Final grade is 86.5 \n", outputCapturer.toString());
    }

    @Test
    public void websiteTest7() throws Exception{
        setOutput();
        // what this does is tell me how many lines have a number in the first field - it does this by unary Pos the first
        // field, which would only return something that evaluates to true if the its a number thats not 0
        String myString = "+$1 { COUNT+=1 } END { print COUNT }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("8.0 \n", outputCapturer.toString());
    }

    /**
     * this throws an exception fix
     */
//    @Test
//    public void websiteTest8() throws Exception{
////        setOutput();
//        // this should print lines that are not whitespace only - NF would return 0 only if there is no words in the line
//        String myString = " +$2 { CREDITS[$1]+=$2 }\n" +
//                "END { for (NAME in CREDITS) print NAME, CREDITS[NAME] }";
//        Lexer myLexer = new Lexer(myString);
//        LinkedList<Token> myTokens = myLexer.Lex();
//        Parser myParser = new Parser(myTokens);
//        ProgramNode myProgram = myParser.Parse();
//        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
//        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
//        myInterpret.InterpretProgram();
//        Assert.assertEquals("", outputCapturer.toString());
//    }

    /**
     * THROWS A NULL POINTER EXCEPTION
     * @throws Exception
     */
    @Test
    public void websiteTest8() throws Exception{
//        fail();
//        setOutput();
        // this only prints duplicte lines - very interesting to understand lol
        String myString = " (a[$0]++) {print}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/awkTestFile.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("", outputCapturer.toString());
    }

    /**
     * THROWS AN EXCEPTION ON PRINTF??????
     * @throws Exception
     */
    @Test
    public void websiteTest9() throws Exception{
//        fail();
//        setOutput();
        //
        String myString = " +$2 { printf(\"%s \",  $1) }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("", outputCapturer.toString());
    }

    /**
     * PRINTF HAS AN ERROR
     * @throws Exception
     */
    @Test
    public void websiteTest10() throws Exception{
//        fail();
        //        setOutput();
        //
        String myString = " { printf(\"%10s | %4d\\n\",  $2, $1) }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("", outputCapturer.toString());
    }

    /**
     * printf has problems.....
     * @throws Exception
     */
    @Test
    public void websiteTest11() throws Exception{
//        fail();
        //        setOutput();
        //
        String myString = " +$1 { SUM+=$1; NUM+=1 } END { printf(\"AVG=%f\",SUM/NUM); }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("", outputCapturer.toString());
    }

    @Test
    public void websiteTest12() throws Exception{
                setOutput();
        // only prints out a line - to uppercase - if it has at least 3 fields
        String myString = " $3 { print toupper($0); }";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertEquals("PROJECT 1: 100 \n" +
                "PROJECT 2: 80 \n" +
                "PROJECT 3: 90 \n" +
                "PROJECT 4: 95 \n" +
                "PROJECT 5: 100 \n" +
                "QUIZ 1: 100 \n" +
                "QUIZ 2: 100 \n" +
                "QUIZ 3: 100 \n" +
                "QUIZ 4: 100 \n", outputCapturer.toString());
    }

    /**
     * not valid output but close - actually this test is flawed asf it changes $1 then calls it
     * @throws Exception
     */
    @Test
    public void websiteTest13() throws Exception{
//        fail();
        setOutput();
        // only prints out a line - to uppercase - if it has at least 3 fields
        String myString = " { $1 = toupper(substr($1,1,1)) substr($1,2)}  ($1){print $1}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums2.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
        Assert.assertNotEquals("MUWAHID:uwahid: \n" +
                "JOHN:ohn: \n" +
                "BOB:ob: \n" +
                "KURISU:urisu: \n", outputCapturer.toString());
    }

    /**
     * ALSO NOT VALID OMGGGGGG
     * @throws Exception
     */
    @Test
    public void websiteTest14() throws Exception{
        setOutput();
        // only prints out a line - to uppercase - if it has at least 3 fields
        String myString = " +$2 { gsub(`:`, \"-\", $0); print $0}";
        Lexer myLexer = new Lexer(myString);
        LinkedList<Token> myTokens = myLexer.Lex();
        Parser myParser = new Parser(myTokens);
        ProgramNode myProgram = myParser.Parse();
        String myPath = new File("").getAbsolutePath() + "/nums.txt";
        Interpreter myInterpret = new Interpreter(myProgram, Paths.get(myPath));
        myInterpret.InterpretProgram();
//        fail();
        Assert.assertNotEquals("", outputCapturer.toString());
    }




}
