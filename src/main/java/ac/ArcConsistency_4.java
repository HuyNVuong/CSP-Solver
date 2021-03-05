package ac;

import ac.models.AcResponse;
import ac.models.VVP;
import ac.models.VVPV;
import csp.BinaryPair;
import csp.Constraint;
import csp.Variable;

import java.util.*;

public class ArcConsistency_4 {
    public static AcResponse solve(List<Constraint> constraints) {
        var acResponse = new AcResponse();
        var isArcConsistent = true;
        var S = new HashMap<VVP, ArrayList<VVP>>();
        var variables = new HashMap<String, Variable>();
        var counters = new HashMap<VVPV, Integer>();

        for (var constraint : constraints) {
            if (constraint.arity == 2) {
                var vi = constraint.getVariables().get(0);
                var vj = constraint.getVariables().get(1);
                variables.putIfAbsent(vi.getName(), vi);
                variables.putIfAbsent(vj.getName(), vj);
                populate(vi, vj, constraint, counters, S, acResponse, true);
                populate(vj, vi, constraint, counters, S, acResponse,false);
            }
        }

        Queue<VVP> queue = new LinkedList<>();
        for (var counter : counters.entrySet()) {
            if (counter.getValue() == 0) {
                queue.add(new VVP(counter.getKey().v1, counter.getKey().a1));
            }
        }

        var M = new HashSet<VVP>();
        while (!queue.isEmpty()) {
            var vvp = queue.poll();
            M.add(vvp);
            variables.get(vvp.v.getName()).removeValues(new HashSet<>(Collections.singletonList(vvp.value)));
            for (var vvpNeighbor : S.getOrDefault(vvp, new ArrayList<>())) {
                acResponse.cc++;
                if (!counters.containsKey(new VVPV(vvpNeighbor.v, vvpNeighbor.value, vvp.v)))
                    continue;
                counters.put(new VVPV(vvpNeighbor.v, vvpNeighbor.value, vvp.v), counters.get(new VVPV(vvpNeighbor.v, vvpNeighbor.value, vvp.v)) - 1);

                acResponse.fVal++;
                if (counters.get(new VVPV(vvpNeighbor.v, vvpNeighbor.value, vvp.v)) == 0)
                    queue.add(new VVP(vvpNeighbor.v, vvpNeighbor.value));
            }

            if (variables.values().stream().anyMatch(v -> v.getDomain().getCurrentDomain().size() == 0)) {
                isArcConsistent = false;
                break;
            }

        }

        acResponse.isArcConsistent = isArcConsistent;
        acResponse.fVal = M.size();
        return acResponse;
    }

    private static void populate(
            Variable vi, Variable vj, Constraint constraint,
            Map<VVPV, Integer> counters,
            Map<VVP, ArrayList<VVP>> S,
            AcResponse acResponse,
            boolean nominalOrder
    ) {
        for (int di : vi.getDomain().getCurrentDomain()) {
            for (int dj : vj.getDomain().getCurrentDomain()) {
                S.putIfAbsent(new VVP(vi, di), new ArrayList<>());

                counters.putIfAbsent(new VVPV(vi, di, vj), 0);
                if (constraint.isIntension) {
                    acResponse.cc++;
                    var pair = nominalOrder
                            ? new int[]{di, dj}
                            : new int[]{dj, di};
                    if (constraint.intensionEvaluator.apply(pair) == 0) {
                        counters.put(new VVPV(vi, di, vj), counters.get(new VVPV(vi, di, vj)) + 1);
                        S.get(new VVP(vi, di)).add(new VVP(vj, dj));
                    }
                } else {
                    var binaryLookup = nominalOrder
                            ? constraint.binaryConstraintValueLookup
                            : constraint.reversedBinaryConstraintValueLookup;

                    if (binaryLookup.contains(new BinaryPair(di, dj))) {
                        if (constraint.definition.equals("supports")) {
                            acResponse.cc++;
                            counters.put(new VVPV(vi, di, vj), counters.get(new VVPV(vi, di, vj)) + 1);
                            S.get(new VVP(vi, di)).add(new VVP(vj, dj));
                        }
                    }
                    if (!binaryLookup.contains(new BinaryPair(di, dj))) {
                        if (constraint.definition.equals("conflicts")) {
                            acResponse.cc++;
                            counters.put(new VVPV(vi, di, vj), counters.get(new VVPV(vi, di, vj)) + 1);
                            S.get(new VVP(vi, di)).add(new VVP(vj, dj));
                        }
                    }
                }
            }
        }
    }
}
