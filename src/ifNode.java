import java.util.Optional;

public class ifNode extends StatementNode{
    private Optional<Node> condition;
    private BlockNode thisBlock;
    private Optional<StatementNode> next;

    public ifNode(Optional<Node> condition, BlockNode statements, Optional<StatementNode> next){
        this.condition = condition;
        this.thisBlock = statements;
        this.next = next;
    }

    public void setNext(Optional<StatementNode> next){
        this.next = next;
    }

    public Optional<StatementNode> getNext(){
        return next;
    }

    public BlockNode getBlock() {
        return thisBlock;
    }

    public Optional<Node> getCondition() {
        return condition;
    }

    public String toString(){
        if(condition.isEmpty()){
            return "ELSE {" + thisBlock.getStatements() + "}\n";
        }
        if(next.isPresent()){
            return "IF (" + condition.get() + ") {" + thisBlock.getStatements() + " }" + "else " + next.get() + "\n";
        }
        else return "IF (" + condition.get() + ") " + thisBlock.getStatements() + "\n";
    }
}
