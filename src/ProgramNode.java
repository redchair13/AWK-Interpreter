import java.util.LinkedList;

public class ProgramNode extends Node{
    private LinkedList<BlockNode> beginblocks;
    private LinkedList<BlockNode> endblocks;
    private LinkedList<BlockNode> otherblocks;
    private LinkedList<FunctionDefinitionNode> functionDefinitions;

    public ProgramNode(LinkedList<BlockNode> begins, LinkedList<BlockNode> ends,
                       LinkedList<BlockNode> others, LinkedList<FunctionDefinitionNode> funcs){
        this.beginblocks = begins;
        endblocks = ends;
        otherblocks = others;
        functionDefinitions = funcs;
    }

    @Override
    public String toString() {
        return functionDefinitions.toString() + beginblocks.toString() + otherblocks.toString() + endblocks.toString();
    }

    public void addBeginBlock(BlockNode block){
        beginblocks.add(block);
    }

    public void addEndBlock(BlockNode block){
        endblocks.add(block);
    }

    public void addBlock(BlockNode block){
        otherblocks.add(block);
    }

    public void addFunctionDefs(FunctionDefinitionNode func){
        functionDefinitions.add(func);
    }

    public LinkedList<BlockNode> getBeginblocks(){
        return beginblocks;
    }
    public LinkedList<BlockNode> getEndblocks(){
        return endblocks;
    }
    public LinkedList<BlockNode> getOtherblocks(){
        return otherblocks;
    }
    public LinkedList<FunctionDefinitionNode> getFunctionDefinitions(){
        return functionDefinitions;
    }

}
