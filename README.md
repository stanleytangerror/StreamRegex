# StreamRegex

An **Object stream** matching regex engine by Java 1.8, matching given alignment of **object list**, supporting:

1. Position: start (`^`), end (`$`)
2. Literal: escape (`\`), wildcard (`.`)
3. Repeat: greedy (`*`, `+`), non-greedy (`*?`, `+?`), alternate (`?`)
4. Group: subexp (`(`, `)`, with anonymous capture), branch (`[`, `]`)

The approach is:

1. translate regex sequence to post ordered expression
2. translate post ordered expression into NFA
3. use NFA to match input sequence

Note: the regex sequence and input sequence might not be string, any type can be an matching sequence, only if **the pattern class in regex (e.g., `ValueChecker::new`)** and **matching method (e.g., `VelueChecker::check`)** is provided.

## Usage

```
    public static void main(String[] args) {
        /** regex */
        String regex = "^(<3 >2+?) (>2) =5+ .* %2$";
        /** input */
        Integer[] temp = {1, 9, 3, 3, 4, 5, 5, 7, 6, 8, 10};
        List<Integer> input = Arrays.asList(temp);

        class ValueChecker {
            char operator;
            int operand;

            /** construct a value pattern */
            ValueChecker(String str) {
                operator = str.charAt(0);
                operand = Integer.valueOf(str.substring(1));
            }

            /** matching method between patteen and value */
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
```
