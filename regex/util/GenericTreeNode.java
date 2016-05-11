package regex.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vivin Paliath on 1/26/2015.
 * site: http://vivin.net/2010/01/30/generic-n-ary-tree-in-java/
 */
public class GenericTreeNode<T> implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = 2298779910569353329L;
	public T data;
    private List<GenericTreeNode<T>> children;
    private GenericTreeNode<T> parent;

    public GenericTreeNode() {
        super();
        children = new ArrayList<>();
        parent = null;
    }

    public GenericTreeNode(T data) {
        this();
        setData(data);
    }

    public List<GenericTreeNode<T>> getChildren() {
        return this.children;
    }

    public int getNumberOfChildren() {
        return getChildren().size();
    }

    public boolean hasChildren() {
        return (getNumberOfChildren() > 0);
    }

    public void setChildren(List<GenericTreeNode<T>> children) {
        this.children = children;
        for (GenericTreeNode<T> child : this.children) {
        	child.parent = this;
        }
    }

    public void addChild(GenericTreeNode<T> child) {
    	child.parent = this;
        this.children.add(child);
    }

    public void addChildAt(int index, GenericTreeNode<T> child) throws IndexOutOfBoundsException {
        child.parent = this;
    	children.add(index, child);
    }

    public void removeChildren() {
        this.children = new ArrayList<GenericTreeNode<T>>();
    }

    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }

    public GenericTreeNode<T> getChildAt(int index) throws IndexOutOfBoundsException {
        return children.get(index);
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toString() {
        StringBuilder buff = new StringBuilder();
        this.toStringHelper(buff, 0);
        return buff.toString();
    }

    private void toStringHelper(StringBuilder buff, int indent) {
        if (this == null || this.getData() == null)
            return;
        for (int i = 0; i < indent; ++i) {
            buff.append("  ");
        }
        if (this.getData()!= null)
            buff.append(this.getData().toString());
        buff.append("\n");
        for (GenericTreeNode<T> child : this.getChildren()) {
            child.toStringHelper(buff, indent + 1);
        }
    }

    /**
     * need to be reconsideration
     * @param node
     * @return
     */
    public boolean equals(GenericTreeNode<T> node) {
        return node.getData().equals(getData());
    }

    public int hashCode() {
        return getData().hashCode();
    }

    public String toStringVerbose() {
        String stringRepresentation = getData().toString() + ":[";

        for (GenericTreeNode<T> node : getChildren()) {
            stringRepresentation += node.getData().toString() + ", ";
        }

        /**
         * Pattern.DOTALL causes ^ and $ to match. Otherwise it won't. It's retarded.
         */
        Pattern pattern = Pattern.compile(", $", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(stringRepresentation);

        stringRepresentation = matcher.replaceFirst("");
        stringRepresentation += "]";

        return stringRepresentation;
    }

	public GenericTreeNode<T> getParent() {
		return this.parent;
	}

}