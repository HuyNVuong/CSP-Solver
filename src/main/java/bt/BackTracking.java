package bt;

import csp.BinaryPair;
import csp.Constraint;
import csp.Variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackTracking {
    public static ArrayList<ArrayList<Integer>> bcssp(List<Variable> variables) {
        int i = 0;
        var D = variables.stream().map(v -> {
            var domain = v.getDomain();
            return new HashSet<>(domain.getCurrentDomain());
        }).collect(Collectors.toList());
        Integer exploringNodeValue = null;
        var paths = new ArrayList<ArrayList<Integer>>();
        paths.add(new ArrayList<>());
        int pathId = 0;
        List<Integer> exploredNodeValues = new ArrayList<>();
        while (0 <= i && i < variables.size()) {
            var constraintToEvaluate = i == 0 ? null : variables.get(i).getSharedConstraint(variables.get(i - 1).getName());
            var xi = selectValue(exploredNodeValues, D.get(i), constraintToEvaluate);
            if (xi == null) {
                if (exploredNodeValues.size() > 0)
                    exploredNodeValues.remove(exploredNodeValues.size() - 1);
                paths.add(new ArrayList<>(paths.get(paths.size() - 1)));
                pathId++;
                if (paths.get(pathId).size() > 0)
                    paths.get(pathId).remove(paths.get(pathId).size() - 1);
                i--;
            } else {
                i++;
                paths.get(pathId).add(xi);
                exploredNodeValues.add(xi);
                if (i < variables.size())
                    D.set(i, new HashSet<>(variables.get(i).getDomain().getCurrentDomain()));
            }
        }

        if (i < 0) {
            return null;
        }

        var foo = paths.stream().filter(p -> p.size() == variables.size()).count();
        return paths;
    }

    private static Integer selectValue(
            List<Integer> previousDomainValues, Set<Integer> currentDomain,
            Constraint constraint
    ) {
        var valuesToRemoveFromDomain = new HashSet<Integer>();
        Integer a = null;
        for (var d : currentDomain) {
            valuesToRemoveFromDomain.add(d);
            if (previousDomainValues.isEmpty()) {
                a = d;
                break;
            }
            if (constraint == null) {
                break;
            }
            var lastSeenValue = previousDomainValues.get(previousDomainValues.size() - 1);
            var isConsistent = !previousDomainValues.contains(d)
                    && ((constraint.definition.equals("supports") && constraint.binaryConstraintValueLookup.contains(new BinaryPair(lastSeenValue, d)))
                    || (constraint.definition.equals("conflicts") && !constraint.binaryConstraintValueLookup.contains(new BinaryPair(lastSeenValue, d))));
            if (isConsistent) {
                a = d;
                break;
            }
        }
        valuesToRemoveFromDomain.forEach(currentDomain::remove);
        return a;
    }
}
