import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class myMain {

    public static void main(String[]args) throws Exception {
//        String content = "purple_ \"helloWorld\" lights";
//        Lexer myLex = new Lexer(content);
//        System.out.println(myLex.Lex());
        try {
//            Path myPath = Paths.get("myAwk.awk");
        Path myPath = Paths.get(args[0]);
            String myContent = new String(Files.readAllBytes(myPath)); // converts the file taken in as input to a string
            Lexer myLexerr = new Lexer(myContent);
            System.out.println(myLexerr.Lex());
        }
        catch(Exception e){
//            System.out.println(e.getMessage());
            System.out.println(e);
        }
    }
}
