import java.util.Optional;

public class OperationNode extends Node {
    private Optional<Node> left;
    private Optional<Node> right;
    private possibleOps operator;


    // match and not match:
    // Pre/post increment/decrement, unary +/-
    // concat
    // in
    enum possibleOps {
        EQUALS, NOTEQUALS, LESSTHAN, LESSEQUAL, GREATERTHAN, GREATEREQUAL, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR,
        PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG, IN,
        EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, CONCATENATION
    }

    public OperationNode(Optional<Node> left, possibleOps type, Optional<Node> rightSide) {
        this.left = left;
        this.operator = type;
        this.right = rightSide;
    }

    public String toString() {
        if (right.isPresent() && left.isPresent()) {
            return left.get() + " " + operator.toString() + " " + right.get() + " ";
        } else if (left.isPresent()) {
            return left.get() + " " + operator.toString();
        } else return operator.toString() + " " + right.get();
    }

    public possibleOps getOperator(){
        return operator;
    }

    public boolean isLeftPresent(){
        return left.isPresent();
    }
    public boolean isRightPresent(){
        return right.isPresent();
    }

    public Node getRight(){
        return right.get();
    }

    public Node getLeft(){
        return left.get();
    }
}
