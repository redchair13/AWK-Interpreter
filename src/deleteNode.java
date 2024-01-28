import java.util.Optional;

public class deleteNode extends StatementNode{
    private Optional<Node> array;

    public deleteNode(Optional<Node> array){
        this.array = array;
    }

    public String toString(){
        return "DELETE " + array.get();
    }

    public Node getValue(){
        return array.get();
    }
}
