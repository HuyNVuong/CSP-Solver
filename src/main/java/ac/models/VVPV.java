package ac.models;

import csp.Variable;

import java.util.Objects;

public class VVPV {
    public Variable v1;
    public Variable v2;
    public int a1;

    public VVPV(Variable _v1, int _a1, Variable _v2) {
        v1 = _v1;
        v2 = _v2;
        a1 = _a1;
    }

    @Override
    public String toString() {
        return String.format("(%s,%d,%s)", v1.getName(), a1, v2.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VVPV vvp = (VVPV) o;
        return a1 == vvp.a1 && v1.getName().equals(vvp.v1.getName())
                && v2.getName().equals(vvp.v2.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(v1.getName(), a1, v2.getName());
    }
}
