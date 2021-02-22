package csp;

import ac.AcSolver;
import ac.ArcConsistency_1;
import ac.ArcConsistency_3;

public class Program {
    public static void main(String [] args) {
        if (args.length < 2) {
            return;
        }
        if (args.length == 2) {
            var parser = new MyParser(args[1]);
            parser.parse();
            parser.verbose();
        } else if (args.length == 4) {
            var acSolver = args[3].equals("ac1")
                    ? new AcSolver(ArcConsistency_1::solve)
                    : new AcSolver(ArcConsistency_3::solve);
            acSolver.loadInstance(args[1]);
            acSolver.solve();
            acSolver.report();
        }

    }
}
