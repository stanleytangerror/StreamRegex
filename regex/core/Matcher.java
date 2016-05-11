package regex.core;

import regex.util.Pair;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Created by tshun_000 on 3/24/2015.
 */
public class Matcher<T, R> {
    private final boolean requiredStart;
    private final boolean requiredEnd;
    private final int captureCound;
    private Map<Integer, Stack<Pair<Integer, Integer>>> groupMap;
    private BiPredicate<T, R> compare;
    private List<R> input;
    private NFA<T> nfa;
    private boolean matched;
    private int from;
    private int to;

    Matcher(Pattern<T> pattern, List<R> input, BiPredicate<T, R> compare) {
        this.requiredStart = pattern.isRequiredStart();
        this.requiredEnd = pattern.isRequiredEnd();
        this.captureCound = pattern.getNfa().getGroupCount();
        this.groupMap = new HashMap<>();
        this.compare = compare;
        this.input = input;
        this.nfa = pattern.getNfa();
        this.from = -1;
        this.to = -1;
        this.matched = false;
        for (int i = 0; i <= this.captureCound; ++i)
            groupMap.put(i, new Stack<>());
    }

    public boolean match() {
        int len = input.size();
        for (int i = 0; i < len; ++i) {
            if (requiredStart && i > 0)
                break;
            this.from = i;
            if (search(nfa.getStart(), i)) {
                if (requiredEnd && to != len)
                    continue;
                matched = true;
                groupMap.get(0).push(new Pair<>(this.from, this.to));
                return true;
            }
        }
        return false;
    }

    private boolean search(NFA<T>.State state, int from) {
        int len = input.size();
        if (from > len) {
            System.err.println("Error : error happenes in matching");
            return false;
        }
        if (from == len) {
            switch (state.node.type) {
                case WILDCARD:
                case LITERAL:
                    return false;
                case MATCHED:
                    this.to = from;
                    return true;
                default:
                    break;
            }
        }
        switch (state.node.type) {
            case CAPTURESTART:
                PostExpression.CaptureStartNode csnode = (PostExpression.CaptureStartNode) state.node;
                groupMap.get(csnode.id).push(new Pair<>(from, -1));
                for (NFA<T>.State next : state.outList) {
                    if (search(next, from))
                        return true;
                }
                return false;
            case CAPTUREEND:
                PostExpression.CaptureEndNode cenode = (PostExpression.CaptureEndNode) state.node;
                groupMap.get(cenode.id).peek().setSecond(from);
                for (NFA<T>.State next : state.outList) {
                    if (search(next, from))
                        return true;
                }
                return false;
            case BRANCH:
            case ALTERN:
            case NGREPEAT:
            case NGKLEENE:
            case KLEENE:
            case REPEAT:
                for (NFA<T>.State next : state.outList) {
                    if (search(next, from))
                        return true;
                }
                return false;
            case WILDCARD:
                for (NFA<T>.State next : state.outList) {
                    if (search(next, from + 1))
                        return true;
                }
                return false;
            case LITERAL:
                PostExpression.LiteralNode<T> lnode = (PostExpression.LiteralNode<T>) state.node;
                if (compare.test(lnode.content, input.get(from))) {
                    for (NFA<T>.State next : state.outList) {
                        if (search(next, from + 1))
                            return true;
                    }
                }
                return false;
            case MATCHED:
                if (requiredEnd)
                    return false;
                this.to = from;
                return true;
            default:
                System.err.println("Error : in NFA");
                return false;

        }
    }

    private Pair<Integer, Integer> groupBound(int groupNo) {
        if (!matched) {
            System.err.println("Error : call group() without matched result");
            return null;
        }
        if (groupNo > captureCound)
            return null;
        Stack<Pair<Integer, Integer>> stack = groupMap.get(groupNo);
        Pair<Integer,Integer> pair;
        while (!stack.empty()) {
            pair = stack.peek();
            if (stack.peek().getSecond() != -1)
                return pair;
            stack.pop();
        }
        return null;
    }

    public List<R> group(int groupNo) {
        Pair<Integer, Integer> pair = groupBound(groupNo);
        if (pair == null)
            return new ArrayList<>();
        return input.subList(pair.getFirst(), pair.getSecond());
    }

    public MatchedResult<R> getResult() {
        if (!matched)
            return null;
        MatchedResult<R> result = new MatchedResult<>(captureCound, input);
        for (int i = 0; i <= captureCound; ++i) {
            result.groups.add(groupBound(i));
        }
        return result;
    }

    public static class MatchedResult<R> {
        List<Pair<Integer, Integer>> groups;
        List<R> input;

        public MatchedResult(int captureCount, List<R> input) {
            this.groups = new ArrayList<>(captureCount + 1);
            this.input = input;
        }

        public List<R> group(int groupNo) {
            Pair<Integer, Integer> pair = groups.get(groupNo);
            if (pair == null)
                return new ArrayList<>();
            return input.subList(pair.getFirst(), pair.getSecond());
        }
    }

    public static void main(String[] args) {
//        String regex = "(start)([a bbb] (c*) d|uvwx(yy)* )+(end)";
        /** regex */
        String regex = "^(<3 >2+?) (>2) =5+ .* %2$";
        /** input */
        Integer[] temp = {1, 9, 3, 3, 4, 5, 5, 7, 6, 8, 10};
        List<Integer> input = Arrays.asList(temp);

        class ValueChecker {
            char operator;
            int operand;

            ValueChecker(String str) {
                operator = str.charAt(0);
                operand = Integer.valueOf(str.substring(1));
            }

            boolean check(int value) {
                switch (operator) {
                    case '<':
                        return value < operand;
                    case '>':
                        return value > operand;
                    case '=':
                        return value == operand;
                    case '%':
                        return value % operand == 0;
                    default:
                        return false;
                }
            }
        }
        Pattern<ValueChecker> pattern = Pattern.compile(regex, ValueChecker::new);
        Matcher<ValueChecker, Integer> matcher = pattern.matcher(input, ValueChecker::check);
        if (matcher.match())
        for (int i = 0; i <= matcher.captureCound; ++i)
            System.out.println("Group[" + i + "] = " + matcher.group(i));
    }
}
