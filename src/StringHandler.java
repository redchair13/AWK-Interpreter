public class StringHandler {

    private String document;
    private int index; // the "finger" in our idea of reading input

    /**
     * looks “i” characters ahead and returns that character; doesn’t move the index
     * @param i
     * @return
     */
    char peek(int i){
        return document.charAt(index + i);
    }

    /**
     * returns a string of the next “i” characters but doesn’t move the index
     * @param i
     * @return
     */
    public String PeekString(int i){
        if(index + i > document.length() - 1){
            return null;
        }
        return document.substring((index + 1), index + 1 + i);
    }

    /**
     * returns the next character and moves the index
     * @return
     */
    public char GetChar(){
        return document.charAt(++index);
    }

    /**
     * moves the index ahead “i” positions
     * @param i
     */
    public void Swallow(int i){
        index = index + i;
        return;
    }

    /**
     *returns true if we are at the end of the document
     * @return
     */
    public boolean IsDone(){
        return (index >= document.length() - 1);
       // return false;
    }

    /**
     * returns the rest of the document as a string
     * @return
     */
    public String Remainder(){
        return document.substring(index);
    }

    public StringHandler(String file){
        this.document = file;
        this.index = -1;
    }

}