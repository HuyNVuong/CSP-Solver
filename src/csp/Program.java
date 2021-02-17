package csp;

import ac.ArcConsistency_1;

public class Program {
    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }


        MyParser parser = new MyParser(args[1]);
        parser.parse();
        parser.verbose();

        ArcConsistency_1.Solve(parser.getConstraints());

        parser.verbose();
    }
}
