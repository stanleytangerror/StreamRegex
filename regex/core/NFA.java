package regex.core;

import regex.core.PostExpression.Node;
import regex.util.Pair;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Created by tshun_000 on 2/9/2015.
 */
public class NFA<T> {
    private final int groupCount;
    private State start;

    private NFA(PostExpression<T> postExpression) {
        this.groupCount = postExpression.getCaptureCount();
        parse(postExpression.getSequence());
    }

    int getGroupCount() {
        return groupCount;
    }

    State getStart() {
        return start;
    }

    static <T> NFA<T> compile(PostExpression<T> postExpression) {
        return new NFA<>(postExpression);
    }
//    enum Type {
//        SPLIT, MATCHED, LITERAL, WILDCARD, GREEDY, NOGREEDY
//    }

    class State {
        Node node;
        List<State> outList;

        State(Node node, State ... outs) {
            this.node = node;
            this.outList = new LinkedList<>();
            for (State out : outs)
                this.outList.add(out);
        }

        @Override
        public String toString() {
            return node.toString();
        }

        void addOut(State out) {
            this.outList.add(out);
        }

        void addOut(int pos, State out) {
            this.outList.add(pos, out);
        }
    }

    class Fragment {
        private State start;
        private List<State> outList;

        Fragment(State start, List<State> outList) {
            this.start = start;
            this.outList = outList;
        }

        Fragment(State start, State... outs) {
            this(start, new LinkedList<>());
            if (outs.length > 0)
            for (State out : outs)
                this.outList.add(out);
            else
                this.outList.add(start);
        }

        State getStart() {
            return start;
        }

        List<State> getOutList() {
            return outList;
        }

        void setStart(State start) {
            this.start = start;
        }

        void setOutList(List<State> outList) {
            this.outList = outList;
        }

        @Override
        public String toString() {
            return start + " -> " + outList.toString();
        }

        void patch(Fragment next) {
            for (State out : this.outList) {
                switch (out.node.type) {
                    case NGKLEENE:
                    case NGREPEAT:
                        out.addOut(0, next.getStart());
                        break;
                    default:
                        out.addOut(next.getStart());
                }
            }
            this.setOutList(next.getOutList());
        }
    }

    private void parse(List<Node> sequence) {
        Stack<Fragment> stack = new Stack<>();
        Fragment f1, f2, f;
        List<State> list;
        for (Node node : sequence) {
            switch (node.type) {
                case LITERAL:
                case WILDCARD:
                    f = new Fragment(new State(node));
                    stack.push(f);
                    break;
                case CONCAT:
                    f1 = stack.pop();
                    f2 = stack.pop();
                    f2.patch(f1);
                    stack.push(f2);
                    break;
                case BRANCH:
                    f1 = stack.pop();
                    f2 = stack.pop();
                    f = new Fragment(new State(node, f1.getStart(), f2.getStart()));
                    list = new LinkedList<>(f1.getOutList());
                    list.addAll(f2.getOutList());
                    f.setOutList(list);
                    stack.push(f);
                    break;
                case KLEENE:
                case NGKLEENE:
                    f1 = stack.pop();
                    f = new Fragment(new State(node, f1.getStart()));
                    f1.patch(f);
                    list = new LinkedList<>();
                    list.add(f.getStart());
                    f.setOutList(list);
                    stack.push(f);
                    break;
                case REPEAT:
                case NGREPEAT:
                    f1 = stack.pop();
                    f = new Fragment(new State(node, f1.getStart()));
                    f1.patch(f);
                    stack.push(f1);
                    break;
                case LPAREN: case RPAREN: case ESCAPE:
                    break;
                case ALTERN:
                    f1 = stack.pop();
                    f = new Fragment(new State(node, f1.getStart()));
                    list = new LinkedList<>();
                    list.add(f.getStart());
                    list.addAll(f1.getOutList());
                    f.setOutList(list);
                    stack.push(f);
                    break;
                case CAPTURESTART:
                    f1 = stack.pop();
                    f = new Fragment(new State(node));
                    f1.patch(new Fragment(new State(new PostExpression.CaptureEndNode(((PostExpression.CaptureStartNode)node).id))));
                    f.patch(f1);
                    stack.push(f);
                    break;
                default:
                    System.err.println("Error : error in post expression");
                    System.exit(1);
            }
        }
        Fragment startFrag = stack.pop();
        startFrag.patch(new Fragment(new State(new PostExpression.MatchedNode())));
        this.start = startFrag.getStart();
    }

    public static void main(String[] args) {
//        String regex = "^ root ( . ) $";
        String regex = "[a b c]*(qwe|asdf)+";
//        String regex = "a . ? e";
//        NFA<String> nfa = NFA.compile(regex);
    }
}
