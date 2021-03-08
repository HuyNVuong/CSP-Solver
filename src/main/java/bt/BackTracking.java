package bt;

import ac.models.VVP;
import bt.models.BtResponse;
import csp.BinaryPair;
import csp.Constraint;
import csp.Variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackTracking {
    static long cc;
    static long nv;

    public static BtResponse vanillaSearch(List<Variable> variables, boolean solveAllSolutions) {
        int i = 0;
        cc = 0;
        nv = 0;
        long bt = 0;

        var D = variables.stream().map(v -> {
            var domain = v.getDomain();
            return new HashSet<>(domain.getCurrentDomain());
        }).collect(Collectors.toList());
        var paths = new ArrayList<ArrayList<Integer>>();
        paths.add(new ArrayList<>());
        int pathId = 0;
        var exploredVVPs = new ArrayList<VVP>();
        while (0 <= i && (i < variables.size() || solveAllSolutions)) {
            Integer xi;
            if (i == variables.size()) {
                xi = null;
            } else {
                var variableAtLevel = variables.get(i);
                xi = selectValue(exploredVVPs, D.get(i), variableAtLevel);
            }
            if (xi == null) {
                bt++;
                if (exploredVVPs.size() > 0) {
                    int lastIndex = exploredVVPs.size() - 1;
                    exploredVVPs.remove(lastIndex);
                }

                if (paths.get(pathId).size() > 0) {
                    paths.add(new ArrayList<>(paths.get(paths.size() - 1)));
                    pathId++;
                    paths.get(pathId).remove(paths.get(pathId).size() - 1);
                }
                i--;
            } else {
                i++;
                paths.get(pathId).add(xi);
                exploredVVPs.add(new VVP(variables.get(i - 1), xi));
                if (i < variables.size()) {
                    D.set(i, new HashSet<>(variables.get(i).getDomain().getCurrentDomain()));
                }
            }
        }

        if (i < 0 && !solveAllSolutions) {
             return new BtResponse(new ArrayList<>(), cc, nv, bt);
        }

        var solutions = paths.stream()
                .filter(p -> p.size() == variables.size())
                .collect(Collectors.toList());

        return new BtResponse(solutions, cc, nv, bt);
    }

    private static Integer selectValue(
            List<VVP> previousVVPs, Set<Integer> currentDomain,
            Variable currentVariable
    ) {
        var previousDomainValues = previousVVPs.stream().mapToInt(o -> o.value).boxed().collect(Collectors.toList());
        var valuesToRemoveFromDomain = new HashSet<Integer>();
        Integer a = null;
        var currentDomainsList = new ArrayList<>(currentDomain);
        for (int d : currentDomainsList) {
            nv++;
            valuesToRemoveFromDomain.add(d);
            if (previousDomainValues.isEmpty()) {
                a = d;
                break;
            }
            var isConsistent = previousVVPs.stream().allMatch(vvp -> {
                var constraint = vvp.v.getSharedConstraint(currentVariable.getName());
                cc++;
                if (constraint == null && vvp.v.hasUnaryConstraint) {
                    return unaryConsistent(d, vvp.v.unaryConstraint);
                }

                return binaryConsistent(vvp.value, d, constraint);
            });
            if (isConsistent) {
                a = d;
                break;
            }
        }
        valuesToRemoveFromDomain.forEach(currentDomain::remove);
        return a;
    }

    public static boolean unaryConsistent(int d, Constraint constraint) {
        if (constraint.isIntension()) {
            return constraint.intensionEvaluator.apply(new int[]{d}) == 0;
        }

        return (constraint.unaryConstraintValueLookup.contains(d) && constraint.definition.equals("supports"))
                || (!constraint.unaryConstraintValueLookup.contains(d) && constraint.definition.equals("conflicts"));
    }

    public static boolean binaryConsistent(int di, int dj, Constraint constraint) {
        if (constraint == null) {
            return true;
        }
        if (constraint.isIntension()) {
            return constraint.intensionEvaluator.apply(new int[]{di, dj}) == 0;
        }

        return ((constraint.definition.equals("supports")
                    && constraint.binaryConstraintValueLookup.contains(new BinaryPair(di, dj)))
                || (constraint.definition.equals("conflicts")
                    && !constraint.binaryConstraintValueLookup.contains(new BinaryPair(di, dj))));
    }
}
