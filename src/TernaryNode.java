public class TernaryNode extends Node{
    private Node expression;
    private Node firstValue;
    private Node secondValue;

    public TernaryNode(Node expression, Node firstValue, Node secondValue){
        this.expression = expression;
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    @Override
    public String toString(){
        return expression + " ? " + firstValue + " : " + secondValue;
    }

    public Node getExpression() {
        return expression;
    }

    public Node getFirstValue() {
        return firstValue;
    }

    public Node getSecondValue() {
        return secondValue;
    }
}
