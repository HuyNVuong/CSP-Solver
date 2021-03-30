package bt;

import ac.models.VVP;
import bt.models.BtResponse;
import csp.Variable;
import nc.NodeConsistency;

import java.util.*;
import java.util.stream.Collectors;

public class ConflictedBackJumping {
    static long cc;
    static long nv;

    public static BtResponse search(List<Variable> variables, boolean solveAllSolutions) {
        int i = 0;
        int n = variables.size();
        cc = 0;
        nv = 0;
        long bt = 0;

        variables.forEach(NodeConsistency::enforceNodeConsistency);

        var D = variables.stream().map(v -> {
            var domain = v.getDomain();
            return new HashSet<>(domain.getCurrentDomain());
        }).collect(Collectors.toList());
        var J = variables.stream()
                .map(v -> new TreeMap<Integer, Variable>())
                .collect(Collectors.toCollection(ArrayList::new));
        var solutions = new ArrayList<ArrayList<Integer>>();
        List<VVP> exploredVVPs = new ArrayList<>();
        boolean hasSolution = false;
        var cbf = new boolean[n];

        while(0 <= i && (i < n || solveAllSolutions)) {
            Integer xi;
            if (i == variables.size()) {
                i--;
                xi = null;
            } else {
                var variableAtLevel = variables.get(i);
                xi = selectValue(exploredVVPs, D.get(i), i,  variableAtLevel, J.get(i));
            }
            if (xi == null) {
                bt++;
                int iPrev = i;
                if (cbf[i]) {
                    cbf[i] = false;
                    i--;
                } else {
                    var lastConflictEntry = J.get(i).lastEntry();
                    if (lastConflictEntry == null)
                        break;
                    i = lastConflictEntry.getKey();
                }
                if (i == -1)
                    break;
                for (var pairs : J.get(iPrev).entrySet()) {
                    J.get(i).putIfAbsent(pairs.getKey(), pairs.getValue());
                }

                J.get(i).remove(i);

                if (exploredVVPs.size() > 0) {
                    exploredVVPs = exploredVVPs.subList(0, i);
                }
            } else {
                i++;
                exploredVVPs.add(new VVP(variables.get(i - 1), xi));
                if (i < variables.size()) {
                    D.set(i, new HashSet<>(variables.get(i).getDomain().getCurrentDomain()));
                    J.set(i, new TreeMap<>());
                }
                if (i == n) {
                    Arrays.fill(cbf, true);
                    solutions.add(new ArrayList<>(exploredVVPs.stream().map(vvp -> vvp.value).collect(Collectors.toList())));
                }
            }
        }

        if (i < 0 && !solveAllSolutions) {
            return new BtResponse(new ArrayList<>(), cc, nv, bt);
        }

        return new BtResponse(solutions, cc, nv, bt);
    }

    private static Integer selectValue(
            List<VVP> previousVVPs, HashSet<Integer> currentDomain,
            int currentIndex, Variable currentVariable, TreeMap<Integer, Variable> Ji
    ) {
        Integer a = null;
        var allConsistent = true;
        var valuesToRemoveFromDomain = new HashSet<Integer>();
        for (var d : currentDomain) {
            nv++;
            int k = 0;
            boolean hasConsistent = true;
            valuesToRemoveFromDomain.add(d);
            while (k < previousVVPs.size() && hasConsistent) {
                var vvp = previousVVPs.get(k);
                cc++;

                if (vvp.v.shareManyConstraintsWithNeighbor(currentVariable.getName())) {
                    var allConstraints = vvp.v.getAllConstraintForPairs(currentVariable.getName());

                    hasConsistent = allConstraints.stream().allMatch(subConstraint -> {
                        var isReversed = !subConstraint
                                .getVariables().get(1).getName()
                                .equals(currentVariable.getName());
                        return Helper.binaryConsistent(vvp.value, d, subConstraint, isReversed);
                    });
                } else {
                    var constraint = vvp.v.getSharedConstraint(currentVariable.getName());
                    if (constraint == null)
                        break;
                    var isReversed = !constraint.getVariables().get(1).getName().equals(currentVariable.getName());
                    hasConsistent = Helper.binaryConsistent(vvp.value, d, constraint, isReversed);
                }

                if (hasConsistent) {
                    k++;
                } else {
                    Ji.putIfAbsent(k, vvp.v);
                }
            }
            allConsistent = hasConsistent;

            if (allConsistent) {
                a = d;
                break;
            }
        }

        valuesToRemoveFromDomain.forEach(currentDomain::remove);

        return a;
    }
}
