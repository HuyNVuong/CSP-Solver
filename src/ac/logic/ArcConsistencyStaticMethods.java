package ac.logic;

import csp.BinaryPair;
import csp.Constraint;
import csp.Variable;

import java.util.HashSet;

public class ArcConsistencyStaticMethods {
    public static boolean revise(Variable xi, Variable xj, Constraint constraint) {
        var domainModified = false;
        var valuesToRemove = new HashSet<Integer>();
        for (var ai : xi.getDomain().getValues()) {
            for (var aj : xj.getDomain().getValues()) {
                if (constraint.binaryConstraintValueLookup.contains(new BinaryPair(ai, aj))) {
                    domainModified = true;
                    valuesToRemove.add(ai);
                }
            }
        }

//        System.out.println("Variable " + xi.getName());
//        System.out.println("Remove " + valuesToRemove.toString());
        xi.removeValues(valuesToRemove);
//        System.out.println("values left " + xi.getDomain().getValues());
        return domainModified;
    }
}
