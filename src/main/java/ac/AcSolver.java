package ac;

import ac.models.AcResponse;
import csp.Constraint;
import csp.MyParser;
import csp.Variable;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.function.Function;

public class AcSolver {
    private double timeSetup;

    private long cpuTime;

    private int cc;

    private int fVal;

    private double iSize;

    private double fSize;

    private double fEffect;

    private MyParser parser;

    private final Function<List<Constraint>, AcResponse> arcConsistencyFunction;

    private final Timer timer;

    public AcSolver() {
        arcConsistencyFunction = ArcConsistency_1::solve;
        timer = new Timer();
        fEffect = -1;
    }

    public AcSolver(Function<List<Constraint>, AcResponse> acSolverFunction) {
        arcConsistencyFunction = acSolverFunction;
        timer = new Timer();
        fEffect = -1;
    }

    public void loadInstance(String problem) {
        parser = new MyParser(problem);
        parser.parse();
    }

    public void solve() {
        var networks = parser.getConstraints();
        var startTime = System.nanoTime();
        var acResponse = arcConsistencyFunction.apply(networks);
        var endTime = System.nanoTime();
        iSize = parser.getVariables().stream()
                .map(Variable::getDomain)
                .mapToDouble(domain -> Math.log(domain.getInitialDomain().size()))
                .sum();
        fSize = parser.getVariables().stream()
                .map(Variable::getDomain)
                .mapToDouble(domain -> Math.log(domain.getCurrentDomain().size()))
                .sum();
        if (acResponse.isArcConsistent) {
            fEffect = parser.getVariables().stream()
                    .map(Variable::getDomain)
                    .mapToDouble(domain -> {
                        var logInitialDomain = Math.log(domain.getInitialDomain().size());
                        var logCurrentDomain = Math.log(domain.getCurrentDomain().size());
                        return Math.abs(logCurrentDomain - logInitialDomain);
                    })
                    .sum();
            System.out.println("Problem is arc-consistent");
        }

        cpuTime = endTime - startTime;
        cc = acResponse.cc;
        fVal = acResponse.fVal;
    }

    public long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isThreadCpuTimeSupported())
            return 0L;
        return bean.getThreadCpuTime(java.lang.Thread.currentThread().getId());
    }

    public static double logProductSum(Variable v1, Variable v2) {
        return v1.logProduct() + v2.logProduct();
    }

    public void report() {
        System.out.printf("Instance name: %s\n", parser.name);
        System.out.printf("cc: %d\n", cc);
        System.out.printf("cpu: %.3f\n", cpuTime / 1000000.0);
        System.out.printf("fval: %d\n", fVal);
        System.out.println("iSize:  " + iSize);
        System.out.println("fSize: " + (Double.isFinite(fSize) ? fSize : "false"));
        System.out.println("fEffect: " + (fEffect != -1 ? fEffect : "false"));
    }
}
