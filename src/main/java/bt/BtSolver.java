package bt;

import bt.models.BtResponse;
import csp.MyParser;
import csp.Variable;
import interfaces.F3;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

public class BtSolver {
    private String variableOrdering;
    private String orderingHeuristic;

    private long cc;
    private long nv;
    private long bt;
    private double cpuTime;

    private long allSolsNv;
    private long allSolsCc;
    private long allSolsBt;
    private double allSolsCpuTime;
    private int numberOfSolutions;

    private MyParser parser;
    private long startTime;
    private long time;

    private final F3<List<Variable>, Boolean, BtResponse> btFunction;

    private final List<Long> cpuTimes;
    private final List<Integer> ccs;
    private final List<Integer> nvs;

    private int[] firstSolution;
    private Map<String, Variable> variableLookup;


    public BtSolver(F3<List<Variable>, Boolean, BtResponse> solverFunction) {
        btFunction = solverFunction;
        cpuTimes = new ArrayList<>();
        ccs = new ArrayList<>();
        nvs = new ArrayList<>();
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

    public void solve() {
        var orderedVariables = variableLookup.keySet().stream().sorted(Comparator.naturalOrder())
                .map(variableLookup::get).collect(Collectors.toList());
        startTime = getCpuTime();
        var oneSolutionBtResponse = btFunction.apply(orderedVariables, false);
        time = getCpuTime() - startTime;
        cpuTime = time / 1000000.0;
        cc = oneSolutionBtResponse.cc;
        nv = oneSolutionBtResponse.nv;
        bt = oneSolutionBtResponse.bt;
        firstSolution = oneSolutionBtResponse.paths.isEmpty()
                ? new int[0]
                : oneSolutionBtResponse.paths.get(0).stream().mapToInt(i -> i).toArray();


        startTime = getCpuTime();
        var allSolutionsBtResponse = btFunction.apply(orderedVariables, true);
        time = getCpuTime() - startTime;
        allSolsCpuTime = time / 1000000.0;
        allSolsCc = allSolutionsBtResponse.cc;
        allSolsNv = allSolutionsBtResponse.nv;
        allSolsBt = allSolutionsBtResponse.bt;
        numberOfSolutions = allSolutionsBtResponse.paths.size();
    }

    public void report() {
        System.out.println("Instance name: " + parser.name);
        System.out.println("variable-order-heuristic: LX|LD|DEG|UU");
        System.out.println("var-static-dynamic: static");
        System.out.println("cc: " + cc);
        System.out.println("nv: " + nv);
        System.out.println("bt: " + bt);
        System.out.println("cpu: " + cpuTime);
        System.out.printf("First Solution:\n\t%s\n", Arrays.toString(firstSolution));
        System.out.println("all-sol cc: " + allSolsCc);
        System.out.println("all-sol nv: " + allSolsNv);
        System.out.println("all-sol bt: " + allSolsBt);
        System.out.println("all-sol cpu: " + allSolsCpuTime);
        System.out.println("Number of solutions: " + numberOfSolutions);
    }

    private int leastDomainOrderingHeuristic(String a, String b) {
        if (variableLookup.get(a).getDomain().getCurrentDomain().size() == variableLookup.get(b).getDomain().getCurrentDomain().size()) {
            return a.compareTo(b);
        }

        return variableLookup.get(a).getDomain().getCurrentDomain().size() - variableLookup.get(b).getDomain().getCurrentDomain().size();
    }

    public long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isThreadCpuTimeSupported())
            return 0L;
        return bean.getThreadCpuTime(java.lang.Thread.currentThread().getId());
    }

}
