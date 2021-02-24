package ac;

import ac.logic.ArcConsistencyStaticMethods;
import ac.models.AcResponse;
import ac.models.QueueTuple;
import csp.Constraint;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ArcConsistency_3 {
    public static AcResponse solve(List<Constraint> constraints) {
        Queue<QueueTuple> queue = new LinkedList<>();
        for (var constraint: constraints) {
           queue.add(new QueueTuple(
                   constraint.getVariables().get(0),
                   constraint.arity == 2 ? constraint.getVariables().get(1) : null,
                   constraint
           ));
        }
        var acResponse = new AcResponse();
        while (!queue.isEmpty()) {
            var item = queue.poll();
            var xi = item.xi;
            var xj = item.constraint.arity == 2 ? item.xj : null;
            var reviseResponse = ArcConsistencyStaticMethods.revise(xi, xj, item.constraint, false);
            acResponse.fVal += reviseResponse.fVal;
            acResponse.cc += reviseResponse.cc;
            if (xi.getDomain().getCurrentDomain().isEmpty()) {
                acResponse.isArcConsistent = false;
                return acResponse;
            }
            if (reviseResponse.domainModified) {
                for (var xk : xi.getNeighbors()) {
                    queue.add(new QueueTuple(xk, xj, item.constraint));
                }
            }
        }
        acResponse.isArcConsistent = true;

        return acResponse;
    }
}
