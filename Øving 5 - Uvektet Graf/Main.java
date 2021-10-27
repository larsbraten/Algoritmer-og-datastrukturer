package AlgDat.Graphs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {

        String graphName;

        System.out.println("Select graph by number: \n" +
                "1. L7G1 - One component only.\n" +
                "2. L7G2 - Random graph.\n" +
                "3. L7g5 - Many components.\n" +
                "4. L7G6 - Graph from page 188 in course book.\n" +
                "5. L7Skandinavia - Road map limited to 1000 edges.");
        switch (args.length != 1 ? new Scanner(System.in).nextInt() : Integer.parseInt(args[0])){
            case 1:
                // L7g1 - Graf med én komponent
                graphName = "L7g1";
                break;
            case 2:
                // L7g2 - Tilfeldig graf
                graphName = "L7g2";
                break;
            case 3:
                // L7g5 - Mange komponenter
                graphName = "L7g5";
                break;
            case 4:
                // L7g6 - Graf fra side 188
                graphName = "L7g6";
                break;
            case 5:
                // L7Skandinavia -  Veikart (will not load whole everything to prevent stack overflow.
                graphName = "L7Skandinavia";
                break;
            default:
                throw new IllegalArgumentException("Choose a valid number.");
        }

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        new URL("http://www.iie.ntnu.no/fag/_alg/uv-graf/" + graphName).openStream()));

        LinkedList<Tuple<Integer, Integer>> data = bufferedReader
                .lines()
                .limit(1000)
                .map(string -> string.trim().replaceAll(" +", " "))
                .map(string -> string.trim().replaceAll("\t+", " "))
                .map(string -> string.split(" "))
                .map(strings -> new Tuple<>(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])))
                .collect(Collectors.toCollection(LinkedList::new));

        Tuple<Integer,Integer> metaData = data.pop();
        Graph<Integer> graph;

        if(graphName.equals("L7Skandinavia")){
            graph = new Graph<>(data.size());
        } else{
            graph = new Graph<>(metaData.getA());

            for(int i = 0; i < metaData.getA(); i++)
                graph.addNode(i);
        }

        data.forEach(edge -> graph.addEdge(edge.getA(), edge.getB()));

        List<String> stronglyConnectedComponents = graph.stronglyConnectedComponents();

        System.out.println("\n" + graphName + " har følgende kanter: \n\n" + graph + "\n" +
                "Grafen " + graphName + " har " + stronglyConnectedComponents.size() + " sterkt " +
                "sammenhengende komponenter.\n\n" +
                "Komponent      Noder i komponenten");

        for (int i = 0; i < stronglyConnectedComponents.size(); i++) {
            System.out.println(i + 1 + "\t\t\t\t" + stronglyConnectedComponents.get(i));
        }


    }
}
