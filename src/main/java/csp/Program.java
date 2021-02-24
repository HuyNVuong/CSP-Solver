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
            var acSolver = switch (args[3]) {
                case "ac1" -> new AcSolver(ArcConsistency_1::solve);
                case "ac3" -> new AcSolver(ArcConsistency_3::solve);
                default    -> null;
            };
            if (acSolver != null) {
                acSolver.loadInstance(args[1]);
                acSolver.solve();
                acSolver.report();
            }

        }

    }
}
