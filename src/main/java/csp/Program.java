package csp;

import ac.AcSolver;
import ac.ArcConsistency_1;
import ac.ArcConsistency_3;
import ac.ArcConsistency_4;
import bt.BackTracking;
import bt.BtSolver;

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
        } else if (args.length == 8) {
            var btSolver = switch (args[5]) {
                case "BT" -> new BtSolver(BackTracking::vanillaSearch);
                default   ->  null;
            };
            assert btSolver != null;
            btSolver.loadInstance(args[1]);
            btSolver.solve(args[7]);
            btSolver.solve(args[7]);
            for (var t : new String[]{"LX", "LD", "DEG", "DD"}) {
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
