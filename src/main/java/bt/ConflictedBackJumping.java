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
                .map(v -> new HashSet<Integer>())
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
                if (cbf[i] || J.get(i).size() == 0) {
                    cbf[i] = false;
                    i--;
                } else {
                    i = Collections.max(J.get(i));
                }
                if (i == -1)
                    break;
                for (var k : J.get(iPrev)) {
                    J.get(i).add(k);
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
                    J.set(i, new HashSet<>());
                }
                if (i == n) {
                    Arrays.fill(cbf, true);
//                    System.out.printf("Solution %d\n", solutions.size());
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
            int currentIndex, Variable currentVariable, HashSet<Integer> Ji
    ) {
        Integer a = null;
        var allConsistent = true;
        var valuesToRemoveFromDomain = new HashSet<Integer>();
        for (var d : currentDomain) {
            nv++;
            int k = 0;
            boolean hasConsistent = true;
            valuesToRemoveFromDomain.add(d);
            if (previousVVPs.isEmpty()) {
                a = d;
                break;
            }
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
                    if (constraint == null) {
                        k++;
                        continue;
                    }
                    var isReversed = !constraint.getVariables().get(1).getName().equals(currentVariable.getName());
                    hasConsistent = Helper.binaryConsistent(vvp.value, d, constraint, isReversed);
                }

                if (hasConsistent) {
                    k++;
                } else {
                    Ji.add(k);
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
