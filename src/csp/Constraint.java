package csp;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PExtensionConstraint;
import abscon.instance.components.PIntensionConstraint;
import abscon.instance.components.PVariable;

import java.util.*;
import java.util.stream.Collectors;

public class Constraint {

    private final PConstraint constraintRef;

    private List<Variable> variables;

    private final List<String> variableKeys;

    public Set<BinaryPair> binaryConstraintValueLookup;

    public String name;

    public String definition;

    public final int arity;

    public Constraint(PConstraint constraint) {
        constraintRef = constraint;

        variableKeys = Arrays.stream(constraint.getScope())
                .map(PVariable::getName)
                .collect(Collectors.toList());
        if (constraint instanceof PExtensionConstraint conExtend) {
            binaryConstraintValueLookup = Arrays.stream(conExtend.getRelation().getTuples())
                    .map(tuple -> new BinaryPair(tuple[0], tuple[1]))
                    .collect(Collectors.toSet());
            definition = conExtend.getRelation().getSemantics();
        }
        arity = constraint.getArity();
    }

    public void setVariables(Map<String, Variable> variableLookup) {
        variables = variableKeys.stream().map(variableLookup::get).collect(Collectors.toList());
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
        if (constraintRef instanceof PExtensionConstraint conExtend) {
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
