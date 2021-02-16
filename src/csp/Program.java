package csp;

public class Program {
    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }


        MyParser parser = new MyParser("xmls/3queens-conflicts.xml");
        parser.parse();
    }
}
