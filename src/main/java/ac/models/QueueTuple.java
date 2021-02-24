package ac.models;

import csp.Constraint;
import csp.Variable;

public class QueueTuple {
    public Variable xi;
    public Variable xj;
    public Constraint constraint;

    public QueueTuple(Variable xi, Variable xj, Constraint constraint) {
        this.xi = xi;
        this.xj = xj;
        this.constraint = constraint;
    }
}
