package ac.logic;

import csp.BinaryPair;
import csp.Constraint;
import csp.Variable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ArcConsistencyStaticMethods {
    public static boolean revise(Variable xi, Variable xj, Constraint constraint) {
        var domainModified = false;
        System.out.println(Arrays.toString(constraint.binaryConstraintValueLookup.toArray()));
        var valuesToRemove = new HashSet<Integer>();
        for (var ai : xi.getDomain().getValues()) {
            var valuesHaveNoRelation = !supported(ai, xj.getDomain().getValues(), constraint);
            if (valuesHaveNoRelation) {
                domainModified = true;
                valuesToRemove.add(ai);
            }
        }
        
        xi.removeValues(valuesToRemove);
        return domainModified;
    }

    private static boolean supported(int ai, Set<Integer> xjDomain, Constraint constraint) {
        return switch (constraint.definition) {
            case "conflicts" -> xjDomain.stream().noneMatch(aj -> constraint.binaryConstraintValueLookup.contains(new BinaryPair(ai, aj)));
            case "supports"  -> xjDomain.stream().anyMatch(aj -> constraint.binaryConstraintValueLookup.contains(new BinaryPair(ai, aj)));
            default          -> false;
        };

    }
}
