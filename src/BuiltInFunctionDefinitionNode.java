import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode{
    private boolean Variadic;
    BiFunction<HashMap<String, InterpreterDataType>, LinkedList<String>, String> Execute;
    int minParams;
    int maxParams;

    public BuiltInFunctionDefinitionNode(BiFunction<HashMap<String, InterpreterDataType>, LinkedList<String>, String> thisLambda, boolean Variadic,
                                         int minParams, int maxParams){
        this.Variadic = Variadic;
        this.Execute = thisLambda;
        this.minParams = minParams;
        this.maxParams = maxParams;
    }

    public boolean isVariadic(){
        return Variadic;
    }


    public String toString(){
       return "help";
    }
    
}
