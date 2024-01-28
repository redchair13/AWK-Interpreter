import java.util.LinkedList;
import java.util.Optional;

public class BlockNode extends Node {
    private LinkedList<StatementNode> statements;
    private Optional<Node> condition;

    public BlockNode(Optional<Node> condition, LinkedList<StatementNode> statements){
        this.statements = statements;
        this.condition = condition;
    }

    @Override
    public String toString() {
        if(condition.isPresent()){
            return "(" + condition.get() + ")" + statements.toString();
        }
        else return statements.toString();
    }

    public LinkedList<StatementNode> getStatements(){
        return statements;
    }

    public void addStatement(Optional<StatementNode> statement){
        statements.add(statement.get());
        // YOU CAN ADD A STATEMENT HERE SAYING TO DO IT ONLY IF STATEMENT ACTUALLY EXISTS
    }

    public Optional<Node> getCondition() {
        return condition;
    }
}
