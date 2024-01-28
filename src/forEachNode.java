import java.util.Optional;

public class forEachNode extends StatementNode{
    private Optional<Node> forEachStatement;
    private BlockNode block;

    public forEachNode(Optional<Node> statement, BlockNode block){
        this.forEachStatement = statement;
        this.block = block;
    }

    public String toString(){
        return "for(" + forEachStatement.get() + ") " + block.getStatements() + "\n";
    }

    public Node getForEachStatement() {
        return forEachStatement.get();
    }

    public BlockNode getBlock() {
        return block;
    }
}
