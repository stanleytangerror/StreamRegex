package regex.util;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Vivin Paliath on 1/26/2015.
 * site: http://vivin.net/2010/01/30/generic-n-ary-tree-in-java/
 */
public class GenericTree {

	enum GenericTreeTraversalOrderEnum {
		PRE_ORDER,
		POST_ORDER
	}

	public static <T> int getNumberOfNodes(GenericTreeNode<T> root) {
		int numberOfNodes = 0;

		if (root != null) {
			numberOfNodes = getNumberOfNodesHelper(root) + 1; // 1 for the
																	// root!
		}

		return numberOfNodes;
	}

	private static <T> int getNumberOfNodesHelper(GenericTreeNode<T> node) {
		int numberOfNodes = node.getNumberOfChildren();

		for (GenericTreeNode<T> child : node.getChildren()) {
			numberOfNodes += getNumberOfNodesHelper(child);
		}

		return numberOfNodes;
	}

	public static <T> boolean exists(GenericTreeNode<T> root, GenericTreeNode<T> nodeToFind) {
		return (find(root, nodeToFind) != null);
	}

	public static <T> List<GenericTreeNode<T>> build(GenericTreeNode<T> node,
			GenericTreeTraversalOrderEnum traversalOrder) {
		List<GenericTreeNode<T>> traversalResult = new ArrayList<>();

		if (traversalOrder == GenericTreeTraversalOrderEnum.PRE_ORDER) {
			buildPreOrder(node, traversalResult);
		}

		else if (traversalOrder == GenericTreeTraversalOrderEnum.POST_ORDER) {
			buildPostOrder(node, traversalResult);
		}

		return traversalResult;
	}

	private static <T> void buildPreOrder(GenericTreeNode<T> node,
			List<GenericTreeNode<T>> traversalResult) {
		traversalResult.add(node);

		for (GenericTreeNode<T> child : node.getChildren()) {
			buildPreOrder(child, traversalResult);
		}
	}

	public static <T> void preOrderWalk(GenericTreeNode<T> root, Consumer<? super T> consumer) {
		if (root == null)
			return;
		consumer.accept(root.getData());
		for (GenericTreeNode<T> child : root.getChildren()) {
			preOrderWalk(child, consumer);
		}
	}

	private static <T> void buildPostOrder(GenericTreeNode<T> node,
			List<GenericTreeNode<T>> traversalResult) {
		for (GenericTreeNode<T> child : node.getChildren()) {
			buildPostOrder(child, traversalResult);
		}

		traversalResult.add(node);
	}

	public static <T> Map<GenericTreeNode<T>, Integer> buildWithDepth(
			GenericTreeNode<T> node,
			GenericTreeTraversalOrderEnum traversalOrder) {
		Map<GenericTreeNode<T>, Integer> traversalResult = new LinkedHashMap<GenericTreeNode<T>, Integer>();

		if (traversalOrder == GenericTreeTraversalOrderEnum.PRE_ORDER) {
			buildPreOrderWithDepth(node, traversalResult, 0);
		}

		else if (traversalOrder == GenericTreeTraversalOrderEnum.POST_ORDER) {
			buildPostOrderWithDepth(node, traversalResult, 0);
		}

		return traversalResult;
	}

	private static <T> void buildPreOrderWithDepth(GenericTreeNode<T> node,
			Map<GenericTreeNode<T>, Integer> traversalResult, int depth) {
		traversalResult.put(node, depth);

		for (GenericTreeNode<T> child : node.getChildren()) {
			buildPreOrderWithDepth(child, traversalResult, depth + 1);
		}
	}

	private static <T> void buildPostOrderWithDepth(GenericTreeNode<T> node,
			Map<GenericTreeNode<T>, Integer> traversalResult, int depth) {
		for (GenericTreeNode<T> child : node.getChildren()) {
			buildPostOrderWithDepth(child, traversalResult, depth + 1);
		}

		traversalResult.put(node, depth);
	}

	public static <T, P> List<T> match(GenericTreeNode<T> root, GenericTreeNode<P> pattern, BiPredicate<T, P> nodeDataMatcher) {
		if (root == null || pattern == null)
			return null;
		List<T> sourceList = new ArrayList<>();
		List<P> patternList = new ArrayList<>();
		preOrderWalk(root, data -> sourceList.add(data));
		preOrderWalk(pattern, data -> patternList.add(data));
		List<Integer> matchIndex = listMatch(sourceList, patternList, nodeDataMatcher);
		if (matchIndex == null)
			return null;
		List<T> result = new ArrayList<>();
		matchIndex.forEach(index -> result.add(sourceList.get(index)));
		return result;
	}

	private static <T, P> List<Integer> listMatch(List<T> source, List<P> pattern, BiPredicate<T, P> nodeDataMatcher) {
		int srcLen = source.size();
		int patLen = pattern.size();
		if (srcLen == 0 || patLen == 0)
			return null;

		int[][] dp = new int[srcLen][patLen];
		int matchLen = 0;
		Pair<Integer, Integer> matchEnd = new Pair<>(0, 0);
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < srcLen; ++i) {
			for (int j = 0; j < patLen; ++j) {
				boolean matched = nodeDataMatcher.test(source.get(i), pattern.get(j));
				if (i == 0 && j == 0) {
					dp[i][j] = (matched ? 1 : 0);
				} else if (i == 0) {
					dp[i][j] = Math.max(dp[i][j - 1], matched ? 1 : 0);
				} else if (j == 0) {
					dp[i][j] = Math.max(dp[i - 1][j], matched ? 1 : 0);
				} else {
					int temp = Math.max(dp[i][j - 1], dp[i - 1][j]);
					dp[i][j] = Math.max(temp, dp[i - 1][j - 1] + (matched ? 1 : 0));
				}
				if (matchLen < dp[i][j]) {
					matchLen = dp[i][j];
					matchEnd = new Pair<>(i, j);
				}
			}
		}
		if (matchLen != patLen)
			return null;
		Pair<Integer, Integer> cur = new Pair<>(matchEnd);
		int pre = matchLen;
		result.add(0, cur.getFirst());
		while (pre > 0) {
			cur = backTraceHelper(pre - 1, dp, new Pair<>(cur.getFirst() + 1, cur.getSecond() + 1));
			result.add(cur.getFirst());
			-- pre;
		}
		return result;
	}

	private static Pair<Integer, Integer> backTraceHelper(int value, int[][] dp, Pair<Integer, Integer> size) {
		for (int dis = 0; dis <= Math.min(size.getFirst(), size.getSecond()); ++dis) {
			for (int i = 1; i <= dis; ++i) {
				if (dp[size.getFirst() - i][size.getSecond() - dis + i] == value) {
					return new Pair<>(size.getFirst() - i, size.getSecond() - dis + i);
				}
			}
		}
		return new Pair<>(0, 0);
	}

	public static <S, D> GenericTreeNode<D> deepCopy(
			GenericTreeNode<S> src, Function<S, D> copier) {
		if (src == null)
			return null;
		GenericTreeNode<D> dst = new GenericTreeNode<>();
		dst.setData(copier.apply(src.getData()));
		for (GenericTreeNode<S> kid : src.getChildren()) {
			dst.addChild(deepCopy(kid, copier));
		}
		return dst;
	}

	public static <T, S> Set<GenericTreeNode<T>> findAll(GenericTreeNode<T> root, S data, BiPredicate<T, S> comparator) {
		Set<GenericTreeNode<T>> results = new HashSet<>();
		findAllHelper(root, data, comparator, results);
		return results;
	}

	private static <T, S> void findAllHelper(GenericTreeNode<T> root, S data, BiPredicate<T, S> comparator, Set<GenericTreeNode<T>> results) {
		if (comparator.test(root.data, data)) {
			results.add(root);
		}
		for (GenericTreeNode<T> child : root.getChildren()) {
			findAllHelper(child, data, comparator, results);
		}
	}

	public static <T> GenericTreeNode<T> find(GenericTreeNode<T> root, GenericTreeNode<T> nodeToFind) {
		GenericTreeNode<T> returnNode = null;

		if (root != null) {
			returnNode = findHelper(root, nodeToFind);
		}

		return returnNode;
	}

	private static <T, S> GenericTreeNode<T> findHelper(GenericTreeNode<T> currentNode,
													 GenericTreeNode<T> nodeToFind) {
		GenericTreeNode<T> returnNode = null;
		int i = 0;

		if (currentNode.equals(nodeToFind)) {
			returnNode = currentNode;
		}

		else if (currentNode.hasChildren()) {
			i = 0;
			while (returnNode == null && i < currentNode.getNumberOfChildren()) {
				returnNode = findHelper(currentNode.getChildAt(i),
						nodeToFind);
				i++;
			}
		}

		return returnNode;
	}

	public static <T> Set<T> getAllData(GenericTreeNode<T> root) {
		Set<T> result = new HashSet<>();
		getAllDataHelper(root, result);
		return result;
	}

	private static <T> void getAllDataHelper(GenericTreeNode<T> root, Set<T> result) {
		if (root.data != null) {
			result.add(root.data);
		}
		for (GenericTreeNode<T> child : root.getChildren()) {
			getAllDataHelper(child, result);
		}
	}

}