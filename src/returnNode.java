import java.util.Optional;

public class returnNode extends StatementNode{
    private Optional<Node> parameter;

    public returnNode(Optional<Node> parameter){
        this.parameter = parameter;
    }

    public String toString(){
        if(parameter.isPresent()){
            return "RETURN " + parameter.get() + "\n";
        }
        return "RETURN \n";
    }

    public Optional<Node> getParameter() {
        return parameter;
    }
}
