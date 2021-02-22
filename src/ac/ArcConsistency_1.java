package ac;

import ac.logic.ArcConsistencyStaticMethods;
import ac.models.AcResponse;
import csp.Constraint;

import java.util.List;

public class ArcConsistency_1 {
    public static AcResponse solve(List<Constraint> constraints) {
        var isDomainModified = false;
        var response = new AcResponse();
        do {
            for (var constraint : constraints) {
                switch (constraint.arity) {
                    case 1:
                        var x = constraint.getVariables().get(0);
                        var xRevised = ArcConsistencyStaticMethods.revise(x, null, constraint, false);
                        isDomainModified |= xRevised.revised;
                        break;
                    case 2:
                        var xi = constraint.getVariables().get(0);
                        var xj = constraint.getVariables().get(1);
                        var xiRevised = ArcConsistencyStaticMethods.revise(xi, xj, constraint, false);
                        if (xiRevised.revised && xi.getDomain().getCurrentDomain().isEmpty()) {
                            response.isArcConsistent = false;
                            response.cc += (xiRevised.cc);
                            response.fVal += (xiRevised.fVal);
                            return response;
                        }
                        var xjRevised = ArcConsistencyStaticMethods.revise(xj, xi, constraint, true);
                        if (xjRevised.revised && xj.getDomain().getCurrentDomain().isEmpty()) {
                            response.isArcConsistent = false;
                            response.cc += (xiRevised.cc + xjRevised.cc);
                            response.fVal += (xiRevised.fVal + xjRevised.fVal);
                            return response;
                        }
                        response.cc += (xiRevised.cc + xjRevised.cc);
                        response.fVal += (xiRevised.fVal + xjRevised.fVal);
                        isDomainModified = xiRevised.revised || xjRevised.revised;
                    default:
                        break;
                }

            }
        } while (isDomainModified);

        response.isArcConsistent = true;
        return response;
    }
}
