package AlgDat.Graphs;

/**
 * Used to store arbitrary data in one unit. Especially useful in lambda's to
 * carry multiple data objects simultaneously in a pipeline, i.e. java streams for instance.
 * @param <A> Any data.
 * @param <B> Any data
 */

class Tuple<A,B>{
    private final A a;
    private final B b;

    Tuple(A a, B b){
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;

        if (!getA().equals(tuple.getA())) return false;
        return getB().equals(tuple.getB());
    }

    @Override
    public int hashCode() {
        int result = getA().hashCode();
        result = 31 * result + getB().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(" + getA() + "," + getB() + ")";
    }
}
