package ac.models;

import csp.Constraint;
import csp.Variable;

public class QueueTuple {
    public Variable xi;
    public Variable xj;
    public Constraint constraint;
    public boolean reversed;

    public QueueTuple(Variable xi, Constraint constraint) {
        this.xi = xi;
        this.xj = null;
        this.constraint = constraint;
        reversed = false;
    }

    public QueueTuple(Variable xi, Variable xj, Constraint constraint) {
        this.xi = xi;
        this.xj = xj;
        this.constraint = constraint;
        reversed = !xi.getName().equals(constraint.getVariables().get(0).getName());
    }
}
