public class ReturnType {
    enum type{
        Normal, Break, Continue, Return
    }
    // remember Return can only be used in a function!!
    private type returnType;
    private String retVal;

    public ReturnType(type retType){
        this.returnType = retType;
    }
    public ReturnType(type retType, String returnString){
        this.returnType = retType;
        this.retVal = returnString;
    }

    public String toString(){
        if(retVal == null){
            return returnType + ""; // adding empty "" for my pookie bear compiler
        }
        else return returnType + ": " + retVal;
    }

    public type getReturnType(){
        return returnType;
    }

    public String getRetVal() {
        if(retVal == null){
            return "";
        }
        return retVal;
    }
}
