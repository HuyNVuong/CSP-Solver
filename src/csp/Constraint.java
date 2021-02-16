package csp;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PExtensionConstraint;
import abscon.instance.components.PIntensionConstraint;
import abscon.instance.components.PVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Constraint {

    private final PConstraint constraintRef;

    private final List<Variable> variables;

    public Map<Integer, Integer> binaryConstraintValueLookup;

    public Constraint(PConstraint constraint) {
        constraintRef = constraint;
        variables = Arrays.stream(constraint.getScope())
                .map(Variable::new)
                .collect(Collectors.toList());
        if (constraintRef instanceof PExtensionConstraint) {
            var conExtend = (PExtensionConstraint) constraintRef;
            var foo = Arrays.stream(conExtend.getRelation().getTuples())
                    .collect(Collectors.groupingBy(entry -> entry[0]));
        }
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public String toString() {
        String repr = "";
        repr += String.format("Name: %s, ", constraintRef.getName());
        List<String> scopeName = Arrays.stream(constraintRef.getScope())
                .map(PVariable::getName)
                .collect(Collectors.toList());
        repr += String.format("variables: {%s}, ", String.join(",", scopeName));
        if (constraintRef instanceof PExtensionConstraint) {
            var conExtend = (PExtensionConstraint) constraintRef;
            repr += String.format("definition: %s ", conExtend.getRelation().getSemantics());
            List<String> relationTuples = Arrays.stream(conExtend.getRelation().getTuples())
                    .map(tuple -> String.format("(%s)", Arrays.stream(tuple).mapToObj(t -> String.format("%d", t)).collect(Collectors.joining(","))))
                    .collect(Collectors.toList());
            repr += String.format("{%s}", String.join(",", relationTuples));
        } else if (constraintRef instanceof PIntensionConstraint) {
            var conIntent = (PIntensionConstraint) constraintRef;
            repr += String.format("definition: intension function: %s, ", conIntent.getFunction().getFunctionalExpression());
            repr += String.format("params: {%s}", String.join(",", conIntent.getParameters()));
        }


        return repr;
    }
}
