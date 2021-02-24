package ac.models;

public class ReviseResponse {
    public int cc;
    public boolean domainModified;
    public int fVal;

    public ReviseResponse(int cc, boolean domainModified, int fVal) {
        this.cc = cc;
        this.domainModified = domainModified;
        this.fVal = fVal;
    }
}
