import com.sun.jdi.event.BreakpointEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Interpreter {
    public class LineManager {
        private List<String> input; // the input file we'll be reading

        /**
         * we use this every time we want to get a new line. we are able to split everything in it
         * to its proper global variable. this is to make it easy for us later in the interpreter, when
         * we are going to have to go over every single line in the wk program and perform operations on them
         *
         * @return
         */
        public boolean SplitAndAssign() {
            if (input.size() == 0) {
                return false;
            }
            InterpreterDataType FS = globalVariables.get("FS");
            InterpreterDataType tempfnr = globalVariables.get("FNR");
            if (tempfnr == null) {
                tempfnr = new InterpreterDataType("0");
            }
            if(Integer.parseInt(tempfnr.getValue()) >= input.size() ){
                return false;
            }
            String currLine = input.get(Integer.parseInt(tempfnr.getValue())); // gotta fix this
            if (currLine == null) {
                return false;
            }
            //  AWK clears the previous dollar sign variabls every time split and assign is called
            for(int i = 0; i < globalVariables.size(); i++){
                if(globalVariables.containsKey("$" + i)){
                    globalVariables.remove("$" + i);
                }
                else{
                    break;
                }
            }
            globalVariables.put("$0", new InterpreterDataType(currLine));
            String[] splitLine = currLine.split(FS.getValue());
            for (int i = 1; i <= splitLine.length; i++) {
                globalVariables.put("$" + i, new InterpreterDataType(splitLine[i - 1]));
            }
            if(currLine.equals("")){
                globalVariables.put("NF", new InterpreterDataType("0"));
            }
            else{
                globalVariables.put("NF", new InterpreterDataType(String.valueOf(splitLine.length)));
            }
            int preNumRecords = 0;
            if(globalVariables.containsKey("NR")){
                preNumRecords = Integer.parseInt(globalVariables.get("NR").getValue());
            }
            globalVariables.put("NR", new InterpreterDataType(String.valueOf(++preNumRecords)));
            int preFNR = 0;
            if(globalVariables.containsKey("FNR")){
                preFNR = Integer.parseInt(globalVariables.get("FNR").getValue());
            }
            globalVariables.put("FNR", new InterpreterDataType(String.valueOf(++preFNR)));
            return true;
        }

        public LineManager(List<String> input) {
            this.input = input;
        }
    }

    private HashMap<String, InterpreterDataType> globalVariables;
    private HashMap<String, FunctionDefinitionNode> definedFunctions;
    ProgramNode program;
    LineManager manager;

    public HashMap<String, FunctionDefinitionNode> getDefinedFunctions() {
        return definedFunctions;
    }

    public HashMap<String, InterpreterDataType> getGlobalVars() {
        return globalVariables;
    }

    public Interpreter(ProgramNode program, Path file) throws IOException {
        this.program = program;
        globalVariables = new HashMap<>();
        definedFunctions = new HashMap<>();
//        LineManager manager;
        if (file == null) {
            manager = new LineManager(new ArrayList<String>());
            globalVariables.put("FILENAME", new InterpreterDataType(" "));
        } else {
            manager = new LineManager(Files.readAllLines(file));
            globalVariables.put("FILENAME", new InterpreterDataType(file.toString()));
        }

        globalVariables.put("FS", new InterpreterDataType(" "));
        globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
        globalVariables.put("OFS", new InterpreterDataType(" "));
        globalVariables.put("ORS", new InterpreterDataType("\n"));
        globalVariables.put("NR", new InterpreterDataType("0"));
        // AWK actually initializes NR at zero, so if you call NR in the begin block before split and assign,
        // it will print 0

        /**
         * unlike the predefined AWK functions, that we will have a separate class for, the functions that the users
         * define will be added to the functions hashmap here
         */
        for(int i = 0; i < program.getFunctionDefinitions().size(); i++){
            FunctionDefinitionNode cur = program.getFunctionDefinitions().get(i);
            definedFunctions.put(cur.getFunctionName(), cur);
        }
        /**
         * we will have print have a singular parameter - Interpreter Array Data type. this is easy for us
         * to iterate over and print out
         * print in AWK can have no parameter - it defaults to $0
         */
        definedFunctions.put("PRINT", new BuiltInFunctionDefinitionNode((s, LL) -> {
            if(LL.isEmpty()){
                System.out.println(globalVariables.get("$0").getValue());
                return " ";
            }
            InterpreterArrayDataType IADT = (InterpreterArrayDataType) s.get(LL.get(0));
            HashMap<String, InterpreterDataType> map = IADT.getHashmap();
            for (var i = 0; ; i++) {
                if (map.containsKey(Integer.toString(i))){
//                    if(map.get(Integer.toString(i)) == null){
//                        continue;
//                    }
                    System.out.print(map.get(Integer.toString(i)).toString() + " ");
                }
                else{
                    System.out.print("\n");
                    break;
                }
            }
            return "0";
        }, true, 0, 1));

        // I MIGHT HAVE TO CHECK IF THE RETURN VALUE OF THE GET METHOD IS IN THE GLOBAL
        // VARIABLES AND PRINT THAT IF IT EXISTS
        /**
         * we will have print have a singular paramater - Interpreter Array Data type. this is easy for us
         * to iterate over and print out
         */
        definedFunctions.put("PRINTF", new BuiltInFunctionDefinitionNode((s, LL) -> {
            var IDT = s.get(LL.get(0));
            String IDTString = IDT.getValue();
            InterpreterArrayDataType IADT = null;
            if(LL.size() > 1){
                IADT = (InterpreterArrayDataType) s.get(LL.get(1));
            }
            if (IADT != null) {
                // intellij tells me i have to have the new IDT in the parameter or else it will cry about private values....
                InterpreterDataType[] values = IADT.getHashmap().values().toArray(new InterpreterDataType[0]);
                String[] valuesAsStrings = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    valuesAsStrings[i] = values[i].getValue();
                }
                System.out.printf(IDTString, (Object[]) valuesAsStrings);
                return "0";
            } else {
                System.out.println(IDTString);
                return "0";
            }
        }, true, 1, 2));

        definedFunctions.put("GETLINE", new BuiltInFunctionDefinitionNode((s, LL) -> {
            manager.SplitAndAssign();
            return "0";
        }, false, 0, 0));

        definedFunctions.put("NEXT", new BuiltInFunctionDefinitionNode((s, LL) -> {
            manager.SplitAndAssign();
            return "0";
        }, false, 0, 0));

        // MIGHT USE MATCHER CLASS
        /**
         * GSUB in AWK takes 3 paramaters. 2 are mandatory, 1 is optional, so i check if the last one(the location) is
         * there. AWK defaults it to $0 if it isnt there.
         */
        definedFunctions.put("gsub", new BuiltInFunctionDefinitionNode((s, LL) -> {
            int numRepsMade = 0;
            InterpreterDataType original = s.get(LL.get(0));
            InterpreterDataType replacement = s.get(LL.get(1));
            InterpreterDataType location = null;
            if(LL.size() > 2){
                location = s.get(LL.get(2));
            }
            if (location == null) {
                location = new InterpreterDataType("$0");
            }
//            if (globalVariables.containsKey(original.getValue())) {
//                original = globalVariables.get(original.getValue());
//            }
//            if (globalVariables.containsKey(replacement.getValue())) {
//                replacement = globalVariables.get(replacement.getValue());
//            }
            String locationString;
//            if (globalVariables.containsKey(location.getValue())) {
//                locationString = globalVariables.get(location.getValue()).getValue();
//            } else
                locationString = location.getValue();
            String originalString = original.getValue();
            String replacementString = replacement.getValue();
//            locationString = location.getValue();

            Pattern pat = Pattern.compile(originalString);
            Matcher matcher = pat.matcher(locationString);
            while (matcher.find()) {
                numRepsMade++;
                locationString = matcher.replaceFirst(replacementString);
                if(LL.size() > 2){
                    globalVariables.put(LL.get(2), new InterpreterDataType(locationString));
                }
                else{
                    globalVariables.put("$0", new InterpreterDataType(locationString));
                }
                matcher = pat.matcher(locationString);
            }

//            String testCase = globalVariables.get(locationString).getValue();
//            String newCase = testCase.replaceFirst(originalString, replacementString);
//            while(!testCase.equals(newCase)){
//                numRepsMade++;
//                globalVariables.put(locationString, new InterpreterDataType(newCase));
//                testCase = newCase;
//                newCase = testCase.replaceFirst(originalString, replacementString);
//            }

            return String.valueOf(numRepsMade);
        }, false, 2 ,3));

        /**
         * Match in AWK has 3 parmaaters. 2 that are mandatory, one that is optional. the optional one is an array -
         * this is for when we want to add the matched strings into an array at index 0; otherwise, we use the Matcher
         * and Pattern classes to return the locaation of thr first character that matched the Match
         */
        definedFunctions.put("match", new BuiltInFunctionDefinitionNode((s, LL) -> {
            InterpreterDataType location = s.get(LL.get(0));
            InterpreterDataType lookFor = s.get(LL.get(1));
//            InterpreterArrayDataType array = (InterpreterArrayDataType) s.get("array");
            InterpreterDataType array = null;
            if (LL.size() > 2) {
                array = s.get("array");
            }
            String locationString = location.getValue();
            String lookForString = lookFor.getValue();
//            if (globalVariables.containsKey(locationString)) {
//                locationString = globalVariables.get(locationString).getValue();
//            }
            Pattern pat = Pattern.compile(lookForString);
            Matcher matcher = pat.matcher(locationString);
            if (matcher.find()) {
                if (array != null) {
                    HashMap<String, InterpreterDataType> inputHash = new HashMap<>();
                    inputHash.put("0", new InterpreterDataType(matcher.group()));
                    InterpreterArrayDataType tempIADT = new InterpreterArrayDataType(inputHash);
                    globalVariables.put(array.getValue(), tempIADT);
                }
                return String.valueOf(matcher.start() + 1); // we add one because java length starts at 0 and AWK starts at 1
            } else return "0";

//            thisLine.matches()
        }, false, 2, 3));

        /**
         * the AWK sub replaces the given regex with the replacemnet inside of the location given
         */
        definedFunctions.put("sub", new BuiltInFunctionDefinitionNode((s, LL) -> {
            InterpreterDataType regex = s.get(LL.get(0));
            InterpreterDataType replacement = s.get(1);
            InterpreterDataType location = null;
            if(LL.size() > 2){
                location = s.get("location");
            }
            if (location == null) {
                location = new InterpreterDataType("$0");
            }
            String regexString = regex.getValue();
            String replacementString = replacement.getValue();
            String locationString;
//            if (globalVariables.containsKey(location.getValue())) {
//                locationString = globalVariables.get(location.getValue()).getValue();
//            } else
                locationString = location.getValue();
            // THIS IS ONLY VALID IN AWK, AND THEREFORE VALID IN GAWK. ITS ACTUALLY BAD, AND BASICALLY DOES NOTHING

            Pattern pat = Pattern.compile(regexString);
            Matcher matcher = pat.matcher(locationString);
            if (matcher.find()) {
                if (replacementString.contains("&")) {
                    replacementString = replacementString.replace("&", matcher.group());
                }
                locationString = matcher.replaceFirst(replacementString);
                globalVariables.put(location.getValue(), new InterpreterDataType(locationString));
                return "1";
            } else return "0";
        }, false, 2, 3));

        /**
         * uses the Java indexOf to get the index of the substring we are looking for.
         */
        definedFunctions.put("index", new BuiltInFunctionDefinitionNode((s, LL) -> {
            InterpreterDataType lookIn = s.get(LL.get(0));
            InterpreterDataType lookFor = s.get(LL.get(1));
            String lookInString = lookIn.getValue();
            String lookForString = lookFor.getValue();

//            if (globalVariables.containsKey(lookInString)) {
//                lookInString = globalVariables.get(lookInString).getValue();
//            }

            if (lookInString.contains(lookForString)) {
                return String.valueOf(lookInString.indexOf(lookForString) + 1); // javas indexof starts from 0, AWK's one starts from 1 ;)
            } else return "0";
        }, false, 2, 2));

        /**
         * returns the length of thre given input.
         */
        definedFunctions.put("length", new BuiltInFunctionDefinitionNode((s, LL) -> {
            InterpreterDataType input = null;
            if(!(LL.isEmpty())){
                input = s.get(LL.get(0));
            }
            if (input == null) {
                input = globalVariables.get("$0");
            }
            String inputString = input.getValue();
//            if (globalVariables.containsKey(inputString)) {
//                inputString = globalVariables.get(inputString).getValue();
//            }
            return String.valueOf(inputString.length());
        }, false, 0, 1));

        /**
         * takes 4 inputs. the fourth one is optional. if it is given(an array), then we populate that
         * array with how many of the separators there are. if not, we just populat the first array with the
         * string we are looking at - split by the separator.
         */
        definedFunctions.put("split", new BuiltInFunctionDefinitionNode((s, LL) -> {
            InterpreterDataType input = s.get(LL.get(0));
            InterpreterDataType splitArrayName = s.get(LL.get(1));
            InterpreterDataType fieldSeparator = null;
            InterpreterDataType separatorArrayName = null;
            if(LL.size() > 2){
               fieldSeparator = s.get(LL.get(2));
               separatorArrayName = s.get(LL.get(2));
            }
            if (fieldSeparator == null) {
                fieldSeparator = globalVariables.get("FS");
            }
            String lookIn = input.getValue();
            String lookInCopy = lookIn;
//            if (globalVariables.containsKey(lookIn)) {
//                lookIn = globalVariables.get(lookIn).getValue();
//            }

            String[] splits = lookIn.split(fieldSeparator.getValue());
            globalVariables.put(splitArrayName.getValue(), new InterpreterArrayDataType(new HashMap<>()));
            InterpreterArrayDataType temp = (InterpreterArrayDataType) globalVariables.get(splitArrayName.getValue());

            for (int i = 0; i < splits.length; i++) {
                temp.getHashmap().put(String.valueOf(i + 1), new InterpreterDataType(splits[i]));
            }
            if (separatorArrayName != null) {
                Pattern pat = Pattern.compile(fieldSeparator.getValue());
                Matcher matcher = pat.matcher(lookInCopy);
                int j = 1;
                globalVariables.put(separatorArrayName.getValue(), new InterpreterArrayDataType(new HashMap<>()));
                InterpreterArrayDataType arrayOfSeps = (InterpreterArrayDataType) globalVariables.get(separatorArrayName.getValue());
                while (matcher.find()) {
                    arrayOfSeps.getHashmap().put(String.valueOf(j), new InterpreterDataType(matcher.group()));
                    j++;
                }
            }
            return String.valueOf(splits.length);
        }, false, 2, 4));

        /**
         * returns a substring of the given amounts. the endPoint is optional. if it isnt given, we just give the rest of the
         * string. we also make sure both the start anbd and index dont go over bounds.
         */
        definedFunctions.put("substr", new BuiltInFunctionDefinitionNode((s, LL) -> {
            InterpreterDataType lookIn = s.get(LL.get(0));
            InterpreterDataType startPoint = s.get(LL.get(1));
            InterpreterDataType endPoint = null;
            if(LL.size() > 2){
                endPoint = s.get("endpoint");
            }
            int end = 0;
            int startPointInt = 0;

            String startPointString = startPoint.getValue();
            startPointInt = Integer.parseInt(startPointString);

            String lookInString = lookIn.getValue();
//            if (globalVariables.containsKey(lookInString)) {
//                lookInString = globalVariables.get(lookInString).getValue();
//            }

            if (endPoint == null) {
                return lookInString.substring(startPointInt - 1); // AWK substr and Java substr are off by 1
//                end = lookInString.length() - startPointInt;
            } else end = Integer.parseInt(endPoint.getValue());
            if ((startPointInt > lookInString.length()) || startPointInt - 1 + end > lookInString.length()) {
                return null;
            }
            return lookInString.substring(startPointInt - 1, startPointInt + end - 1);
        }, false, 2, 3));

        /**
         * uses the Java toLower to convert everything to lowercase
         */
        definedFunctions.put("tolower", new BuiltInFunctionDefinitionNode((s, LL) -> {

            InterpreterDataType input = s.get(LL.get(0));
//            if (globalVariables.containsKey(input.getValue())) {
//                input = globalVariables.get(input.getValue());
//            }
            String inputString = input.getValue();
            return inputString.toLowerCase();
        }, false, 1, 1));

        /**
         * uses the Java toLower to convert everything to uppercase
         */
        definedFunctions.put("toupper", new BuiltInFunctionDefinitionNode((s, LL) -> {
            InterpreterDataType input = s.get(LL.get(0));
//            if (globalVariables.containsKey(input.getValue())) {
//                input = globalVariables.get(input.getValue());
//            }
            String inputString = input.getValue();
            return inputString.toUpperCase();
        }, false, 1, 1));

    }

    public InterpreterDataType GetIDT(Node node, HashMap<String, InterpreterDataType> localVariables) throws Exception {
        if(node == null){
            return new InterpreterDataType("");
        }
        if (node instanceof AssignmentNode) {
            Node tempTarget = ((AssignmentNode) node).getTarget();
            if (tempTarget instanceof VariableReferenceNode) {
                // we treat them separatley when they are an array vs when theyre not.
                // in AWK, when an array index is refereced and it doesnt exist, it actually makes it. it only throws an exception
                // when the value already exists as a non array
                if (((VariableReferenceNode) tempTarget).isArrayReference()) {
                    InterpreterDataType index = GetIDT(((VariableReferenceNode) tempTarget).getIndex(), localVariables);
                    if (localVariables.containsKey(((VariableReferenceNode) tempTarget).getName())) {
                        if (localVariables.get(((VariableReferenceNode) tempTarget).getName()) instanceof InterpreterArrayDataType) {
                            InterpreterArrayDataType tempArray = (InterpreterArrayDataType) localVariables.get(((VariableReferenceNode) tempTarget).getName());
                            tempArray.getHashmap().put(index.getValue(), GetIDT(((AssignmentNode) node).getExpression(), localVariables));
                            return tempArray.getHashmap().get(index.getValue());
                        } else throw new Exception("Tried to use a constant as an array for assignment");
                    }
                    if (globalVariables.containsKey(((VariableReferenceNode) tempTarget).getName())) {
                        if (globalVariables.get(((VariableReferenceNode) tempTarget).getName()) instanceof InterpreterArrayDataType) {
                            InterpreterArrayDataType tempArray = (InterpreterArrayDataType) globalVariables.get(((VariableReferenceNode) tempTarget).getName());
                            tempArray.getHashmap().put(index.getValue(), GetIDT(((AssignmentNode) node).getExpression(), localVariables));
                            return tempArray.getHashmap().get(index.getValue());
                        } else throw new Exception("Tried to use a constant as an array for assignment");
                    } else {
                        globalVariables.put(((VariableReferenceNode) tempTarget).getName(), new InterpreterArrayDataType(new HashMap<>()));
                        InterpreterArrayDataType temp = (InterpreterArrayDataType) globalVariables.get(((VariableReferenceNode) tempTarget).getName());
//                        temp.getHashmap().put(index.getValue(), new InterpreterDataType(((AssignmentNode) node).getExpression().toString()));
                        temp.getHashmap().put(index.getValue(), GetIDT(((AssignmentNode) node).getExpression(), localVariables));

                        return temp.getHashmap().get(index.getValue());
                    }
                }
                InterpreterDataType temp = GetIDT(((AssignmentNode) node).getExpression(), localVariables);
                if (localVariables.containsKey(((VariableReferenceNode) tempTarget).getName())) {
                    InterpreterDataType local = localVariables.get(((VariableReferenceNode) tempTarget).getName());
                    if (local instanceof InterpreterArrayDataType) {
                        throw new Exception("attempting to use an array as a scalar");
                    }
                    localVariables.put(((VariableReferenceNode) tempTarget).getName(), temp);
                    return temp;
                }
                if (globalVariables.containsKey(((VariableReferenceNode) tempTarget).getName())) {
                    InterpreterDataType global = globalVariables.get(((VariableReferenceNode) tempTarget).getName());
                    if (global instanceof InterpreterArrayDataType) {
                        throw new Exception("attempting to use an array as a scalar");
                    }
                    globalVariables.put(((VariableReferenceNode) tempTarget).getName(), temp);
                    return temp;
                }
                globalVariables.put(((VariableReferenceNode) tempTarget).getName(), temp);
//                globalVariables.put(tempVarRN.getName(), temp);
                return temp;
            } else if (tempTarget instanceof OperationNode) {
                if (!(((OperationNode) tempTarget).getOperator() == OperationNode.possibleOps.DOLLAR)) {
                    throw new Exception("this operation cannot be assigned - it is not a valid left hand side variable");
                }
                InterpreterDataType temp = GetIDT(((AssignmentNode) node).getExpression(), localVariables);
                OperationNode tempOp = (OperationNode) ((AssignmentNode) node).getTarget();
                globalVariables.put("$" + tempOp.getRight().toString(), temp);
                return temp;

            } else {
                throw new Exception("this value cannot be assigned - it is not a valid left hand side variable");
            }
        }

        if (node instanceof ConstantNode) {
            return new InterpreterDataType(node.toString());
        }

        if (node instanceof functionCallNode) {
            return new InterpreterDataType(RunFunctionCall((functionCallNode) node, localVariables));
        }

        if (node instanceof PatternNode) {
            throw new Exception("invalid placement of pattern :(");
        }

        if (node instanceof TernaryNode) {
            Node expression = ((TernaryNode) node).getExpression();
            Node firstValue = ((TernaryNode) node).getFirstValue();
            Node secondValue = ((TernaryNode) node).getSecondValue();
            if (GetIDT(expression, localVariables).getValue().equals("1")) {
                return GetIDT(firstValue, localVariables);
            } else {
                return GetIDT(secondValue, localVariables);
            }
        }

        if (node instanceof VariableReferenceNode) {
            if (((VariableReferenceNode) node).isArrayReference()) {
                InterpreterDataType index = GetIDT(((VariableReferenceNode) node).getIndex(), localVariables);
                if (localVariables.containsKey(((VariableReferenceNode) node).getName())) {
                    if (localVariables.get(((VariableReferenceNode) node).getName()) instanceof InterpreterArrayDataType) {
                        InterpreterArrayDataType tempIADT = (InterpreterArrayDataType) localVariables.get((((VariableReferenceNode) node).getName()));
                        return (tempIADT.getHashmap().get(index.getValue()));
                    } else throw new Exception("not an interpreter array data type");
                }
                else if (globalVariables.containsKey(((VariableReferenceNode) node).getName())) {
                    if (globalVariables.get(((VariableReferenceNode) node).getName()) instanceof InterpreterArrayDataType) {
                        InterpreterArrayDataType tempIADT = (InterpreterArrayDataType) globalVariables.get((((VariableReferenceNode) node).getName()));
                        return (tempIADT.getHashmap().get(index.getValue()));
                    } else throw new Exception("not an interpreter array data type");
                } else {
                    globalVariables.put(((VariableReferenceNode) node).getName(), new InterpreterArrayDataType(new HashMap<>()));
                    return new InterpreterDataType("");
                }
                // might need to delete this to throw error
            } else {
                if (localVariables.containsKey(node.toString())) {
                    return localVariables.get(node.toString());
                }
                if (globalVariables.containsKey(node.toString())) {
                    return globalVariables.get(node.toString());
                } else {
                    globalVariables.put(node.toString(), new InterpreterDataType(""));
                    return new InterpreterDataType("");
                }
            }
        }

        /**
         * in awk, certain math operations are valid with strings and integers. If there is a string, it is just used as 0.
         * thats why i convert strings to 0 and try to convert the rest to floats
         * AWK also returns 1 for true and 0 for false
         */
        if (node instanceof OperationNode) {
            OperationNode.possibleOps operator = ((OperationNode) node).getOperator();
            if (operator == OperationNode.possibleOps.EXPONENT ||
                    operator == OperationNode.possibleOps.ADD ||
                    operator == OperationNode.possibleOps.SUBTRACT ||
                    operator == OperationNode.possibleOps.MULTIPLY ||
                    operator == OperationNode.possibleOps.DIVIDE ||
                    operator == OperationNode.possibleOps.MODULO) {
                InterpreterDataType left = GetIDT(((OperationNode) node).getLeft(), localVariables);
                InterpreterDataType right = GetIDT(((OperationNode) node).getRight(), localVariables);
                float leftNumber;
                float rightNumber;
                if (convToFloat(left.getValue())) {
                    leftNumber = Float.parseFloat(left.getValue());
                } else leftNumber = 0;
                if (convToFloat(right.getValue())) {
                    rightNumber = Float.parseFloat(right.getValue());
                } else rightNumber = 0;
                if (operator == OperationNode.possibleOps.EXPONENT) {
                    return new InterpreterDataType(String.valueOf(Math.pow(leftNumber, rightNumber)));
                } else if (operator == OperationNode.possibleOps.ADD) {
                    return new InterpreterDataType(String.valueOf(leftNumber + rightNumber));
                } else if (operator == OperationNode.possibleOps.SUBTRACT) {
                    return new InterpreterDataType(String.valueOf(leftNumber - rightNumber));
                } else if (operator == OperationNode.possibleOps.MULTIPLY) {
                    return new InterpreterDataType(String.valueOf(leftNumber * rightNumber));
                } else if (operator == OperationNode.possibleOps.DIVIDE) {
                    return new InterpreterDataType(String.valueOf(leftNumber / rightNumber));
                } else {
                    return new InterpreterDataType(String.valueOf(leftNumber % rightNumber));
                }

            } else if (operator == OperationNode.possibleOps.EQUALS ||
                    operator == OperationNode.possibleOps.NOTEQUALS ||
                    operator == OperationNode.possibleOps.LESSTHAN ||
                    operator == OperationNode.possibleOps.LESSEQUAL ||
                    operator == OperationNode.possibleOps.GREATERTHAN ||
                    operator == OperationNode.possibleOps.GREATEREQUAL) {
                InterpreterDataType left = GetIDT(((OperationNode) node).getLeft(), localVariables);
                InterpreterDataType right = GetIDT(((OperationNode) node).getRight(), localVariables);
                if (convToFloat(left.getValue()) && convToFloat(right.getValue())) {
                    float leftval = Float.parseFloat(left.getValue());
                    float rightval = Float.parseFloat(right.getValue());
                    if (operator == OperationNode.possibleOps.EQUALS) {
                        if (leftval == rightval) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.NOTEQUALS) {
                        if (leftval != rightval) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.LESSTHAN) {
                        if (leftval < rightval) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.LESSEQUAL) {
                        if (leftval <= rightval) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.GREATERTHAN) {
                        if (leftval > rightval) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else {
                        if (leftval >= rightval) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    }
                } else {
                    String leftval = left.getValue();
                    String rightval = right.getValue();
                    int compareVal = leftval.compareTo(rightval);
                    if (operator == OperationNode.possibleOps.EQUALS) {
                        if (compareVal == 0) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.NOTEQUALS) {
                        if (compareVal != 0) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.LESSTHAN) {
                        if (compareVal < 0) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.LESSEQUAL) {
                        if (compareVal <= 0) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else if (operator == OperationNode.possibleOps.GREATERTHAN) {
                        if (compareVal > 0) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else {
                        if (compareVal >= 0) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    }
                }
            } else if (operator == OperationNode.possibleOps.AND || //    &&
                    operator == OperationNode.possibleOps.OR || //             ||
                    operator == OperationNode.possibleOps.NOT) //              !
            {
                // i did not first since it doesnt have a left value and didnt want a NPE
                if (operator == OperationNode.possibleOps.NOT) {
                    InterpreterDataType right = GetIDT(((OperationNode) node).getRight(), localVariables);
                    if (convToFloat(right.getValue())) {
                        float rightval = Float.parseFloat(right.getValue());
                        if (rightval == 0) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else return new InterpreterDataType("0");
                }
                InterpreterDataType leftval = GetIDT(((OperationNode) node).getLeft(), localVariables);
                InterpreterDataType rightval = GetIDT(((OperationNode) node).getRight(), localVariables);
                if (operator == OperationNode.possibleOps.AND) {
                    if (convToFloat(leftval.getValue()) && convToFloat(rightval.getValue())) {
                        float leftFloat = Float.parseFloat(leftval.getValue());
                        float rightfloat = Float.parseFloat(rightval.getValue());
                        if (leftFloat == 0 || rightfloat == 0) {
                            return new InterpreterDataType("0");
                        } else return new InterpreterDataType("1");
                    } else {
                        String left = leftval.getValue();
                        String right = rightval.getValue();
                        if (left.isEmpty() || right.isEmpty()) {
                            return new InterpreterDataType("0");
                        } else return new InterpreterDataType("1");
                    }
                } else {
                    if (convToFloat(leftval.getValue()) && convToFloat(rightval.getValue())) {
                        float leftFloat = Float.parseFloat(leftval.getValue());
                        float rightfloat = Float.parseFloat(rightval.getValue());
                        if (leftFloat == 0 && rightfloat == 0) {
                            return new InterpreterDataType("0");
                        } else return new InterpreterDataType("1");
                    } else {
                        String left = leftval.getValue();
                        String right = rightval.getValue();
                        if (left.isEmpty() && right.isEmpty()) {
                            return new InterpreterDataType("0");
                        } else return new InterpreterDataType("1");
                    }
                }

            } else if (operator == OperationNode.possibleOps.MATCH ||
                    operator == OperationNode.possibleOps.NOTMATCH) {
                InterpreterDataType lookIn = GetIDT(((OperationNode) node).getLeft(), localVariables);
                String lookFor;
                if (((OperationNode) node).getRight() instanceof PatternNode) {
                    PatternNode tempPattern = (PatternNode) ((OperationNode) node).getRight();
                    lookFor = tempPattern.toString();
                }
                // i am doing this SPECIFICALLY because AWK allows this! ive tried many examples. all of these are valid:
                // BEGIN{x = "hi i like hi"; y = "hi"; if("5" ~ "5") print "amadeus" -- pattern compares two strings -- valid in AWK
                // BEGIN{x = "hi i like hi"; y = "hi"; if(x ~ y) print "amadeus"}' -- also works in AWK
                // BEGIN{x = "hi i like hi"; y = "hi"; if("5" ~ (2+3)) print "amadeus"} -- also prints out in AWK
                // i dont know why AWK allows these but here we are ----- typed languages >>>>>> so much better
                else {
                    InterpreterDataType pattern = GetIDT(((OperationNode) node).getRight(), localVariables);
                    lookFor = pattern.getValue();
                }
                Pattern pat = Pattern.compile(lookFor);
                Matcher matcher = pat.matcher(lookIn.getValue());
                if (operator == OperationNode.possibleOps.MATCH) {
                    if (matcher.find()) {
                        return new InterpreterDataType("1");
                    } else return new InterpreterDataType("0");
                }
                if (matcher.find()) {
                    return new InterpreterDataType("0");
                } else return new InterpreterDataType("1");
            } else if (operator == OperationNode.possibleOps.DOLLAR) {
                InterpreterDataType right = GetIDT(((OperationNode) node).getRight(), localVariables);
                /**
                 * AWK throws an exception if someone tries to access $-1, or other negative
                 * also, if you try to do $x, with x being a string with the value "hey", it will default to $0
                 * however, if the string was "1", it would be valid
                 * yes AWK is weird - IDK how i found this
                 */
                int num;
                if(convToFloat(right.getValue())){
                    if(Float.parseFloat(right.getValue()) < 0){
                        throw new Exception("trying to access $" + right.getValue() + ", which is negative and not valid");
                    } else {
                        num = Float.valueOf(right.getValue()).intValue();
                    }
                }
                else{
                    right = new InterpreterDataType("0");
                    num = 0;
                }
                if(globalVariables.containsKey("$" + num)){
                    return globalVariables.get("$" + num);
                }
                return new InterpreterDataType("");
//                return new InterpreterDataType("$" + right.getValue());
            } else if (operator == OperationNode.possibleOps.PREINC ||
                    operator == OperationNode.possibleOps.POSTINC ||
                    operator == OperationNode.possibleOps.PREDEC ||
                    operator == OperationNode.possibleOps.POSTDEC ||
                    operator == OperationNode.possibleOps.UNARYNEG ||
                    operator == OperationNode.possibleOps.UNARYPOS)
            // since we made post/pre inc/dec in parser to ASSIGNMENT nodes, not operation nodes, we dont need to update any
            // value in the hashmap here, we only need to return the value !!! I actually love this
            // this might work differently on post inc/dec tho..... i have to think about it
            {
                InterpreterDataType left;
                InterpreterDataType right;
                float leftVal = 0;
                float rightVal = 0;
                if (((OperationNode) node).isLeftPresent()) {
                    left = GetIDT(((OperationNode) node).getLeft(), localVariables);
                    if (convToFloat(left.getValue())) {
                        leftVal = Float.parseFloat(left.getValue());
                    }
                }
                if (((OperationNode) node).isRightPresent()) {
                    right = GetIDT(((OperationNode) node).getRight(), localVariables);
                    if (convToFloat(right.getValue())) {
                        rightVal = Float.parseFloat(right.getValue());
                    }
                }
                if (operator == OperationNode.possibleOps.PREINC) {
                    if (!(((OperationNode) node).getRight() instanceof VariableReferenceNode)) {
                        throw new Exception("cannot do pre increment on this");
                    }
                    return new InterpreterDataType(String.valueOf(rightVal + 1));
                }
                if (operator == OperationNode.possibleOps.POSTINC) { // im not a 100 percent sure on this one... might update
                    if (!(((OperationNode) node).getLeft() instanceof VariableReferenceNode)) {
                        throw new Exception("cannot do post increment on this");
                    }
//                    if(localVariables.containsKey(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName())){
//                        localVariables.put(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName(), new InterpreterDataType(String.valueOf(leftVal + 1)));
//                    }
//                    else if(globalVariables.containsKey(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName())){
//                        globalVariables.put(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName(), new InterpreterDataType(String.valueOf(leftVal + 1)));
//                    }
                    return new InterpreterDataType(String.valueOf(leftVal + 1));
                }
                if (operator == OperationNode.possibleOps.PREDEC) {
                    if (!(((OperationNode) node).getRight() instanceof VariableReferenceNode)) {
                        throw new Exception("cannot do pre decrement on this");
                    }
                    return new InterpreterDataType(String.valueOf(rightVal - 1));
                }
                if (operator == OperationNode.possibleOps.POSTDEC) { // im not a 100 percent sure on this one... might update
                    if (!(((OperationNode) node).getLeft() instanceof VariableReferenceNode)) {
                        throw new Exception("cannot do post decrement on this");
                    }
//                    if(localVariables.containsKey(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName())){
//                        localVariables.put(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName(), new InterpreterDataType(String.valueOf(leftVal - 1)));
//                    }
//                    else if(globalVariables.containsKey(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName())){
//                        globalVariables.put(((VariableReferenceNode) ((OperationNode) node).getLeft()).getName(), new InterpreterDataType(String.valueOf(leftVal - 1)));
//                    }
                    return new InterpreterDataType(String.valueOf(rightVal - 1));
                }
                if (operator == OperationNode.possibleOps.UNARYNEG) {
                    return new InterpreterDataType(String.valueOf(rightVal * -1));
                } else return new InterpreterDataType(String.valueOf(rightVal));

            } else if (operator == OperationNode.possibleOps.CONCATENATION) {
                InterpreterDataType left = GetIDT(((OperationNode) node).getLeft(), localVariables);
                InterpreterDataType right = GetIDT(((OperationNode) node).getRight(), localVariables);
                return new InterpreterDataType(left.getValue() + right.getValue());
            } else if (operator == OperationNode.possibleOps.IN) {
                InterpreterDataType left = GetIDT(((OperationNode) node).getLeft(), localVariables);
                InterpreterDataType right = GetIDT(((OperationNode) node).getRight(), localVariables);
//                if(((OperationNode) node).getRight() instanceof VariableReferenceNode){
//                    VariableReferenceNode temp = (VariableReferenceNode) ((OperationNode) node).getRight();
//                    if(temp.isArrayReference()){
//                        throw new Exception("trying to use a constant as an array");
//                        // this is what awk throws - you cant say: "5 in a[1]" because a[1] will turn into a constant
//                    }
                if (localVariables.containsKey(right.getValue())) {
                    if (localVariables.get(right.getValue()) instanceof InterpreterArrayDataType) {
                        InterpreterArrayDataType tempArray = (InterpreterArrayDataType) localVariables.get(right.getValue());
                        if (tempArray.getHashmap().containsKey(left.getValue())) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else throw new Exception("trying to use scalar as an array");
                } else if (globalVariables.containsKey(right.getValue())) {
                    if (globalVariables.get(right.getValue()) instanceof InterpreterArrayDataType) {
                        InterpreterArrayDataType tempArray = (InterpreterArrayDataType) globalVariables.get(right.getValue());
                        if (tempArray.getHashmap().containsKey(left.getValue())) {
                            return new InterpreterDataType("1");
                        } else return new InterpreterDataType("0");
                    } else throw new Exception("trying to use scalar as an array");
                } else {
                    globalVariables.put(right.getValue(), new InterpreterArrayDataType(new HashMap<>()));
                    return new InterpreterDataType("");
                }
            } else throw new Exception("cannot use this with the keyword \"in\"");
        }
//        }
        throw new Exception("reached end of GETIDT with nothing");
    }

    public ReturnType ProcessStatement(HashMap<String, InterpreterDataType> locals, StatementNode statement) throws Exception {
//        if (statement instanceof AssignmentNode) {
//            InterpreterDataType left = GetIDT(((AssignmentNode) statement).getTarget(), locals);
//            InterpreterDataType right = GetIDT(((AssignmentNode) statement).getExpression(), locals);
//            if (locals.containsKey(left.getValue())) {
//                locals.put(left.getValue(), new InterpreterDataType(right.getValue()));
//            } else globalVariables.put(left.getValue(), new InterpreterDataType(right.getValue()));
//            return new ReturnType(ReturnType.type.Normal, right.getValue());
//        }
        if (statement instanceof breakNode) {
            return new ReturnType(ReturnType.type.Break);
        }
        if (statement instanceof continueNode) {
            return new ReturnType(ReturnType.type.Continue);
        }
        /**
         * in AWK, delete can take an entire array reference, or a specific index. I make sure I throw exceptions if the
         * user tries to delete just a normal variable. in AWK, if the array doesnt exist, it actually creates it as an empty
         * array. if the index doesnt exist it does nothing. Also, when you delete in AWK, it doesnt actually delete the variable
         * from memory. rather, it keeps it as an array, so you cant reassign the variable to a scalar
         */
        if (statement instanceof deleteNode) {
            VariableReferenceNode variable = (VariableReferenceNode) ((deleteNode) statement).getValue();
            InterpreterArrayDataType tempArray;

            if (locals.containsKey(variable.getName())) {
                if (locals.get(variable.getName()) instanceof InterpreterArrayDataType) {
                    tempArray = (InterpreterArrayDataType) locals.get(variable.getName());
                } else throw new Exception("trying to delete a scalar, not an array!");
            }
            else if (globalVariables.containsKey(variable.getName())) {
                if (globalVariables.get(variable.getName()) instanceof InterpreterArrayDataType) {
                    tempArray = (InterpreterArrayDataType) globalVariables.get(variable.getName());
                } else throw new Exception("trying to delete a scalar, not an array!");
            } else {
                // awk actually creates it, if it doesnt exist
                globalVariables.put(variable.getName(), new InterpreterArrayDataType(new HashMap<>()));
                return new ReturnType(ReturnType.type.Normal);
            }
            if (variable.isArrayReference()) {
                InterpreterDataType index = GetIDT(variable.getIndex(), locals);
                tempArray.getHashmap().remove(index.getValue());
//                tempArray.getHashmap().put(index.getValue(), new InterpreterDataType(""));
                return new ReturnType(ReturnType.type.Normal);
            }
            tempArray.getHashmap().clear();
            return new ReturnType(ReturnType.type.Normal);

            /**
             * i was really sleepy when I wrote the following commented out code, and even more of my code. it is basically
             * the exact same thing, I just basically had duplicated code.
             * I was looking back on it and I realized it was really useless, so i moved some lines to fix it up and commented
             * the rest for memory
             */

//            if (locals.containsKey(variable.getName())) {
//                if (locals.get(variable.getName()) instanceof InterpreterArrayDataType) {
//                    InterpreterArrayDataType tempArray = (InterpreterArrayDataType) locals.get(variable.getName());
//                    tempArray.getHashmap().clear();
//                    return new ReturnType(ReturnType.type.Normal);
//                } else throw new Exception("trying to delete a scalar, not an array!");
//            }
//            if (globalVariables.containsKey(variable.getName())) {
//                if (globalVariables.get(variable.getName()) instanceof InterpreterArrayDataType) {
//                    InterpreterArrayDataType tempArray = (InterpreterArrayDataType) globalVariables.get(variable.getName());
//                    tempArray.getHashmap().clear();
//                    return new ReturnType(ReturnType.type.Normal);
//                } else throw new Exception("trying to delete a scalar, not an array!");
//            } else {
                // awk actually creates it, if it doesnt exist
//                globalVariables.put(variable.getName(), new InterpreterArrayDataType(new HashMap<>()));
//                return new ReturnType(ReturnType.type.Normal);
//            }
        }


        if (statement instanceof doWhileNode) {
            boolean checkBool = true;
            do {
                ReturnType current = InterpretListOfStatements(((doWhileNode) statement).getBlock().getStatements(), locals);
                if (current.getReturnType() == ReturnType.type.Break) {
                    break;
                } else if (current.getReturnType() == ReturnType.type.Continue) {
//                    continue;
                } else if (current.getReturnType() == ReturnType.type.Return) {
//                    throw new Exception("attempt to use Return outside of function!");
                    return new ReturnType(ReturnType.type.Return, current.getRetVal());
                }
                InterpreterDataType check = GetIDT(((doWhileNode) statement).getConditon(), locals);
                if(convToFloat(check.getValue())){
                    if(Float.parseFloat(check.getValue()) == 0){
                        checkBool = false;
                    }
                }
                else if(check.getValue().equals("")){
                    checkBool = false;
                }
            }
            while (checkBool);
            return new ReturnType(ReturnType.type.Normal);
        }

        if (statement instanceof forNode) {
            boolean checkBool = true;
            Optional<Node> firstExpr = ((forNode) statement).getFirstExpr();
            Optional<Node> secondExpr = ((forNode) statement).getSecondExpr();
            Optional<Node> thirdExpr = ((forNode) statement).getThirdExpr();

            if (firstExpr.isPresent()) {
                ProcessStatement(locals, (StatementNode) firstExpr.get());
            }

            if(secondExpr.isPresent()){
                InterpreterDataType check = GetIDT(secondExpr.get(), locals);
                if(convToFloat(check.getValue())){
                    if(Float.parseFloat(check.getValue()) == 0){
                        checkBool = false;
                    }
                }
                else if(check.getValue().equals("")){
                    checkBool = false;
                }
            }
            while (secondExpr.isEmpty() || checkBool) {
                ReturnType current = InterpretListOfStatements(((forNode) statement).getBlock().getStatements(), locals);
                if (current.getReturnType() == ReturnType.type.Break) {
                    break;
                } else if (current.getReturnType() == ReturnType.type.Continue) {
//                    continue;
                } else if (current.getReturnType() == ReturnType.type.Return) {
                    return new ReturnType(ReturnType.type.Return, current.getRetVal());
                }
                if (thirdExpr.isPresent()) {
                    ProcessStatement(locals, (StatementNode) thirdExpr.get());
                }
                // for the next check
                if(secondExpr.isPresent()){
                    InterpreterDataType check = GetIDT(secondExpr.get(), locals);
                    if(convToFloat(check.getValue())){
                        if(Float.parseFloat(check.getValue()) == 0){
                            checkBool = false;
                        }
                    }
                    else if(check.getValue().equals("")){
                        checkBool = false;
                    }
                }
            }
            return new ReturnType(ReturnType.type.Normal);
        }
        /**
         * Find the array, loop over every key in the array’s hashMap.
         * Set the variable to the key, then call InterpretListOfStatements on the forEach’s statements.
         * Follows the same return rules as doWhile
         * Also, Makes sure that the user does not input invalid syntax. You cannot check inside somethign thats not an array,
         * and you cannot use an array to look in an array
         */
        if (statement instanceof forEachNode) {
            OperationNode forInStatement = (OperationNode) ((forEachNode) statement).getForEachStatement();
            if (!(forInStatement.getLeft() instanceof VariableReferenceNode) || ((VariableReferenceNode) forInStatement.getLeft()).isArrayReference()) {
                throw new Exception("Syntax error in ForEach - trying to look for something thats not an index");
            }
            if (locals.containsKey(forInStatement.getLeft().toString())) {
                if (locals.get(((VariableReferenceNode) forInStatement.getLeft()).getName()) instanceof InterpreterArrayDataType) {
                    throw new Exception("trying to use array as a scalar");
                }
//                locals.put(((VariableReferenceNode) forInStatement.getLeft()).getName(), )
            }
            else if (globalVariables.containsKey(forInStatement.getLeft().toString())) {
                if (globalVariables.get(((VariableReferenceNode) forInStatement.getLeft()).getName()) instanceof InterpreterArrayDataType) {
                    throw new Exception("trying to use array as a scalar");
                }
            }
            if (!(forInStatement.getRight() instanceof VariableReferenceNode) || ((VariableReferenceNode) forInStatement.getRight()).isArrayReference()) {
                throw new Exception("Syntax error in ForEach - trying to look in something thats not an array");
            }
            if (locals.containsKey(((VariableReferenceNode) forInStatement.getRight()).getName())) {
                if (!(locals.get(((VariableReferenceNode) forInStatement.getRight()).getName()) instanceof InterpreterArrayDataType)) {
                    throw new Exception("trying to use scalar as a array!");
                }
//                locals.put(((VariableReferenceNode) forInStatement.getLeft()).getName(), )
            }
            else if (globalVariables.containsKey(((VariableReferenceNode) forInStatement.getRight()).getName())) {
                if (!(globalVariables.get(((VariableReferenceNode) forInStatement.getRight()).getName()) instanceof InterpreterArrayDataType)) {
                    throw new Exception("trying to use scalar as an array!");
                }
            }

            HashMap<String, InterpreterDataType> map;
            if(locals.containsKey(((VariableReferenceNode) forInStatement.getRight()).getName())){
                InterpreterArrayDataType IADT = (InterpreterArrayDataType) locals.get(((VariableReferenceNode) forInStatement.getRight()).getName());
                map =  IADT.getHashmap();
            }
            else if(globalVariables.containsKey(((VariableReferenceNode) forInStatement.getRight()).getName())){
                InterpreterArrayDataType IADT = (InterpreterArrayDataType) globalVariables.get(((VariableReferenceNode) forInStatement.getRight()).getName());
                map =  IADT.getHashmap();
            }
            else{
                globalVariables.put(((VariableReferenceNode) forInStatement.getRight()).getName(), new InterpreterArrayDataType(new HashMap<>()));
                return new ReturnType(ReturnType.type.Normal);
            }
            String[] keyArray = map.keySet().toArray(new String[0]);

            for(int i = 0; i < keyArray.length; i++) {
                if(locals.containsKey(forInStatement.getLeft().toString())){
                    locals.put(forInStatement.getLeft().toString(), new InterpreterDataType(keyArray[i]));
                }
                else{
                    globalVariables.put(forInStatement.getLeft().toString(), new InterpreterDataType(keyArray[i]));
                }
                ReturnType current = InterpretListOfStatements(((forEachNode) statement).getBlock().getStatements(), locals);
                if (current.getReturnType() == ReturnType.type.Break) {
                    break;
                } else if (current.getReturnType() == ReturnType.type.Continue) {
                    continue;
                } else if (current.getReturnType() == ReturnType.type.Return) {
                    return new ReturnType(ReturnType.type.Return, current.getRetVal());
                }
            }
            return new ReturnType(ReturnType.type.Normal);
        }

//        if(statement instanceof functionCallNode){
//            return new ReturnType(ReturnType.type.Normal, RunFunctionCall((functionCallNode) statement, locals));
//        }
        /**
         * i have a infinite loop that loops until there are no more in the ifnode linked list. this is because theoretically
         * the user can have infinite if else's.
         */
        if(statement instanceof ifNode){
            ifNode cur = (ifNode) statement;
            while(true){
                boolean isEmpty = cur.getCondition().isEmpty();
                InterpreterDataType condition = new InterpreterDataType("");
                if(!isEmpty){
                    condition = GetIDT(cur.getCondition().get(), locals);
                }
                // condition.getValue().equals("1") || condition.getValue().equals("1.0")
                if(isEmpty || !(condition.getValue().equals("0") ||
                        condition.getValue().equals("0.0") || condition.getValue().equals(""))){
                    ReturnType current = InterpretListOfStatements(cur.getBlock().getStatements(), locals);
                    if (current.getReturnType() == ReturnType.type.Break) {
                        return new ReturnType(ReturnType.type.Break);
                    } else if (current.getReturnType() == ReturnType.type.Continue) {
                        return new ReturnType(ReturnType.type.Continue);
                    } else if (current.getReturnType() == ReturnType.type.Return) {
                        return new ReturnType(ReturnType.type.Return, current.getRetVal());
                    }
                }
                if(cur.getNext().isEmpty()){
                    return new ReturnType(ReturnType.type.Normal);
                }
                cur = (ifNode) cur.getNext().get();
            }
        }
        /**
         * instead of just using java return, we return a ReturnNode with return in it because we want to pass this up
         */
        if(statement instanceof returnNode){
            if(((returnNode) statement).getParameter().isPresent()){
                InterpreterDataType retVal = GetIDT(((returnNode) statement).getParameter().get(), locals);
                return new ReturnType(ReturnType.type.Return, retVal.getValue());
            }
            return new ReturnType(ReturnType.type.Return);
        }
        /**
         * simple - keeps gonig until the condition is false. I set anything thats not 0(which evals to false in AWK) to "1"
         * to make it easy to compare, because there are infinite numbers the user could have imputted
         */
        if (statement instanceof whileNode) {
            boolean checkBool = true;
            InterpreterDataType condition = GetIDT(((whileNode) statement).getCondition(), locals);

//            if(convToFloat(condition.getValue())){
//                if(Float.parseFloat(condition.getValue()) != 0){
//                    condition = new InterpreterDataType("1");
//                }
//            }
            if(convToFloat(condition.getValue())){
                if(Float.parseFloat(condition.getValue()) == 0){
                    checkBool = false;
                }
            }
            else if(condition.getValue().equals("")){
                checkBool = false;
            }
            while (checkBool) {
                ReturnType current = InterpretListOfStatements(((whileNode) statement).getBlock().getStatements(), locals);
                if (current.getReturnType() == ReturnType.type.Break) {
                    break;
                } else if (current.getReturnType() == ReturnType.type.Continue) {
//                    continue;
                } else if (current.getReturnType() == ReturnType.type.Return) {
                    return new ReturnType(ReturnType.type.Return, current.getRetVal());
                }
                condition = GetIDT(((whileNode) statement).getCondition(), locals);
                if(convToFloat(condition.getValue())){
                    if(Float.parseFloat(condition.getValue()) == 0){
                        checkBool = false;
                    }
                }
                else if(condition.getValue().equals("")){
                    checkBool = false;
                }
            }
            return new ReturnType(ReturnType.type.Normal);
        }
        else{
            // we call GETIDT to check for assignment and function calls. Also, AWK is lenient with statements - what is condiered
            // just an expression in Java can be a statement as well as an expression in AWK. So you can have just the number 100
            // as a statement in AWK. thats why we call GETIDT.
            try{
                return new ReturnType(ReturnType.type.Normal, GetIDT(statement, locals).getValue());
            }
            catch(Exception e){
                throw new Exception("not a valid statement in AWK! \n" + e.getMessage());
            }
        }
    }

    /**
     * we do this so we can look ahead in our block(like an if block, for, etc) and check if there are non normal returns
     * (breaks, returns, continues) and if so, we pass it up.
     * @param statements
     * @param locals
     * @return
     * @throws Exception
     */
    ReturnType InterpretListOfStatements(LinkedList<StatementNode> statements, HashMap<String, InterpreterDataType> locals) throws Exception {
        for(int i = 0; i < statements.size(); i++){
            ReturnType curr = ProcessStatement(locals, statements.get(i));
            if(curr.getReturnType() == ReturnType.type.Break){
                return new ReturnType(ReturnType.type.Break);
            }
            else if(curr.getReturnType() == ReturnType.type.Continue){
                return new ReturnType(ReturnType.type.Continue);
            }
            else if(curr.getReturnType() == ReturnType.type.Return){
                return new ReturnType(ReturnType.type.Return, curr.getRetVal());
            }
        }
        return new ReturnType(ReturnType.type.Normal);
    }

    /**
     * for built ins, we check if num of params are valid by checking the BUILTinFunctions integers.
     * for user defined, we can easily check by checking the functionDef params.
     * we make sure the hashmaps for variadic functions are properly constructed, putting the last few variables in the IADT
     * for non variadics we also set the hashmap properly.
     *
     * @param functionCall
     * @param localVariables
     * @return
     * @throws Exception
     */
    public String RunFunctionCall(functionCallNode functionCall, HashMap<String, InterpreterDataType> localVariables) throws Exception {
        String name = functionCall.getName();
        if(!this.definedFunctions.containsKey(name)){
            throw new Exception("Function does not exist");
        }
        FunctionDefinitionNode functionDef = definedFunctions.get(name);
        // For built in functions, I actually do all the checking inside the lambda function, so i dont have to do it here
        if(!(functionDef instanceof BuiltInFunctionDefinitionNode)){
            if(functionCall.getParams().size() != functionDef.getParameters().size()){
                throw new Exception("Wrong number of paramaters for function " + functionCall.getName());
            }
        }
        HashMap<String, InterpreterDataType> map = new HashMap<>();
        LinkedList<String> ll = new LinkedList<>();

        if(functionDef instanceof BuiltInFunctionDefinitionNode && ((BuiltInFunctionDefinitionNode) functionDef).isVariadic()){
//            if(functionCall.getParams().size() < ((BuiltInFunctionDefinitionNode) functionDef).minParams ||
//            functionCall.getParams().size() > ((BuiltInFunctionDefinitionNode) functionDef).maxParams){
//                throw new Exception("Function called with too many or too little params");
//            }
            for(int i = 0; i < ((BuiltInFunctionDefinitionNode) functionDef).maxParams; i++){
                if(functionCall.getParams().isEmpty() || functionCall.getParams().get(i) == null){
                    break;
                }
                if(i >= ((BuiltInFunctionDefinitionNode) functionDef).maxParams - 1){
                    List<Node> last = functionCall.getParams().subList(i, functionCall.getParams().size());
                    InterpreterArrayDataType Iadt = new InterpreterArrayDataType(new HashMap<>());
                    for(int j = 0; j < last.size(); j++){
                        Iadt.getHashmap().put(Integer.toString(j), GetIDT(last.get(j),localVariables));
                    }
                    map.put(Integer.toString(i), Iadt);
                    ll.add(Integer.toString(i));
                    break;
                }
                else{
                    map.put(Integer.toString(i), GetIDT(functionCall.getParams().get(i), localVariables));
                    ll.add(Integer.toString(i));
                }
            }
            return ((BuiltInFunctionDefinitionNode) functionDef).Execute.apply(map, ll);
        }

        if(functionDef instanceof BuiltInFunctionDefinitionNode){
            if(functionCall.getParams().size() < ((BuiltInFunctionDefinitionNode) functionDef).minParams ||
                    functionCall.getParams().size() > ((BuiltInFunctionDefinitionNode) functionDef).maxParams){
                throw new Exception("Function called with too many or too little params");
            }
            LinkedList<String> paramNames = new LinkedList<>();
            for(int i = 0; i < functionCall.getParams().size(); i++){
                paramNames.add(functionCall.getParams().get(i).toString());
            }
            for(int i = 0; i < functionCall.getParams().size(); i++){
                InterpreterDataType temp = GetIDT(functionCall.getParams().get(i), localVariables);
                map.put(functionCall.getParams().get(i).toString(), temp);
            }
            return ((BuiltInFunctionDefinitionNode) functionDef).Execute.apply(map, paramNames);
        }

        for(int i = 0; i < functionCall.getParams().size(); i++){
            InterpreterDataType temp = GetIDT(functionCall.getParams().get(i), localVariables);
            map.put(functionDef.getParameters().get(i), temp);
        }
//        if(functionDef instanceof BuiltInFunctionDefinitionNode){
//            LinkedList<String> paramNames = new LinkedList<>();
//            for(int i = 0; i < functionCall.getParams().size(); i++){
//                paramNames.add(functionCall.getParams().get(i).toString());
//            }
//            return ((BuiltInFunctionDefinitionNode) functionDef).Execute.apply(map, paramNames);
//        }
//        else{
        ReturnType ret = interpretBlock(new BlockNode(Optional.empty(), functionDef.getStatements()), localVariables);
        if (ret.getReturnType() == ReturnType.type.Return) {
            return ret.getRetVal();
        }
        return "";
//        }
    }

    /**
     * i do this because i want ot be able to catch exceptinos without finishing my code because its sadly
     * valid in awk to do hello + world and have it return 0 because neither of them are even valid
     *
     * @return
     */
    public boolean convToFloat(String convValue) {
        try {
            float retVal = Float.parseFloat(convValue);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * runs our entire AWK program for us - basically we are interpreting our AST
     * @throws Exception
     */
    public void InterpretProgram() throws Exception {
        HashMap<String, InterpreterDataType> emptyLocals = new HashMap<>();
        // I do this so I dont have to make a new empty hashmap every time i call interpretBlock
        for(int i = 0; i < program.getBeginblocks().size(); i++){
            if(interpretBlock(program.getBeginblocks().get(i), emptyLocals).getReturnType() == ReturnType.type.Return){
                throw new Exception("Trying to return in a Begin block, not a function");
            }
        }
        while(manager.SplitAndAssign()){
            for(int i = 0; i < program.getOtherblocks().size(); i++){
                interpretBlock(program.getOtherblocks().get(i), emptyLocals);
            }
        }
        for(int i = 0; i < program.getEndblocks().size(); i++){
            interpretBlock(program.getEndblocks().get(i), emptyLocals);
        }
    }

    /**
     * runs the entire block, making sure to only run if the condition is empty or its true.
     * @param block
     * @param locals
     * @return
     * we return a ReturnType so we can make sure that we can throw an exception later on if there was a return in a block
     * that wasnt a function defintion. Also, saves the return value of a function to pass up
     * @throws Exception
     */
    public ReturnType interpretBlock(BlockNode block, HashMap<String, InterpreterDataType> locals) throws Exception {
        InterpreterDataType temp = new InterpreterDataType();
        if(block.getCondition().isPresent()){
            temp = GetIDT(block.getCondition().get(), locals);
        }
        if(block.getCondition().isEmpty() || !(temp.getValue().equals("0") ||
                temp.getValue().equals("0.0") || temp.getValue().equals(""))){
            for(int i = 0; i < block.getStatements().size(); i++){
                ReturnType ret = ProcessStatement(locals, block.getStatements().get(i));
                if (ret.getReturnType() == ReturnType.type.Break) {
                    break;
                } else if (ret.getReturnType() == ReturnType.type.Continue) {
                    continue;
                } else if (ret.getReturnType() == ReturnType.type.Return) {
                    return new ReturnType(ReturnType.type.Return, ret.getRetVal());
                }
            }
        }
        return new ReturnType(ReturnType.type.Normal);
    }

}
