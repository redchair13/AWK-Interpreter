import java.util.Optional;
public class whileNode extends StatementNode{
    private Optional<Node> condition;
    private BlockNode block;

    public whileNode(Optional<Node> condition, BlockNode block){
        this.condition = condition;
        this.block = block;
    }

    public String toString(){
        return "while (" +  condition.get() + ") " + block.getStatements() + "\n";
    }

    public Node getCondition() {
        return condition.get();
    }

    public BlockNode getBlock() {
        return block;
    }
}
