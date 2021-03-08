package bt.models;

import java.util.ArrayList;
import java.util.List;

public class BtResponse {
    public List<ArrayList<Integer>> paths;
    public long cc;
    public long nv;
    public long bt;

    public BtResponse() {
        paths = new ArrayList<>();
    }

    public BtResponse(List<ArrayList<Integer>> paths, long cc, long nv, long bt) {
        this.paths = paths;
        this.cc = cc;
        this.nv = nv;
        this.bt = bt;
    }
}
