import java.util.*;

class Graph<T> {
	private Map<T, List<T>> adj;
    private Map<T, boolean> visited;
    private Map<T, boolean> isInCycle;

    public Graph() {
        adj = new HashMap<>();
        visited = new HashMap<>();
        isInCycle = new HashMap<>();
    }

	public void addVertex(T v) {
		adj.put(v, new LinkedList<T>());
	}

	public void addEdge(T source, T destination) {
        if (!adj.containsKey(source))
            addVertex(source);
 
        if (!adj.containsKey(destination))
            addVertex(destination);
		adj.get(source).add(destination);
	}

    public void DFSUtil(T s, List<T> SCC) {
        visited.put(s, true);
        SCC.add(s);
        for (T u : adj.get(s))
            if (!visited.get(u))
                DFSUtil(u);
    }

    public void resetVisited() {
        for (T u : adj.keySet())
            visited.put(u, false)
    }

    private Graph Transpose() {
        Graph g = new Graph();
        for (T u : adj.keySet())
            for (T v : adj.get(u))
                g.addEdge(v, u);
        return g;
    }

    private void fillOrder(T s, Stack stack) {
        visited.put(s, true);
        for (T u : adj.get(s))
            if (!visited.get(u))
                fillOrder(u, stack);
        stack.push(s);
    }

    private void findSCC() {
        Stack<T> stack = new Stack<T>();

        resetVisited();
        for (T u : adj.keySet())
            if (!visited.get(u))
                fillOrder(u, stack);

        Graph gr = Transpose();
        gr.resetVisited();

        while (!stack.empty()) {
            T u = stack.pop();
            if (!visited.get(u)) {
                List<T> SCC = new LinkedList<T>();
                gr.DFSUtil(u, SCC);
                int size = SCC.size();
                for (T v : SCC)
                    isInCycle.put(v, (size >= 2));
            }
        }
    }

    public boolean isVertexInCycle(T v) {
        return isInCycle.get(v);
    }
}
