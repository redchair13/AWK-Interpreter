public class AssignmentNode extends StatementNode{
    private Node target;
    private Node expression;

    public Node getTarget(){
        return target;
    }
    public Node getExpression(){
        return expression;
    }

    public AssignmentNode(Node target, Node expression){
        this.expression = expression;
        this.target = target;
    }

    @Override
    public String toString() {
        return target + " = " + expression;
    }
}
