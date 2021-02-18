package ac;

import csp.Constraint;

import java.util.List;
import java.util.function.Function;

public class AcSolver {
    private String problem;

    private double timeSetup;

    private double cpuTime;

    private int cc;

    private int fVal;

    private int iSize;

    private int fSize;

    private int fEffect;

    private Function<List<Constraint>, Void> arcConsistencyFunction;

    public void setAcSolverFunction(Function<List<Constraint>, Void> arcConsistencyFunction) {
        this.arcConsistencyFunction = arcConsistencyFunction;
    }
}
