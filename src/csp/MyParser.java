package csp;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;
import abscon.instance.tools.InstanceParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyParser {

    private List<Variable> variables;

    private final InstanceParser parser;

    public MyParser(String fileName) {
        parser = new InstanceParser();
        parser.loadInstance(fileName);
    }

    public void parse() {
        parser.parse(false);

        System.out.printf("Instance name: %s\n", parser.getInstanceName());

        PVariable[] parserVariables = parser.getVariables();

        variables = Arrays.stream(parserVariables).map(Variable::new).collect(Collectors.toList());

        var variablesLookup = variables.stream().collect(Collectors.toMap(Variable::getName, item -> item));

        var constraints = new ArrayList<Constraint>();

        for (String key : parser.getMapOfConstraints().keySet()) {
            PConstraint con = parser.getMapOfConstraints().get(key);
            Constraint constraint = new Constraint(con);
            constraints.add(constraint);
            for (PVariable variable : con.getScope()) {
                variablesLookup.get(variable.getName()).addConstraint(con);
            }
        }

        System.out.println("Variables:");
        variables.forEach(System.out::println);
        System.out.println();
        System.out.println("Constraints:");
        constraints.forEach(System.out::println);
    }

    public List<Variable> getVariable() {
        return variables;
    }
}
