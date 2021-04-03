package algorithms.models;

public class AcResponse {
    public int cc;
    public boolean isArcConsistent;
    public int fVal;
    public long cpuTime;

    public AcResponse() {
        cc = 0;
        isArcConsistent = false;
        fVal = 0;
        cpuTime = 0L;
    }
}
