package csp;

import algorithms.ac.AcSolver;
import algorithms.ac.ArcConsistency_1;
import algorithms.ac.ArcConsistency_3;
import algorithms.ac.ArcConsistency_4;
import algorithms.search.*;

import java.io.File;
import java.util.stream.Stream;

public class Program {


    public static void main(String[] args) {
        if (args.length < 2) {
            solve17a();
//            solve17c();
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
                case "ac4" -> new AcSolver(ArcConsistency_4::solve);
                default    -> null;
            };
            assert acSolver != null;
            acSolver.loadInstance(args[1]);
            acSolver.solve();
            acSolver.report();
        } else if (args.length == 6) {
            var btSolver = switch (args[3]) {
                case "BT" -> new SearchSolver(BackTracking::search);
                case "CBJ" -> new SearchSolver(ConflictedBackJumping::search);
                case "FC" -> new SearchSolver(ForwardChecking::search);
                case "FCCBJ" -> new SearchSolver(ForwardCheckingWithConflictDirectedBackJumping::search);
                default   ->  null;
            };
            assert btSolver != null;
            btSolver.loadInstance(args[1]);
//            btSolver.solve(args[5]);
            btSolver.solve(args[5]);
            btSolver.report();
            for (var t : new String[]{"LX", "LD", "DEG", "DD", "MWO"}) {
                btSolver.solve(t);
                btSolver.report();
            }
        }
    }

    public static void solve17c() {
        var acSolver = new AcSolver(ArcConsistency_1::solve);
        Stream.of(new File("./v32_d8_p20").listFiles())
                .map(File::getName)
                .forEach(subFolder -> {
                    Stream.of(new File(String.format("./v32_d8_p20/%s", subFolder)).listFiles())
                            .map(File::getName)
                            .forEach(file -> {
                                var filePath = String.format(
                                        "./v32_d8_p20/%s/%s",
                                        subFolder,
                                        file);
                                acSolver.loadInstance(filePath);
                                acSolver.solve();
                            });
                });
        acSolver.buildExcelReport();
    }

    public static void solve17a() {
        var acSolver = new AcSolver(ArcConsistency_4::solve);
        Stream.of(new File("./17a").listFiles())
                .map(File::getName)
                .forEach(file -> {
                    var filePath = String.format("./17a/%s", file);
                    acSolver.loadInstance(filePath);
                    acSolver.solve();
                });
        acSolver.buildExcelReport();
    }
}
