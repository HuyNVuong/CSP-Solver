package ac;

import ac.logic.ArcConsistencyStaticMethods;
import csp.Constraint;

import java.util.List;

public class ArcConsistency_1 {
    public static void Solve(List<Constraint> constraints) {
        var isDomainModified = false;
        do {
            for (var constraint : constraints) {
                switch (constraint.arity) {
                    case 2:
                        var xi = constraint.getVariables().get(0);
                        var xj = constraint.getVariables().get(1);
                        var xiRevised = ArcConsistencyStaticMethods.revise(xi, xj, constraint);
                        var xjRevised = ArcConsistencyStaticMethods.revise(xj, xi, constraint);
                        isDomainModified = xiRevised || xjRevised;
                    default:
                        System.out.printf("Un-support for non-binary constraint of arity %d\n%n", constraint.arity);
                }
            }
        } while (isDomainModified);

    }
}
