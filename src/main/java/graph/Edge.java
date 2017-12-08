package graph;

import operation.Operation;

/**
 * Created by Tyson Loveless
 *
 * Edge class is simply a Pair of vertices u and v; optional cost for weighted edges
 *
 * @param <u>
 * @param <v>
 */
public class Edge<u, v> {
    private final u u_op;
    private final v v_op;
    private Integer cost;

    public Edge(u u_op, v v_op) {
        this.u_op = u_op;
        this.v_op = v_op;

    }

    public Edge(u u_op, v v_op, Integer cost) {
        this.u_op = u_op;
        this.v_op = v_op;
        this.cost = cost;
    }

    public u getU() { return u_op; }
    public v getV() { return v_op; }
    public int getCost() {
        if (cost != null) {
            return cost;
        }
        else {
            return -1;
        }
    }

    @Override
    public int hashCode() { return u_op.hashCode() ^ v_op.hashCode(); }

    @Override //we only care if vertices are the same -- ignore potential costs
    public boolean equals(Object o) {
        if (!(o instanceof Edge)) return false;
        Edge edgeo = (Edge) o;
        return this.u_op.equals(edgeo.getU()) &&
                this.v_op.equals(edgeo.getV());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(" + ((Operation)u_op).getID() + ", " + ((Operation)v_op).getID() + ")");
        if (this.cost != null) {
            sb.append(" with cost: " + this.cost);
        }
        return sb.toString();
    }
}
