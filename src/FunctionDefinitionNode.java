import java.util.LinkedList;

public class FunctionDefinitionNode extends Node {
    private String functionName;
    private LinkedList<String> parameters;
    private LinkedList<StatementNode> statements;

    public FunctionDefinitionNode(String funcName, LinkedList<String> parameters, LinkedList<StatementNode> statements){
        this.functionName = funcName;
        this.parameters = parameters;
        this.statements = statements;
    }
    public FunctionDefinitionNode(){

    }

    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    public String getFunctionName() {
        return functionName;
    }

    public LinkedList<String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "function " + functionName + " " + parameters.toString() + " " + statements.toString();
    }


}
