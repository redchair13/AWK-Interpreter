import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType{
    private HashMap<String, InterpreterDataType> map;

    public InterpreterArrayDataType(HashMap<String, InterpreterDataType> inpMap){
        this.map = inpMap;
    }

    public HashMap<String, InterpreterDataType> getHashmap(){
        return map;
    }

    public String toString() {
        return map.toString();
    }
}
