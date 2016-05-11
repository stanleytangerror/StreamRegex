package regex.core;


import regex.util.Pair;

import java.util.*;
import java.util.function.Function;

/**
 * Created by tshun_000 on 2/9/2015.
 */
public class PostExpression<T> {
    private boolean requiredStart;
    private boolean requiredEnd;
    private List<Node> sequence;
    private int captureCount;
    private Function<String, T> elementConstructor;

    public boolean isRequiredStart() {
        return requiredStart;
    }

    public boolean isRequiredEnd() {
        return requiredEnd;
    }

    public List<Node> getSequence() {
        return sequence;
    }

    public int getCaptureCount() {
        return captureCount;
    }

    public Function<String, T> getElementConstructor() {
        return elementConstructor;
    }

    private PostExpression(Function<String, T> elementConstructor){
        this.requiredStart = false;
        this.requiredEnd = false;
        this.sequence = new LinkedList<>();
        this.captureCount = 0;
        this.elementConstructor = elementConstructor;
    }

    static class Node {
        RegexElement type;

        Node(String symbol) {
            this.type = RegexElement.fromString(symbol);
        }

        Node(RegexElement type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

    static class MatchedNode extends Node {
        public MatchedNode() {
            super(RegexElement.MATCHED);
        }

        @Override
        public String toString() {
            return "Matched";
        }
    }

    static class LiteralNode<T> extends Node {
        T content;

        LiteralNode(T content) {
            super(RegexElement.LITERAL);
            this.content = content;
        }

        @Override
        public String toString() {
            return content.toString();
        }
    }

    static class LParenNode extends Node {
        int id;

        LParenNode(int id) {
            super(RegexElement.LPAREN);
            this.id = id;
        }
    }

    static class CaptureStartNode extends Node {
        int id;

        CaptureStartNode(int id) {
            super(RegexElement.CAPTURESTART);
            this.id = id;
        }

        @Override
        public String toString() {
            return "CapStart#" + id;
        }
    }

    static class CaptureEndNode extends Node {
        int id;

        CaptureEndNode(int id) {
            super(RegexElement.CAPTUREEND);
            this.id = id;
        }

        @Override
        public String toString() {
            return "CapEnd#" + id;
        }
    }

    private String preprocess(String regex) {
        if (regex == null || regex.equals(""))
            return "";
        regex = regex.trim().replaceAll("\\s+", " ");
        int start = 0, end = regex.length();
        if (regex.startsWith(RegexElement.START.toString())) {
            requiredStart = true;
            start++;
        }
        if (regex.endsWith(RegexElement.END.toString())) {
            requiredEnd = true;
            end--;
        }
        return regex.substring(start, end);
    }

    private List<Node> segment(String regex) {
        List<Node> result = new LinkedList<>();
        char[] array = regex.toCharArray();
        int len = array.length;
        int i = 0;
        StringBuilder buff = new StringBuilder();
        while (i < len) {
            char ch = array[i];
            switch (ch) {
                case '?':
                case ')':
                case '[':
                case ']':
                case '|':
                case '.':
                    if (buff.length() != 0) {
                        result.add(new LiteralNode<>(elementConstructor.apply(buff.toString())));
                        buff.delete(0, buff.length());
                    }
                    result.add(new Node(String.valueOf(ch)));
                    i++;
                    break;
                case '(':
                    if (buff.length() != 0) {
                        result.add(new LiteralNode<>(elementConstructor.apply(buff.toString())));
                        buff.delete(0, buff.length());
                    }
                    captureCount++;
                    result.add(new LParenNode(captureCount));
                    i++;
                    break;
                case ' ':
                    if (buff.length() != 0) {
                        result.add(new LiteralNode<>(elementConstructor.apply(buff.toString())));
                        buff.delete(0, buff.length());
                    }
                    i++;
                    break;
                case '+':
                case '*':
                    if (buff.length() != 0) {
                        result.add(new LiteralNode<>(elementConstructor.apply(buff.toString())));
                        buff.delete(0, buff.length());
                    }
                    if (i + 1 < len && array[i + 1] != '?') {
                        /** greedy repeat */
                        result.add(new Node(String.valueOf(ch)));
                        i++;
                    } else {
                        /** non-greedy repeat */
                        result.add(new Node(String.valueOf(ch) + "?"));
                        i += 2;
                    }
                    break;
                case '\\':
                    if (i + 1 < len) {
                        buff.append(array[i + 1]);
                        i += 2;
                    } else {
                        System.err.println("Error : regex error at " + i);
                        System.exit(1);
                    }
                    break;
                default:
                    buff.append(String.valueOf(ch));
                    i++;
                    break;
            }
        }
        if (buff.length() != 0) {
            result.add(new LiteralNode<>(elementConstructor.apply(buff.toString())));
            buff.delete(0, buff.length());
        }
        return result;
    }

    /**
     *  insert "|"s between atoms in brackets,
     *  where should not exist sub-module
     */
    private List<Node> resolveBracket(List<Node> segmented) {
        int branCount = 0;
        boolean inBracket = false;
        List<Node> result = new LinkedList<>();
        for (Node node : segmented) {
            switch (node.type) {
                case LBRACKET:
                    inBracket = true;
                    branCount = 0;
                    result.add(node);
                    break;
                case WILDCARD:case LITERAL:
                    if (inBracket) {
                        if (branCount >= 1) {
                            result.add(new Node(RegexElement.BRANCH));
                            branCount--;
                        }
                        result.add(node);
                        branCount++;
                    } else {
                        result.add(node);
                    }
                    break;
                case RBRACKET:
                    inBracket = false;
                    result.add(node);
                    break;
                default:
                    if (inBracket) {
                        /** escapes should not appears in brackets */
                        System.err.println("Error : escapes should not appear between \"[]\"");
                        System.exit(1);
                    }
                    result.add(node);
                    break;
            }
        }
        return result;
    }

    private void parse(List<Node> segmented) {
        Stack<Node> lparenStack = new Stack<>();
        Stack<Pair<Integer, Integer>> levelStack = new Stack<>();
        int atomCount = 0, branCount = 0;
        for (Node node : segmented) {
            switch (node.type) {
                case LITERAL:
                case WILDCARD:
                    if (atomCount >= 2) {
                        sequence.add(new Node(RegexElement.CONCAT));
                        atomCount--;
                    }
                    sequence.add(node);
                    atomCount++;
                    break;
                case KLEENE:
                case REPEAT:
                case ALTERN:
                case NGKLEENE:
                case NGREPEAT:
                    if (atomCount == 0) {
                        System.err.println("Error : no operand for " + node.type.toString());
                        return;
                    }
                    sequence.add(node);
                    break;
                case LPAREN:
                case LBRACKET:
                    if (atomCount >= 2) {
                        sequence.add(new Node(RegexElement.CONCAT));
                        atomCount--;
                    }
                    levelStack.push(new Pair<>(atomCount, branCount));
                    atomCount = 0;
                    branCount = 0;
                    lparenStack.push(node);
                    break;
                case RBRACKET:
                case RPAREN:
                    if (lparenStack.empty() ||
                            !lparenStack.peek().type.getType().equals(node.type.getType())) {
                        System.err.println("Error : \"()\" or \"[]\" not matched");
                        return;
                    }
                    if (atomCount == 0 || levelStack.empty()) {
                        System.err.println("Error : no operand for \"()\"");
                        return;
                    }
                    while (atomCount >= 2) {
                        sequence.add(new Node(RegexElement.CONCAT));
                        atomCount--;
                    }
                    while (branCount >= 1) {
                        sequence.add(new Node(RegexElement.BRANCH));
                        branCount--;
                    }
                    Pair<Integer, Integer> pair = levelStack.pop();
                    atomCount = pair.getFirst() + 1;
                    branCount = pair.getSecond();
                    Node paren = lparenStack.pop();
                    if (node.type.equals(RegexElement.RPAREN)
                            && paren instanceof LParenNode)
                        sequence.add(new CaptureStartNode(((LParenNode) paren).id));
                    break;
                case BRANCH:
                    if (atomCount == 0) {
                        System.err.println("Error : no operand for " + node.type.toString());
                        return;
                    }
                    /** catenation has higher priority than branch */
                    while (atomCount >= 2) {
                        sequence.add(new Node(RegexElement.CONCAT));
                        atomCount--;
                    }
                    atomCount = 0;
                    branCount++;
                    break;
                default:
                    break;
            }
        }
        if (!levelStack.isEmpty()) {
            System.err.println("Error : \"()\" or \"[]\" not matched");
            return;
        }
        while (atomCount >= 2) {
            sequence.add(new Node(RegexElement.CONCAT));
            atomCount--;
        }
        while (branCount > 0) {
            sequence.add(new Node(RegexElement.BRANCH));
            branCount--;
        }
    }

    static <T> PostExpression<T> compile(String regex, Function<String, T> constructor) {
        PostExpression<T> exp = new PostExpression<>(constructor);
        String temp = exp.preprocess(regex);
        System.err.println(temp);
        List<Node> list = exp.segment(temp);
        System.err.println(list.toString());
        list = exp.resolveBracket(list);
        System.err.println(list.toString());
        exp.parse(list);
        System.err.println(exp.sequence);
        return exp;
    }

    public static void main(String[] args) {
        String regex = "^(a(b|(wefa)sdf)).a+?[asdf werwe]$";
        PostExpression<String> exp = PostExpression.compile(regex, String::toString);
    }
}
