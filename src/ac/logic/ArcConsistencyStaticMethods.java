package ac.logic;

import csp.BinaryPair;
import csp.Constraint;
import csp.Variable;

import java.util.Arrays;
import java.util.HashSet;

public class ArcConsistencyStaticMethods {
    public static boolean revise(Variable xi, Variable xj, Constraint constraint) {
        var domainModified = false;
        System.out.println(Arrays.toString(constraint.binaryConstraintValueLookup.toArray()));
        var valuesToRemove = new HashSet<Integer>();
        for (var ai : xi.getDomain().getValues()) {
            var valuesHaveNoRelation = xj.getDomain().getValues()
                    .stream()
                    .noneMatch(aj -> constraint.binaryConstraintValueLookup.contains(new BinaryPair(ai, aj)));
            if (valuesHaveNoRelation) {
                domainModified = true;
                valuesToRemove.add(ai);
            }
        }
        
        xi.removeValues(valuesToRemove);
        return domainModified;
    }
}
