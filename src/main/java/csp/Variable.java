package csp;

import abscon.instance.components.PVariable;

import java.util.*;
import java.util.stream.Collectors;

public class Variable {

    private final Domain domain;

    private String name;

    private Map<String, Variable> neighbors;

    private Map<String, Constraint> constraints;

    private Map<String, Constraint> constraintOfNeighbors;

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

    public void addConstraint(Constraint constraint) {
        constraints.putIfAbsent(constraint.name, constraint);
    }

    public void removeValues(Set<Integer> values) {
        values.forEach(domain::removeValue);
    }

    public List<Variable> getNeighbors() {
        for (var con : constraints.values()) {
            for (var neighbor : con.getVariables()) {
                if (neighbor.getName().equals(name))
                    continue;
                neighbors.putIfAbsent(neighbor.getName(), neighbor);
                constraintOfNeighbors.putIfAbsent(neighbor.getName(), con);
            }
        }

        return new ArrayList<>(neighbors.values());
    }

    public Constraint getSharedConstraint(String neighborName) {
        return constraintOfNeighbors.get(neighborName);
    }

    public List<Constraint> getAllConstraints() {
        return new ArrayList<>(constraints.values());
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
