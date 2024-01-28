import java.util.LinkedList;
import java.util.Optional;

public class TokenManager {
    private LinkedList<Token> myTokens;

    public TokenManager(LinkedList<Token> tokens){
        myTokens = tokens;
    }

    /**
     * peeks j Tokens ahead, and returns it wrapped in an optional. we return an optional empty if j is out of bounnds
     * so we dont have to deal with too many nulls
     * @param j
     * @return
     */
    public Optional<Token> Peek(int j){
        if(j < myTokens.size()){
            return Optional.ofNullable(myTokens.get(j));
        }
        return Optional.empty();
    }


    boolean MoreTokens(){
        return !myTokens.isEmpty();
    }

    /**
     * extremely useful for the parser. ensures that the next token is what we passed in, then removes it from our token
     * list so we dont have to keep peeking ahead in the parser.
     * also returns an optional.empty if it doesnt match
     * @param t
     * @return
     */
    Optional<Token> MatchAndRemove(Token.TokenType t){
        if(MoreTokens() && (myTokens.getFirst().getTokenType().equals(t))){
            Optional<Token> opt = Optional.of(myTokens.removeFirst());
            return opt;
        }
        return Optional.empty();
    }

}
