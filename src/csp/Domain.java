package csp;

import abscon.instance.components.PDomain;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Domain {
    private PDomain domainRef;

    private Set<Integer> values;

    public Domain(PDomain domainRef) {
        this.domainRef = domainRef;
        this.values = Arrays.stream(domainRef.getValues()).boxed().collect(Collectors.toSet());
    }

    public Set<Integer> getValues() {
        return values;
    }

    public void removeValue(int value) { values.remove(value); }

    public boolean contains(int value) {
        return domainRef.contains(value);
    }
}
