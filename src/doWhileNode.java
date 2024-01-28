import java.util.Optional;

public class doWhileNode extends StatementNode{
    private Optional<Node> condition;
    private BlockNode block;

    public doWhileNode(BlockNode block, Optional<Node> condition){
        this.condition = condition;
        this.block = block;
    }

    public String toString(){
        return "do { " + block.getStatements()+ "} while( " + condition.get() + ")\n";
    }

    public Node getConditon(){
        return condition.get();
    }
    public BlockNode getBlock(){
        return block;
    }

}
