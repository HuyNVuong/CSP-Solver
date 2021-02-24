package ac.logic;

import abscon.instance.components.*;
import csp.Constraint;

public class ArcConsistencyStaticMethodsTests {
    public static void main(String[] args) {
        var pConstraint = new PExtensionConstraint(
                "test",
                new PVariable[]{
                    new PVariable("v1", new PDomain("d1", new int[]{1,2,3})),
                    new PVariable("v2", new PDomain("d1", new int[]{1,2,3}))
                },
                new PRelation("r1", 3, 3, "conflicts", new int[][]{
                        new int[] {1,2},
                }));
        var constraint = new Constraint(pConstraint);
//        var updatedDomain = ArcConsistencyStaticMethods.revise(constraint);
    }
}
