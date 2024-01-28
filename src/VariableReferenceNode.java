import java.util.Optional;

public class VariableReferenceNode extends Node{
    private String name;
    private Optional<Node> index;

    public VariableReferenceNode(String name, Optional<Node> index){
        this.name = name;
        this.index = index;
    }

    public boolean isArrayReference(){
        if(index == null){
            return false;
        }
        return index.isPresent();
    }

    // I only use this when I know theres a index anyway so im not finna get a Null pointer exception
    public Node getIndex(){
        return index.get();
    }

    public String getName(){
        return name;
    }

    public String toString(){
        if(index != null && index.isPresent()){
            return name + " [" + index.get() + "]";
        }
        else return name;
    }
}
