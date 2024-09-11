package com.example.year2ca2;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Node start, Node end) {
        if (start == null || end == null || start.equals(end)) return; // Basic validation

        Edge edge = new Edge(start, end);
        start.addEdge(edge);
        end.addEdge(edge);
        edges.add(edge);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    // This method returns an array of neighboring nodes connected to the current node
    public Node[] getNeighbors(Node current) {
        List<Node> neighbors = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.getStart().equals(current)) {
                neighbors.add(edge.getEnd());
            } else if (edge.getEnd().equals(current)) {
                neighbors.add(edge.getStart());
            }
        }

        return neighbors.toArray(new Node[0]);
    }
}
