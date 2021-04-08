package algorithms.search;

import algorithms.models.SearchResponse;
import csp.Variable;
import nc.NodeConsistency;

import java.util.*;
import java.util.stream.Collectors;

public class ForwardChecking {
    static long cc;
    static long nv;

    public static SearchResponse search(List<Variable> variables, boolean solveAllSolutions) {
        return search(variables, solveAllSolutions, true);
    }

    public static SearchResponse search(List<Variable> variables, boolean solveAllSolutions, boolean useDynamicOrdering) {
        cc = 0;
        nv = 0;
        variables.forEach(NodeConsistency::enforceNodeConsistency);

        List<HashSet<Integer>> D = variables.stream().map(v -> {
            var domain = v.getDomain();
            return domain.getCurrentDomain();
        }).collect(Collectors.toList());
        int i = 0, n = variables.size();
        var lastInstantiatedState = new ArrayList<ArrayList<HashSet<Integer>>>();
        for (int j = 0; j < n; j++) {
            lastInstantiatedState.add(new ArrayList<>(variables.stream()
                    .map(vi -> new HashSet<>(vi.getDomain().getCurrentDomain()))
                    .collect(Collectors.toList())));
        }
        lastInstantiatedState.add(new ArrayList<>(variables.stream()
                .map(vi -> new HashSet<>(vi.getDomain().getCurrentDomain()))
                .collect(Collectors.toList())));
        var solutions = new ArrayList<ArrayList<Integer>>();
        var visited = new ArrayList<Integer>();

        var variableLookup = variables.stream().collect(Collectors.toMap(Variable::getName, v -> v));
        var visitedVariables = new ArrayList<String>();
        long bt = 0;

        while(0 <= i && (i < n || solveAllSolutions)) {
            var xi = i == n ? null : selectValue(n, D, i, variables.get(i), variables);
            if (xi == null) {
                bt++;
                if (!visited.isEmpty()) {
                    visited.remove(visited.size() - 1);
                    visitedVariables.remove(visitedVariables.size() - 1);
                }
                i--;
                for (int k = i + 1; k < n; k++)
                    if (i >= 0)
                        D.set(k, new HashSet<>(lastInstantiatedState.get(i).get(k)));
            } else {
                visited.add(xi);
                visitedVariables.add(variables.get(i).getName());
                nv++;
                if (useDynamicOrdering) {
                    var nextVariablesOrders = leastDomainOrderingHeuristic(variableLookup, visitedVariables);
                    variables = nextVariablesOrders.stream().map(variableLookup::get).collect(Collectors.toList());
                    for (int j = 0; j < variables.size(); j++) {
                        D.set(j, variables.get(j).getDomain().getCurrentDomain());
                    }
                }

                i++;
                lastInstantiatedState.set(i, new ArrayList<>(D.stream().map(HashSet::new).collect(Collectors.toList())));
                if (i == n) {
                    solutions.add(new ArrayList<>(visited));
                }
            }
        }

        return new SearchResponse(solutions, cc, nv, bt, visitedVariables);
    }

    private static ArrayList<String> sortRemainingVariables(Map<String, Variable> variableLookup, List<String> seenVariables) {
        var keys = new ArrayList<>(variableLookup.keySet());
        var degreeCount = keys.stream()
                .collect(Collectors.toMap(k -> k, k -> variableLookup.get(k).getNeighbors().size()));

        var seen = new HashSet<String>();
        var ordered = new ArrayList<String>();
        for (var v : seenVariables) {
            variableLookup.get(v).getNeighbors().forEach(n -> {
                degreeCount.put(n.getName(), degreeCount.get(n.getName()) - 1);
            });
            seen.add(v);
            ordered.add(v);
        }
        do {
            var degree = buildOrderedDegreeMap(keys, degreeCount, seen);

            var maxDegree = degree.lastEntry().getValue().stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
            for (var k : maxDegree) {
                variableLookup.get(k).getNeighbors().forEach(n -> {
                    degreeCount.put(n.getName(), degreeCount.get(n.getName()) - 1);
                });
                seen.add(k);
                ordered.add(k);
            }
        } while (seen.size() < keys.size());

        return ordered;
    }

    public static ArrayList<String> leastDomainOrderingHeuristic(Map<String, Variable> variableLookup, List<String> seenVariables) {
        var keys = new ArrayList<>(variableLookup.keySet());

        keys.sort((a, b) -> {
            if (seenVariables.contains(a) && seenVariables.contains(b)) {
                return a.compareTo(b);
            }
            if (seenVariables.contains(a)) {
                return -1;
            }
            if (seenVariables.contains(b)) {
                return 1;
            }
            if (variableLookup.get(a).getDomain().getCurrentDomain().size()
                    == variableLookup.get(b).getDomain().getCurrentDomain().size()
            ) {
                return a.compareTo(b);
            }

            return variableLookup.get(a).getDomain().getCurrentDomain().size()
                    - variableLookup.get(b).getDomain().getCurrentDomain().size();
        });

        return keys;
    }

    public static TreeMap<Integer, List<String>> buildOrderedDegreeMap(List<String> keys, Map<String, Integer> degreeCount, HashSet<String> seen) {
        var degree = new TreeMap<Integer, List<String>>();
        keys.forEach(k -> {
            if (seen.contains(k))
                return;
            degree.putIfAbsent(degreeCount.get(k), new ArrayList<>());
            degree.get(degreeCount.get(k)).add(k);
        });
        return degree;
    }

    private static Integer selectValue(
            int n, List<HashSet<Integer>> D, int currentIndex,
            Variable currentVariable, List<Variable> variables
    ) {
        var currentDomain = D.get(currentIndex);
        var valuesToRemoveFromCurrentDomain = new HashSet<Integer>();
        var valuesToRemoveFromFutureDomain = new HashMap<Integer, HashSet<Integer>>();
        Integer chosenValue = null;
        for (var a : currentDomain) {
            valuesToRemoveFromCurrentDomain.add(a);
            var hasConsistent = currentIndex + 1 == n;
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
                        cc++;

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
                chosenValue = a;
                break;
            }
        }

        D.get(currentIndex).removeAll(valuesToRemoveFromCurrentDomain);

        return chosenValue;
    }
}
