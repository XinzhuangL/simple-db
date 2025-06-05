package org.lxz.common;

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

public class Pair<F, S> {
    @SerializedName(value = "first")
    public F first;
    @SerializedName(value = "second")
    public S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> create(F first, S second) {
        return new Pair<F, S>(first, second);
    }

    @Override
    /*
     * A pair is equal if both parts are equal().
     */
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<F, S> other = (Pair<F, S>) o;

        // compare first
        if (this.first == null) {
            if (other.first != null) {
                return false;
            }
        } else {
            if (! this.first.equals(other.first)) {
                return false;
            }
        }

        // compare second
        if (this.second == null) {
            return other.second == null;
        } else {
            return this.second.equals(other.second);
        }
    }

    public Pair<S, F> inverse() {
        return Pair.create(second, first);
    }

    @Override
    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    @Override
    public String toString() {
        return first.toString() + ":" + second.toString();
    }

    public static <K, V extends Comparable<? super V>> Comparator<Pair<K, V>> comparingBySecond() {
        return Comparator.comparing(c -> c.second);
    }
}
