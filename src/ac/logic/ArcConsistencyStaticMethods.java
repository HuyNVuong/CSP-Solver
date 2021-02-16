package ac.logic;

import csp.Constraint;

import java.util.Arrays;

public class ArcConsistencyStaticMethods {
    public static boolean revise(Constraint constraint) {
        var variables = constraint.getVariables();
        var xi = variables.get(0);
        var xj = variables.get(1);
        var xjDomain = xj.getDomain();
        xjDomain.setValues(Arrays.stream(xi.getDomain().getValues())
                .filter(ai -> !xjDomain.contains(constraint.binaryConstraintValueLookup.getOrDefault(ai, -1)))
                .toArray());
        return false;
    }
}
