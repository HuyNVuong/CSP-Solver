package csp;

import abscon.instance.components.PDomain;

public class Domain {
    private PDomain domainRef;

    private int[] values;

    public Domain(PDomain domainRef) {
        this.domainRef = domainRef;
        this.values = domainRef.getValues();
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    public boolean contains(int value) {
        return domainRef.contains(value);
    }
}
