public class InterpreterDataType {
    private String value;

    public InterpreterDataType(String data){
        this.value = data;
    }

    public InterpreterDataType(){
    }

    String getValue(){
        if(value != null){
            return value;
        }
        return null;
    }

    String changeValue(){
        return value;
    }

    public String toString() {
        return value;
    }
}
