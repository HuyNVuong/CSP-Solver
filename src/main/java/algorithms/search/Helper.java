package algorithms.search;

import csp.BinaryPair;
import csp.Constraint;

public class Helper {
    public static boolean binaryConsistent(int di, int dj, Constraint constraint, boolean isReversed) {
        if (constraint.isIntension()) {
            var pair = isReversed ? new int[]{dj, di} : new int[]{di, dj};
            return constraint.intensionEvaluator.apply(pair) == 0;
        }

        var R = isReversed ? constraint.reversedBinaryConstraintValueLookup : constraint.binaryConstraintValueLookup;

        return switch (constraint.definition) {
            case "conflicts" -> !R.contains(new BinaryPair(di, dj));
            case "supports" -> R.contains(new BinaryPair(di, dj));
            default -> false;
        };
    }
}
