package regex.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tshun_000 on 2/9/2015.
 */
public enum RegexElement {
    /** parentheses */
    LPAREN("(", Type.PARENTHESE), RPAREN(")", Type.PARENTHESE),
    LBRACKET("[", Type.BRACKET), RBRACKET("]", Type.BRACKET),
    /** bi-operators */
    BRANCH("|", Type.ESCAPE), CONCAT("@", Type.ESCAPE),
    /** repeats */
    ALTERN("?", Type.ESCAPE),
    KLEENE("*", Type.ESCAPE), REPEAT("+", Type.ESCAPE),
    NGKLEENE("*?", Type.ESCAPE), NGREPEAT("+?", Type.ESCAPE),
    /** positions */
    START("^", Type.POSITION), END("$", Type.POSITION),
    /** others */
    ESCAPE("\\", Type.ESCAPE),
    /** in automaton */
    CAPTURESTART("CapStart", Type.CAPTURE), CAPTUREEND("CapEnd", Type.CAPTURE),
    WILDCARD(".", Type.ESCAPE), LITERAL("Literal", Type.LITERAL),
    SPLIT("Split", Type.SPLIT),
    MATCHED("Matched", Type.MATCHED);

    public final String symbol;
    public final Type type;

    private static final Map<String, RegexElement> stringToEscape = new HashMap<>();

    static {
        for (RegexElement symbol : RegexElement.values()) {
            stringToEscape.put(symbol.toString(), symbol);
        }
    }

    RegexElement(String symbol, Type type) {
        this.symbol = symbol;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return this.symbol;
    }

    public static RegexElement fromString(String symbol) {
        return stringToEscape.get(symbol);
    }

    enum Type {
        ESCAPE, LITERAL, CAPTURE, POSITION, BRACKET, PARENTHESE, MATCHED, SPLIT
    }
}

