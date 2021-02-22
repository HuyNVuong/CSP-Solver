package csp;

import abscon.instance.components.PDomain;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Domain {
    private PDomain domainRef;

    private Set<Integer> initialDomain;

    private Set<Integer> currentDomain;

    public Domain(PDomain domainRef) {
        this.domainRef = domainRef;
        this.initialDomain = Arrays.stream(domainRef.getValues()).boxed().collect(Collectors.toSet());
        this.currentDomain = Arrays.stream(domainRef.getValues()).boxed().collect(Collectors.toSet());
    }

    public Set<Integer> getInitialDomain() { return initialDomain; }

    public Set<Integer> getCurrentDomain() {
        return currentDomain;
    }

    public void removeValue(int value) { currentDomain.remove(value); }

    public boolean contains(int value) {
        return domainRef.contains(value);
    }
}
