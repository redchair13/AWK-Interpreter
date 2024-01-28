import java.util.LinkedList;

public class functionCallNode extends StatementNode{
    private String name;
    private LinkedList<Node> params;

    public functionCallNode(String name, LinkedList<Node> params){
        this.name = name;
        this.params = params;
    }

    public void addParam(Node param){
        params.add(param);
    }

    @Override
    public String toString() {
        if(params.isEmpty()){
            return name + "()";
        }
        return name + "(" + params + ")";
    }

    public String getName() {
        return name;
    }

    public LinkedList<Node> getParams() {
        return params;
    }
}
