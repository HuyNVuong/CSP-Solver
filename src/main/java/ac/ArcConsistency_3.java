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
        var acResponse = new AcResponse();
        Queue<QueueTuple> queue = new LinkedList<>();
        for (var constraint : constraints) {
            switch (constraint.arity) {
                case 1 -> queue.add(new QueueTuple(constraint.getVariables().get(0), constraint));
                case 2 -> {
                    queue.add(new QueueTuple(
                            constraint.getVariables().get(0),
                            constraint.getVariables().get(1),
                            constraint
                    ));
                    queue.add(new QueueTuple(
                            constraint.getVariables().get(1),
                            constraint.getVariables().get(0),
                            constraint
                    ));
                }
            }
        }

        while (!queue.isEmpty()) {
            var item = queue.poll();
            var xi = item.xi;
            var xj = item.xj;
            var reviseResponse = ArcConsistencyStaticMethods.revise(xi, xj, item.constraint, item.reversed);
            acResponse.fVal += reviseResponse.fVal;
            acResponse.cc += reviseResponse.cc;
            if (xi.getDomain().getCurrentDomain().isEmpty()) {
                acResponse.isArcConsistent = false;
                return acResponse;
            }
            if (reviseResponse.domainModified) {
                for (var xk : xi.getNeighbors()) {
                    if (xj == null) {
                        for (var xkConstraint : xk.getAllConstraints()) {
                            if (xkConstraint.arity == 1) {
                                queue.add(new QueueTuple(xkConstraint.getVariables().get(0), xkConstraint));
                            } else {
                                queue.add(new QueueTuple(
                                        xkConstraint.getVariables().get(0),
                                        xkConstraint.getVariables().get(1),
                                        xkConstraint));
                            }
                        }
                    } else if (!xk.getName().equals(xj.getName())) {
                        var sharedConstraint = xk.getSharedConstraint(xj.getName());
                        if (sharedConstraint == null)
                            sharedConstraint = xj.getSharedConstraint(xk.getName());
                        if (sharedConstraint != null) {
                            queue.add(new QueueTuple(xk, xj, sharedConstraint));
                        }

                    }
                }
            }
        }
        acResponse.isArcConsistent = true;

        return acResponse;
    }
}
