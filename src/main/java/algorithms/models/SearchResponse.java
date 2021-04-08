package algorithms.models;

import java.util.ArrayList;
import java.util.List;

public class SearchResponse {
    public List<String> orders;
    public List<ArrayList<Integer>> paths;
    public long cc;
    public long nv;
    public long bt;

    public SearchResponse() {
        paths = new ArrayList<>();
    }

    public SearchResponse(List<ArrayList<Integer>> paths, long cc, long nv, long bt) {
        this.paths = paths;
        this.cc = cc;
        this.nv = nv;
        this.bt = bt;
    }

    public SearchResponse(List<ArrayList<Integer>> paths, long cc, long nv, long bt, List<String> variableOrders) {
        this.orders = variableOrders;
        this.paths = paths;
        this.cc = cc;
        this.nv = nv;
        this.bt = bt;
    }
}
