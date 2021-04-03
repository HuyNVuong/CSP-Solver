package algorithms.search;

import algorithms.models.VVP;
import algorithms.models.SearchResponse;
import csp.Variable;
import nc.NodeConsistency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackTracking {
    static long cc;
    static long nv;

    public static SearchResponse search(List<Variable> variables, boolean solveAllSolutions) {
        int i = 0;
        cc = 0;
        nv = 0;
        long bt = 0;

        variables.forEach(NodeConsistency::enforceNodeConsistency);

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
             return new SearchResponse(new ArrayList<>(), cc, nv, bt);
        }

        var solutions = paths.stream()
                .filter(p -> p.size() == variables.size())
                .collect(Collectors.toList());

        return new SearchResponse(solutions, cc, nv, bt);
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
                if (vvp.v.shareManyConstraintsWithNeighbor(currentVariable.getName())) {
                    var allConstraints = vvp.v.getAllConstraintForPairs(currentVariable.getName());

                    return allConstraints.stream().allMatch(subConstraint -> {
                        cc++;
                        var isReversed = !subConstraint
                                .getVariables().get(1).getName()
                                .equals(currentVariable.getName());
                        return Helper.binaryConsistent(vvp.value, d, subConstraint, isReversed);
                    });
                }
                var constraint = vvp.v.getSharedConstraint(currentVariable.getName());
                if (constraint == null)
                    return true;
                cc++;
                var isReversed = !constraint.getVariables().get(1).getName().equals(currentVariable.getName());
                return Helper.binaryConsistent(vvp.value, d, constraint, isReversed);
            });
            if (isConsistent) {
                a = d;
                break;
            }
        }
        valuesToRemoveFromDomain.forEach(currentDomain::remove);
        return a;
    }
}
