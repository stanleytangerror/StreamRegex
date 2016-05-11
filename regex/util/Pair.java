package regex.util;

import java.io.Serializable;

/**
 * Created by tshun_000 on 1/26/2015.
 */
public class Pair<T1, T2> implements Serializable {
    protected T1 first;
    protected T2 second;
    private static final long serialVersionUID = 1360822168806852921L;

    public Pair() {
    }

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Pair<T1, T2> pair) {
        this(pair.getFirst(), pair.getSecond());
    }

    public T1 getFirst() {
        return this.first;
    }

    public T2 getSecond() {
        return this.second;
    }

    public void setFirst(T1 o) {
        this.first = o;
    }

    public void setSecond(T2 o) {
        this.second = o;
    }

    public String toString() {
        return "(" + this.first + ", " + this.second + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Pair))
            return false;
        Pair p = (Pair) o;
        if(!this.first.equals(p.first))
            return false;
        if(!this.second.equals(p.second))
            return false;
        return true;

    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + this.first.hashCode();
        hash = hash * 31 + this.second.hashCode();
        return hash;
    }

}
