package ac;

import ac.logic.ArcConsistencyStaticMethods;
import csp.Constraint;

import java.util.List;

public class ArcConsistency_1 {
    public static boolean solve(List<Constraint> constraints) {
        var isDomainModified = false;
        do {
            for (var constraint : constraints) {
                switch (constraint.arity) {
                    case 2:
                        var xi = constraint.getVariables().get(0);
                        var xj = constraint.getVariables().get(1);
                        var xiRevised = ArcConsistencyStaticMethods.revise(xi, xj, constraint);
                        if (xiRevised && xi.getDomain().getValues().isEmpty()) {
                            System.out.printf("Sub network %s found to be not arc consistent\n", constraint.name);
                            return false;
                        }
                        var xjRevised = ArcConsistencyStaticMethods.revise(xj, xi, constraint);
                        if (xjRevised && xj.getDomain().getValues().isEmpty()) {
                            System.out.printf("Sub network %s found to be not arc consistent\n", constraint.name);
                            return false;
                        }
                        isDomainModified = xiRevised || xjRevised;
                    default:
                        System.out.printf("Un-support for non-binary constraint of arity %d\n%n", constraint.arity);
                }
            }
        } while (isDomainModified);

        return true;
    }
}
