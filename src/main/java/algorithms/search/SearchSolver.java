package algorithms.search;

import abscon.instance.tools.SolutionChecker;
import algorithms.models.SearchResponse;
import csp.MyParser;
import csp.Variable;
import interfaces.F3;

import java.util.*;
import java.util.stream.Collectors;

public class SearchSolver {
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
    private SolutionChecker solutionChecker;
    private String fileName;

    private final F3<List<Variable>, Boolean, SearchResponse> searchFunction;

    private int[] firstSolution;
    private List<String> searchedVariables;
    private List<Variable> variables;


    public SearchSolver(F3<List<Variable>, Boolean, SearchResponse> solverFunction) {
        searchFunction = solverFunction;
        variables = new ArrayList<>();
        cpuTime = 0;
        allSolsCpuTime = 0;
    }

    public void loadInstance(String fileName) {
        parser = new MyParser(fileName);
        this.fileName = fileName;
        parser.parse();
        variables = parser.getVariables();
        parser.verbose();
    }

    public void solve(String order) {
        variableOrdering = order;
        var keys = variables.stream().map(Variable::getName).collect(Collectors.toList());
        var staticOrdering = new StaticOrdering(variables);
        var variableLookup = variables.stream().collect(Collectors.toMap(Variable::getName, v -> v));
        var orderedVariables = (switch (order) {
            case "LX" -> keys.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            case "LD" -> staticOrdering.leastDomainOrderingHeuristic(keys);
            case "DEG" -> staticOrdering.maximalDegreeOrderingHeuristic(keys);
            case "DD" -> staticOrdering.minimalDomainOverDegreeOrderingHeuristic(keys);
            case "MWO" -> staticOrdering.minimalWidthOrderingHeuristic(keys);
            default -> keys;
        }).stream().map(variableLookup::get).collect(Collectors.toList());

        System.out.println(orderedVariables.stream().map(Variable::getName).collect(Collectors.joining(",")));

        var startTime = System.nanoTime();
        var oneSolutionBtResponse = searchFunction.apply(orderedVariables, false);
        var time = System.nanoTime() - startTime;
        cpuTime = time / 1000000.0;
        cc = oneSolutionBtResponse.cc;
        nv = oneSolutionBtResponse.nv;
        bt = oneSolutionBtResponse.bt;
        firstSolution = oneSolutionBtResponse.paths.isEmpty()
                ? new int[0]
                : oneSolutionBtResponse.paths.get(0).stream().mapToInt(i -> i).toArray();
        searchedVariables = oneSolutionBtResponse.orders.isEmpty()
                ? new ArrayList<String>()
                : oneSolutionBtResponse.orders;

        variables.forEach(Variable::resetCurrentDomain);
        var allSolStartTime = System.nanoTime();
        var allSolutionsBtResponse = searchFunction.apply(orderedVariables, true);
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
        System.out.println("First Solution: " + Arrays.toString(new List[]{searchedVariables}));
        System.out.println("First Solution: " + Arrays.toString(firstSolution));
        System.out.println("all-sol cc: " + allSolsCc);
        System.out.println("all-sol nv: " + allSolsNv);
        System.out.println("all-sol bt: " + allSolsBt);
        System.out.println("all-sol cpu: " + allSolsCpuTime);
        System.out.println("Number of solutions: " + numberOfSolutions);
    }
}
