package nc;

import csp.Variable;

import java.util.stream.Collectors;

public class NodeConsistency {
    public static void enforceNodeConsistency(Variable v) {
        if (v.hasUnaryConstraint) {
            var constraints = v.unaryConstraints;
            for (var constraint : constraints) {
                v.removeValues(v.getDomain().getCurrentDomain().stream().filter(val -> {
                    if (constraint.isIntension())
                        return constraint.intensionEvaluator.apply(new int[]{val}) != 0;
                    return switch (constraint.definition) {
                        case "conflicts" -> constraint.unaryConstraintValueLookup.contains(val);
                        case "supports" -> !constraint.unaryConstraintValueLookup.contains(val);
                        default -> false;
                    };
                }).collect(Collectors.toSet()));
            }
        }
    }
}
