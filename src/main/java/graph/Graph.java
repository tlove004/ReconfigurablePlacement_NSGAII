package graph;

import operation.Operation;

import java.util.List;
import java.util.Map;

/**
 * Created by Tyson Loveless
 *
 */
public class Graph {
    private List<Operation> vertices;
    private List<Edge<Operation, Operation>> edges;

    public Graph(List<Operation> vertices,
                 List<Edge<Operation, Operation>> edges) {
        this.vertices = vertices;
        this.edges = edges;
    }

    public List<Operation> getVertices() {
        return vertices;
    }

    public List<Edge<Operation, Operation>> getEdges() {
        return edges;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vertices: [");
        sb.append(getVertices().get(0).getID());
        for (int i = 1; i < getVertices().size(); i++) {
            sb.append(", " + getVertices().get(i).getID());
        }
        sb.append("]\nEdges:\n" + getEdges().toString());
        return sb.toString();
    }
}
