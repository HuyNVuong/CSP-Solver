package algorithms.search;

import algorithms.models.SearchResponse;
import csp.Variable;
import nc.NodeConsistency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ForwardChecking {
    static long cc;
    static long nv;

    public static SearchResponse search(List<Variable> variables, boolean solveAllSolutions) {
        variables.forEach(NodeConsistency::enforceNodeConsistency);

        var D = variables.stream().map(v -> {
            var domain = v.getDomain();
            return new HashSet<>(domain.getCurrentDomain());
        }).collect(Collectors.toList());

        var solutions = new ArrayList<ArrayList<Integer>>();

        int i = 0, n = variables.size();
        long bt = 0;

        while(0 <= i && (i < n || solveAllSolutions)) {
            var xi = selectValue(n, D, i, variables.get(i), variables);
            if (xi == null) {
                i--;
                D.set(i, new HashSet<>(variables.get(i).getDomain().getCurrentDomain()));
            } else {
                i++;
            }
        }

        return new SearchResponse(solutions, cc, nv, bt);
    }

    private static Integer selectValue(
            int n, List<HashSet<Integer>> D, int currentIndex,
            Variable currentVariable, List<Variable> variables
    ) {
        var currentDomain = D.get(currentIndex);
        for (var di : currentDomain) {
            for (int k = currentIndex + 1; k < n; k++) {
                var valuesToRemoveFromDomain = new HashSet<Integer>();
                for (var dj : D.get(k)) {
                    var constraint = currentVariable.getSharedConstraint(variables.get(k).getName());
                    if (constraint == null) {
                        continue;
                    }
                    var isReversed = !constraint.getVariables().get(1).getName().equals(currentVariable.getName());
                    var isConsistent = Helper.binaryConsistent(di, dj, constraint, isReversed);
                    if (!isConsistent) {
                        valuesToRemoveFromDomain.add(dj);
                    }
                }
                D.get(k).removeAll(valuesToRemoveFromDomain);
                if (D.get(k).isEmpty()) {
                    D.set(k, new HashSet<>(variables.get(k).getDomain().getCurrentDomain()));
                } else {
                    return di;
                }
            }
        }

        return null;
    }
}
