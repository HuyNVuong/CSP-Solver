package ac.models;

public class ReviseResponse {
    public int cc;
    public boolean revised;
    public int fVal;

    public ReviseResponse(int cc, boolean revised, int fVal) {
        this.cc = cc;
        this.revised = revised;
        this.fVal = fVal;
    }
}
