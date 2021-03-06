package bt;

import bt.models.BtResponse;
import csp.MyParser;
import csp.Variable;
import interfaces.F3;

import java.util.*;
import java.util.stream.Collectors;

public class BtSolver {
    private long cc;
    private long nv;
    private long bt;
    private double cpuTime;

    private long allSolsNv;
    private long allSolsCc;
    private long allSolsBt;
    private double allSolsCpuTime;
    private int numberOfSolutions;
    private String variableOrdering;

    private MyParser parser;

    private final F3<List<Variable>, Boolean, BtResponse> btFunction;

    private int[] firstSolution;
    private Map<String, Variable> variableLookup;


    public BtSolver(F3<List<Variable>, Boolean, BtResponse> solverFunction) {
        btFunction = solverFunction;
        variableLookup = new HashMap<>();
        cpuTime = 0;
        allSolsCpuTime = 0;
    }

    public void loadInstance(String fileName) {
        parser = new MyParser(fileName);
        parser.parse();
        for (var v : parser.getVariables()) {
            variableLookup.put(v.getName(), v);
        }
        parser.verbose();
    }

    public void solve(String order) {
        variableOrdering = order;
        var keys = new ArrayList<>(variableLookup.keySet());
        var orderedVariables = (switch (order) {
            case "LX" -> keys.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            case "LD" -> leastDomainOrderingHeuristic(keys);
            case "DEG" -> maximalDegreeOrderingHeuristic(keys);
            case "DD" -> minimalDomainOverDegreeOrderingHeuristic(keys);
            default -> keys;
        }).stream().map(variableLookup::get).collect(Collectors.toList());

//        var acSolver = new AcSolver(ArcConsistency_1::solve);
//        acSolver.parser = parser;
//        acSolver.solve();
        System.out.println(orderedVariables.stream().map(Variable::getName).collect(Collectors.joining(",")));

        var startTime = System.nanoTime();
        var oneSolutionBtResponse = btFunction.apply(orderedVariables, false);
        var time = System.nanoTime() - startTime;
        cpuTime = time / 1000000.0;
        cc = oneSolutionBtResponse.cc;
        nv = oneSolutionBtResponse.nv;
        bt = oneSolutionBtResponse.bt;
        firstSolution = oneSolutionBtResponse.paths.isEmpty()
                ? new int[0]
                : oneSolutionBtResponse.paths.get(0).stream().mapToInt(i -> i).toArray();


        var allSolStartTime = System.nanoTime();
        var allSolutionsBtResponse = btFunction.apply(orderedVariables, true);
        var allSolEndTime = System.nanoTime() - allSolStartTime;
        allSolsCpuTime = allSolEndTime / 1000000.0;
        allSolsCc = allSolutionsBtResponse.cc;
        allSolsNv = allSolutionsBtResponse.nv;
        allSolsBt = allSolutionsBtResponse.bt;
        numberOfSolutions = allSolutionsBtResponse.paths.size();
    }

    public void report() {
        System.out.println("Instance name: " + parser.name);
        System.out.println("variable-order-heuristic: " + variableOrdering);
        System.out.println("var-static-dynamic: static");
        System.out.println("cc: " + cc);
        System.out.println("nv: " + nv);
        System.out.println("bt: " + bt);
        System.out.println("cpu: " + cpuTime);
        System.out.println("First Solution: " + Arrays.toString(firstSolution));
        System.out.println("all-sol cc: " + allSolsCc);
        System.out.println("all-sol nv: " + allSolsNv);
        System.out.println("all-sol bt: " + allSolsBt);
        System.out.println("all-sol cpu: " + allSolsCpuTime);
        System.out.println("Number of solutions: " + numberOfSolutions);
    }


    private List<String> leastDomainOrderingHeuristic(List<String> keys) {
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

    private List<String> maximalDegreeOrderingHeuristic(List<String> keys) {
        var degreeCount = keys.stream()
                .collect(Collectors.toMap(k -> k, k -> variableLookup.get(k).getNeighbors().size()));

        var seen = new HashSet<String>();
        var ordered = new ArrayList<String>();
        do {
            var degree = new TreeMap<Integer, List<String>>();
            keys.forEach(k -> {
                if (seen.contains(k))
                    return;
                degree.putIfAbsent(degreeCount.get(k), new ArrayList<>());
                degree.get(degreeCount.get(k)).add(k);
            });

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

    private List<String> minimalDomainOverDegreeOrderingHeuristic(List<String> keys) {
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
