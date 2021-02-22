package csp;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

import java.util.*;
import java.util.stream.Collectors;

public class Variable {

    private final Domain domain;

    private String name;

    private Map<String, PVariable> neighbors;

    private Map<String, PConstraint> constraints;

    private Map<String, PConstraint> constraintOfNeighbors;

    public Variable(PVariable var) {
        name = var.getName();
        domain = new Domain(var.getDomain());
        neighbors = new HashMap<>();
        constraints = new HashMap<>();
        constraintOfNeighbors = new HashMap<>();
    }

    public Domain getDomain() { return domain; }

    public String getName() {
        return name;
    }

    public void addConstraint(PConstraint constraint) {
        if (constraints.containsKey(constraint.getName())) {
            return;
        }

        constraints.put(constraint.getName(), constraint);
    }

    public void removeValues(Set<Integer> values) {
        values.forEach(domain::removeValue);
    }

    private void getNeighbors() {
        for (PConstraint con : constraints.values()) {
            for (PVariable neighbor : con.getScope()) {
                if (neighbor.getName().equals(name))
                    continue;
                neighbors.putIfAbsent(neighbor.getName(), neighbor);
                constraintOfNeighbors.putIfAbsent(neighbor.getName(), con);
            }
        }

        neighbors.values().stream().map(Variable::new).collect(Collectors.toList());
    }

    private String getConstraintRepr() {
        List<String> constraintKeys = new ArrayList<>(constraints.keySet());

        return "{" + String.join(",", constraintKeys) + "}";
    }

    private String getNeighborsRepr() {
        getNeighbors();
        List<String> neighborKeys = new ArrayList<>(neighbors.keySet());

        return "{" + String.join(",", neighborKeys) + "}";
    }

    private String getDomainRepr() {
        List<String> domainValueStr = domain.getCurrentDomain().stream()
                .map(d -> String.format("%d", d))
                .collect(Collectors.toList());

        return "{" + String.join(",", domainValueStr) + "}";
    }

    public double logProduct() {
        return Math.log(domain.getCurrentDomain().stream().reduce(1, (a, b) -> a * b))
                / Math.log(2);
    }

    public String toString() {
        return String.format("Name: %s, initial-domain: %s, constraints: %s, neighbors: %s",
                name,
                getDomainRepr(),
                getConstraintRepr(),
                getNeighborsRepr());
    }
}
