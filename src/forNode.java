import java.util.Optional;

public class forNode extends StatementNode{
    private Optional<Node> firstExpr;
    private Optional<Node> secondExpr;
    private Optional<Node> thirdExpr;
    private BlockNode block;

    public BlockNode getBlock() {
        return block;
    }

    public Optional<Node> getFirstExpr() {
        return firstExpr;
    }

    public Optional<Node> getSecondExpr() {
        return secondExpr;
    }

    public Optional<Node> getThirdExpr() {
        return thirdExpr;
    }

    public forNode(Optional<Node> firstExpr, Optional<Node> secondExpr, Optional<Node> thirdExpr, BlockNode block){
        this.firstExpr = firstExpr;
        this.secondExpr = secondExpr;
        this.thirdExpr = thirdExpr;
        this.block = block;
    }

    public String toString(){
        String retval = "for(";
        // MAYBE YOU HAVE TO HAVE .GET().toString  INSTEAD OF JUST HAVING .GET()
        if(firstExpr.isPresent()){
            retval = retval + firstExpr.get();
        }
        retval += ";";
        if(secondExpr.isPresent()){
            retval = retval + secondExpr.get() ;
        }
        retval += ";";
        if(thirdExpr.isPresent()){
            retval = retval + thirdExpr.get();
        }
        return retval + ")" + block.getStatements() + "\n";
    }

}
