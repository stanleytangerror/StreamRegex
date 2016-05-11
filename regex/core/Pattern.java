package regex.core;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Created by tshun_000 on 3/24/2015.
 */
public class Pattern<T> {
    private final boolean requiredStart;
    private final boolean requiredEnd;
    private NFA<T> nfa;

    private Pattern(PostExpression<T> postExpression, Function<String, T> constructor) {
        this.requiredStart = postExpression.isRequiredStart();
        this.requiredEnd = postExpression.isRequiredEnd();
        this.nfa = NFA.compile(postExpression);
    }

    boolean isRequiredStart() {
        return requiredStart;
    }

    boolean isRequiredEnd() {
        return requiredEnd;
    }

    NFA<T> getNfa() {
        return nfa;
    }

    public static <T> Pattern<T> compile(String regex, Function<String, T> constructor) {
        PostExpression<T> postExpression = PostExpression.compile(regex, constructor);
        return new Pattern<>(postExpression, constructor);
    }

    public <R> Matcher<T, R> matcher(List<R> input, BiPredicate<T, R> compare) {
        return new Matcher<>(this, input, compare);
    }


}
