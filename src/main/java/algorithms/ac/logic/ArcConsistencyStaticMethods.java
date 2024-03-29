package algorithms.ac.logic;

import algorithms.models.ReviseResponse;
import csp.BinaryPair;
import csp.Constraint;
import csp.Variable;
import nc.NodeConsistency;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ArcConsistencyStaticMethods {
    static int cc;

    public static ReviseResponse revise(Variable xi, Variable xj, Constraint constraint, boolean pairReversed) {
        var domainModified = false;
        cc = 0;
        Set<Integer> valuesToRemove;
        if (xj == null) {
            NodeConsistency.enforceNodeConsistency(xi);
            valuesToRemove = new HashSet<>();
            domainModified = true;
        } else {
            valuesToRemove = new HashSet<>();
            for (var ai : xi.getDomain().getCurrentDomain()) {
                var valuesHaveNoRelation = !binarySupported(ai, xj.getDomain().getCurrentDomain(), constraint, pairReversed);
                if (valuesHaveNoRelation) {
                    domainModified = true;
                    valuesToRemove.add(ai);
                }
            }
        }

        xi.removeValues(valuesToRemove);
        return new ReviseResponse(cc, domainModified, valuesToRemove.size());
    }

    public static boolean binarySupported(int ai, Set<Integer> xjDomain, Constraint constraint, boolean pairReversed) {
        if (constraint.isIntension()) {
            return xjDomain.stream().anyMatch(aj -> {
                cc++;
                var pair = pairReversed
                        ? new int[]{aj, ai}
                        : new int[]{ai, aj};
                return constraint.intensionEvaluator.apply(pair) == 0;
            });
        }

        Function<Set<BinaryPair>, Boolean> extensionValidator = lookup -> {
            var pairLookedUp = xjDomain.stream().filter(aj -> lookup.contains(new BinaryPair(ai, aj))).count();
            cc += pairLookedUp;
            return switch (constraint.definition) {
                case "conflicts" -> xjDomain.stream().anyMatch(aj -> !lookup.contains(new BinaryPair(ai, aj)));
                case "supports" -> xjDomain.stream().anyMatch(aj -> lookup.contains(new BinaryPair(ai, aj)));
                default -> false;
            };
        };

        return pairReversed
                ? extensionValidator.apply(constraint.reversedBinaryConstraintValueLookup)
                : extensionValidator.apply(constraint.binaryConstraintValueLookup);
    }
}
