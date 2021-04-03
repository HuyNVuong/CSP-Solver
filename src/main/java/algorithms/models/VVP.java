package algorithms.models;


import csp.Variable;

import java.util.Objects;

public class VVP {
    public Variable v;
    public int value;

    public VVP(Variable _v, int _value) {
        v = _v;
        value = _value;
    }

    @Override
    public String toString() {
        return String.format("(%s,%d)", v.getName(), value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VVP vvp = (VVP) o;
        return value == vvp.value && v.getName().equals(vvp.v.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(v.getName(), value);
    }
}
