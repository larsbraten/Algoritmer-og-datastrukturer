package AlgDat.Graphs;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class supports mapping of nodes, and methods to establish edges between them.
 * Graph may be traversed by deepFirstSearching, and consequently supports Kosaraju's algorithm
 * for tracing the strongly connected components.
 */

public class Graph<T> {

    private final HashMap<T, Node<T>> nodes;

    public Graph(int expectedSize){
        nodes = new HashMap<>(expectedSize * 2);
    }

    public int size(){
        return nodes.size();
    }

    public boolean addNode(T ID){
        if(nodes.containsKey(ID))
            return false;
        nodes.put(ID, new Node<>(ID));
        return true;
    }

    public boolean addEdge(T startID, T endID){
        if(startID.equals(endID)) return false;
        Node<T> headNode = nodes.get(startID);
        Node<T> tailNode = nodes.get(endID);

        if(tailNode == null){
            tailNode = new Node<>(endID);
            nodes.put(endID, tailNode);
        }

        if(headNode == null){
            headNode = new Node<>(startID);
            nodes.put(startID, headNode);
        }
        return headNode.addConnection(tailNode);
    }

    public boolean removeNode(T ID){
        return nodes.remove(ID, nodes.get(ID));
    }

    public boolean removeEdge(T startID, T endID){
        Node<T> headNode = nodes.get(startID);
        Node<T> tailNode = nodes.get(endID);

        if(headNode != null && tailNode != null){
            return headNode.removeConnection(tailNode);
        }
        return false;
    }

    public Graph<T> transposed(){
        Graph<T>graph = new Graph<>(size());

        for (Node<T> value : nodes.values()) {
            graph.addNode(value.ID);
            for (Node<T> node : value) {
                graph.addEdge(node.ID, value.ID);
            }
        }
        return graph;
    }

    /**
     * Method uses Kosaraju's algorithm to find the strongly connected components
     * within this graph.
     * @return List of every component, with their corresponding node id.
     */

    public List<String> stronglyConnectedComponents(){
        Deque<Node<T>> stack = new LinkedBlockingDeque<>();
        HashMap<T, Boolean> traversed = new HashMap<>(nodes.size() * 2);

        for (Node<T> value : nodes.values()) {
            deepFirstSearch(value
                    , nodeIf -> traversed.get(nodeIf.ID) == null
                    , nodeOnce -> {}
                    , nodeEach -> traversed.put(nodeEach.ID, true)
                    , stack::push);
        }

        LinkedList<StringBuilder> scc = new LinkedList<>();
        Graph<T> transposedGraph = transposed();

        while (!stack.isEmpty()){
            Node<T> value = stack.pop();
            deepFirstSearch(transposedGraph.nodes.get(value.ID)
                    , nodePredicate -> traversed.get(nodePredicate.ID)
                    , nodeOnce -> scc.add(new StringBuilder())
                    , nodeEach -> {
                        traversed.put(nodeEach.ID, false);
                        scc.getLast().append("<").append(nodeEach.ID).append(">");
                    }
                    , nodeAfter -> {});
        }
        return scc.stream().map(StringBuilder::toString).collect(Collectors.toList());
    }

    /**
     * Recursive method to traverse the graph. The implementation is partially
     * abstract for more programmer-freedom.
     *
     * @param current Node to traverse from.
     * @param traverseIf Condition for traversing the node.
     * @param doOnce Invoked on first method call if condition i satisfied, i.e. ignores recursive calls afterwards.
     * @param doEach Invoked on every method call if condition i satisfied, i.e. first call and each recursive call.
     * @param doAfter Invoked if condition is satisfied and dfs is done with a particular node.
     */

    public  void deepFirstSearch(Node<T> current
            , Predicate<Node<T>> traverseIf
            , Consumer<Node<T>> doOnce
            , Consumer<Node<T>> doEach
            , Consumer<Node<T>> doAfter){

        if(traverseIf.test(current)){
            doOnce.accept(current);
            doEach.accept(current);
            for (Node<T> node : current) {
                deepFirstSearch(node, traverseIf, nop -> {}, doEach, doAfter);
            }
            doAfter.accept(current);
        }
    }

    @Override
    public String toString() {
        return nodes.values()
                .stream()
                .map(node -> node + "\n")
                .reduce(String::concat)
                .orElse("<Empty graph>\n");
    }

    /**
     * Implementation for nodes within this graph.
     */

    static class Node<T> implements Iterable<Node<T>> {

        private final T ID;
        private final LinkedList<Node<T>> connections;

        @SafeVarargs
        Node(T ID, Node<T>... connections){
            this.ID = ID;
            this.connections = Arrays
                    .stream(connections)
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        boolean addConnection(Node<T> that){
            boolean isAbsent = connections
                    .stream()
                    .noneMatch(this::equals);

            if(isAbsent){
                connections.add(that);
                return true;
            }
            return false;
        }

        boolean removeConnection(Node<T> that){
            return connections.removeFirstOccurrence(that);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node<?> node = (Node<?>) o;

            return ID.equals(node.ID);
        }

        @Override
        public int hashCode() {
            return ID.hashCode();
        }

        @Override
        public String toString() {
            return ID + " connected to: " + connections.stream()
                    .map(node -> "<" + node.ID + ">")
                    .reduce(String::concat)
                    .orElse("<Nothing>");
        }

        @Override
        public Iterator<Node<T>> iterator() {
            return connections.iterator();
        }
    }
}
