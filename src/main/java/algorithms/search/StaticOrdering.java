package algorithms.search;

import csp.Variable;

import java.util.*;
import java.util.stream.Collectors;

public class StaticOrdering {
    Map<String, Variable> variableLookup;

    public StaticOrdering(List<Variable> variables) {
        this.variableLookup = variables.stream().collect(Collectors.toMap(Variable::getName, v -> v));
    }

    public List<String> leastDomainOrderingHeuristic(List<String> keys) {
        keys.sort((a, b) -> {
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

    public List<String> maximalDegreeOrderingHeuristic(List<String> keys) {
        var degreeCount = keys.stream()
                .collect(Collectors.toMap(k -> k, k -> variableLookup.get(k).getNeighbors().size()));

        var seen = new HashSet<String>();
        var ordered = new ArrayList<String>();
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

    public List<String> minimalWidthOrderingHeuristic(List<String> keys) {
        var degreeCount = keys.stream()
                .collect(Collectors.toMap(k -> k, k -> variableLookup.get(k).getNeighbors().size()));

        var seen = new HashSet<String>();
        var ordered = new ArrayList<String>();
        do {
            var degree = buildOrderedDegreeMap(keys, degreeCount, seen);

            var minWidth = degree.firstEntry().getValue().stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
            for (var k : minWidth) {
                variableLookup.get(k).getNeighbors().forEach(n -> {
                    degreeCount.put(n.getName(), degreeCount.get(n.getName()) - 1);
                });
                seen.add(k);
                ordered.add(0, k);
            }
        } while (seen.size() < keys.size());

        return ordered;
    }

    public TreeMap<Integer, List<String>> buildOrderedDegreeMap(List<String> keys, Map<String, Integer> degreeCount, HashSet<String> seen) {
        var degree = new TreeMap<Integer, List<String>>();
        keys.forEach(k -> {
            if (seen.contains(k))
                return;
            degree.putIfAbsent(degreeCount.get(k), new ArrayList<>());
            degree.get(degreeCount.get(k)).add(k);
        });
        return degree;
    }

    public List<String> minimalDomainOverDegreeOrderingHeuristic(List<String> keys) {
        var ddrCount = keys.stream().collect(Collectors.toMap(
                k -> k,
                k -> variableLookup.get(k).getDomain().getCurrentDomain().size() * 1.0 / variableLookup.get(k).getNeighbors().size()
        ));

        var degreeCount = keys.stream()
                .collect(Collectors.toMap(k -> k, k -> variableLookup.get(k).getNeighbors().size()));

        var seen = new HashSet<String>();
        var ordered = new ArrayList<String>();
        do {
            var degree = new TreeMap<Double, List<String>>();
            keys.forEach(k -> {
                if (seen.contains(k))
                    return;
                degree.putIfAbsent(ddrCount.get(k), new ArrayList<>());
                degree.get(ddrCount.get(k)).add(k);
            });

            var maxDegree = degree.firstEntry().getValue().stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
            for (var k : maxDegree) {
                variableLookup.get(k).getNeighbors().forEach(n -> {
                    degreeCount.put(n.getName(), degreeCount.get(n.getName()) - 1);
                    ddrCount.put(n.getName(),
                            variableLookup.get(k).getDomain().getCurrentDomain().size() * 1.0
                                    / (degreeCount.get(n.getName()))
                    );
                });
                seen.add(k);
                ordered.add(k);
            }
        } while (seen.size() < keys.size());

        return ordered;
    }
}
