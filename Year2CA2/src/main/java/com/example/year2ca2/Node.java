package com.example.year2ca2;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private int x;
    private int y;
    private List<Edge> edges = new ArrayList<>();

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
