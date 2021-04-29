package algorithms.search;

import algorithms.models.SearchResponse;
import algorithms.models.VVP;
import csp.Variable;
import nc.NodeConsistency;

import java.util.*;
import java.util.stream.Collectors;

public class ForwardCheckingWithConflictDirectedBackJumping {
    static long cc;
    static long nv;

    public static SearchResponse search(List<Variable> variables, boolean solveAllSolutions) {
        return search(variables, solveAllSolutions, false);
    }

    public static SearchResponse searchWithDynamicOrdering(List<Variable> variables, boolean solveAllSolutions) {
        return search(variables, solveAllSolutions, true);
    }

    public static SearchResponse search(List<Variable> variables, boolean solveAllSolutions, boolean useDynamicOrdering) {
        cc = 0;
        nv = 0;
        long bt = 0;
        variables.forEach(NodeConsistency::enforceNodeConsistency);
        var solutions = new ArrayList<ArrayList<Integer>>();

        var visited = new ArrayList<Integer>();

        var variableLookup = variables.stream().collect(Collectors.toMap(Variable::getName, v -> v));

        var D = variables.stream().map(v -> {
            var domain = v.getDomain();
            return new HashSet<>(domain.getCurrentDomain());
        }).collect(Collectors.toList());
        var J = variables.stream()
                .map(v -> new HashSet<Integer>())
                .collect(Collectors.toCollection(ArrayList::new));

        List<String> visitedVariables = new ArrayList<>();
        int i = 0, n = variables.size();
        List<VVP> exploredVVPs = new ArrayList<>();
        var cbf = new boolean[n];

        var lastInstantiatedState = new HashMap<String, HashMap<String, HashSet<Integer>>>();
        for (int j = 0; j < n; j++) {
            lastInstantiatedState.put(variables.get(j).getName(), new HashMap<>(variables.stream()
                    .collect(Collectors.toMap(
                            Variable::getName,
                            v -> new HashSet<>(v.getDomain().getCurrentDomain())
                    ))
            ));
        }
        lastInstantiatedState.put("completed", new HashMap<>(variables.stream()
                .collect(Collectors.toMap(
                        Variable::getName,
                        v -> new HashSet<>(v.getDomain().getCurrentDomain())
                ))
        ));

        while(0 <= i && (i < n || solveAllSolutions)) {
            Integer xi;
            if (i == variables.size()) {
                i--;
                xi = null;
            } else {
                xi = selectValue(n, D, i, variables.get(i), variables, exploredVVPs, J);
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
                for (int k = i + 1; k < n; k++)
                    if (i >= 0)
                        D.set(k, new HashSet<>(lastInstantiatedState
                                .get(variables.get(i).getName())
                                .get(variables.get(k).getName())));
                if (exploredVVPs.size() > 0) {
                    exploredVVPs = exploredVVPs.subList(0, i);
                    visitedVariables = visitedVariables.subList(0, i);
                }
            } else {
                i++;
                nv++;
                exploredVVPs.add(new VVP(variables.get(i - 1), xi));
                visitedVariables.add(variables.get(i - 1).getName());
                if (i < variables.size()) {
                    D.set(i, new HashSet<>(variables.get(i).getDomain().getCurrentDomain()));
                    J.set(i, new HashSet<>());
                }
                var key = i == n ? "completed" : variables.get(i).getName();
                lastInstantiatedState.put(key, new HashMap<>());
                for (int j = 0; j < n; j++) {
                    lastInstantiatedState.get(key).put(
                            variables.get(j).getName(),
                            new HashSet<>(D.get(j)));
                }
                if (i == n) {
                    Arrays.fill(cbf, true);
//                    System.out.printf("Solution %d\n", solutions.size());
                    solutions.add(new ArrayList<>(exploredVVPs.stream().map(vvp -> vvp.value).collect(Collectors.toList())));
                    if (solutions.size() > 0 && solutions.size() % 1000 == 0) {
                        System.out.printf("Found %d solutions\n", solutions.size());
                    }
                }
            }

        }


        return new SearchResponse(solutions, cc, nv, bt, visitedVariables);
    }

    private static Integer selectValue(
            int n, List<HashSet<Integer>> D, int currentIndex,
            Variable currentVariable, List<Variable> variables,
            List<VVP> previousVVPs, ArrayList<HashSet<Integer>> J
    ) {
        var currentDomain = D.get(currentIndex);
        var valuesToRemoveFromCurrentDomain = new HashSet<Integer>();
        var valuesToRemoveFromFutureDomain = new HashMap<Integer, HashSet<Integer>>();
        Integer instantiated = null;
        for (var a : currentDomain) {
            valuesToRemoveFromCurrentDomain.add(a);
            boolean hasConsistent = true;
            if (!previousVVPs.isEmpty()) {
                int k = 0;
                while (k < previousVVPs.size() && hasConsistent) {
                    var vvp = previousVVPs.get(k);

                    if (vvp.v.shareManyConstraintsWithNeighbor(currentVariable.getName())) {
                        var allConstraints = vvp.v.getAllConstraintForPairs(currentVariable.getName());

                        hasConsistent = allConstraints.stream().allMatch(subConstraint -> {
                            var isReversed = !subConstraint
                                    .getVariables().get(1).getName()
                                    .equals(currentVariable.getName());
                            return Helper.binaryConsistent(vvp.value, a, subConstraint, isReversed);
                        });
                    } else {
                        var constraint = vvp.v.getSharedConstraint(currentVariable.getName());
                        if (constraint == null) {
                            k++;
                            continue;
                        }
                        var isReversed = !constraint.getVariables().get(1).getName().equals(currentVariable.getName());
                        hasConsistent = Helper.binaryConsistent(vvp.value, a, constraint, isReversed);
                    }

                    if (hasConsistent) {
                        k++;
                    } else {
                        J.get(currentIndex).add(k);
                    }
                }
            }
            if (!hasConsistent)
                continue;
            hasConsistent = currentIndex + 1 == n;
            var copy = valuesToRemoveFromFutureDomain.entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey,
                            entry -> entry.getValue() == null ? new HashSet<Integer>() : new HashSet<>(entry.getValue())));
            for (int k = currentIndex + 1; k < n; k++) {
                for (var b : D.get(k)) {
                    var v = variables.get(k);
                    boolean isConsistent;
                    cc++;
                    if (v.shareManyConstraintsWithNeighbor(currentVariable.getName())) {
                        var allConstraints = v.getAllConstraintForPairs(currentVariable.getName());

                        isConsistent = allConstraints.stream().allMatch(subConstraint -> {
                            var isReversed = !subConstraint
                                    .getVariables().get(1).getName()
                                    .equals(currentVariable.getName());
                            return Helper.binaryConsistent(b, a, subConstraint, isReversed);
                        });
                    } else {
                        var constraint = currentVariable.getSharedConstraint(variables.get(k).getName());
                        if (constraint == null) {
                            hasConsistent = true;
                            break;
                        }

                        var isReversed = !constraint.getVariables().get(1).getName().equals(currentVariable.getName());
                        isConsistent = Helper.binaryConsistent(a, b, constraint, isReversed);
                    }

                    if (!isConsistent) {
                        valuesToRemoveFromFutureDomain.putIfAbsent(k, new HashSet<>());
                        valuesToRemoveFromFutureDomain.get(k).add(b);
                    } else {
                        hasConsistent = true;
                    }
                }
            }

            boolean shouldRemove = true;
            for (Map.Entry<Integer, HashSet<Integer>> entry : valuesToRemoveFromFutureDomain.entrySet()) {
                Integer k = entry.getKey();
                HashSet<Integer> v = entry.getValue() == null ? new HashSet<>() : entry.getValue();
                if (v.size() == D.get(k).size()) {
                    shouldRemove = false;
                    hasConsistent = false;
                    break;
                }
            }
            if (shouldRemove)
                valuesToRemoveFromFutureDomain.forEach((k, v) -> {
                    if (v != null) D.get(k).removeAll(v);
                });
            else {
                for (var key : valuesToRemoveFromFutureDomain.keySet()) {
                    if (copy.containsKey(key))
                        valuesToRemoveFromFutureDomain.put(key, copy.get(key));
                    else
                        valuesToRemoveFromFutureDomain.put(key, new HashSet<>());
                }
            }

            if (hasConsistent) {
                instantiated = a;
                break;
            }

        }

        D.get(currentIndex).removeAll(valuesToRemoveFromCurrentDomain);

        return instantiated;
    }
}
