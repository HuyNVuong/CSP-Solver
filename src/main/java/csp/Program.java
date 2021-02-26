package csp;

import ac.AcSolver;
import ac.ArcConsistency_1;
import ac.ArcConsistency_3;

import java.io.File;
import java.util.stream.Stream;

public class Program {
    public static void main(String [] args) {
        if (args.length < 2) {
            solveBulkLocally();
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

    public static void solveBulkLocally() {
        var acSolver = new AcSolver(ArcConsistency_3::solve);
        Stream.of(new File("./v32_d8_p20").listFiles())
                .map(File::getName)
                .forEach(subFolder -> {
                    Stream.of(new File(String.format("./v32_d8_p20/%s", subFolder)).listFiles())
                            .map(File::getName)
                            .forEach(file -> {
                                var filePath = String.format(
                                        "./v32_d8_p20/%s/%s",
                                        subFolder, file);
                                acSolver.loadInstance(filePath);
                                acSolver.solve();
                            });
                });
        acSolver.buildExcelReport();
    }
}