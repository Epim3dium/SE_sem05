package iscte.se.landmanagement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * This class provides methods to calculate property areas and manage changes in a graph structure of properties.
 */
public class CalcAreas {

    private GraphStructure g;
    private Graph<Property, DefaultEdge> graph;

    /**
     * Constructs a CalcAreas instance with the specified graph.
     *
     * @param graph the graph containing the properties
     */
    public CalcAreas(Graph<Property, DefaultEdge> graph) {
        this.graph = graph;

    }

    /**
     * Constructs a CalcAreas instance with the specified graph structure.
     *
     * @param g the graph structure containing the properties
     */
    public CalcAreas(GraphStructure g) {
        this.g = g;
        this.graph = g.getG();
    }

    /**
     * Converts the vertex set of a graph into a list of properties.
     *
     * @param graph the graph whose vertex set is to be converted
     * @return a list of properties in the graph
     */
    public ArrayList<Property> toList(Graph<Property, DefaultEdge> graph) {
        ArrayList<Property> result = new ArrayList<>(graph.vertexSet());
        return result;
    }

    /**
     * Calculates the Average Area of {@link Property} of a geographic area indicated by the user
     *
     * @param areaT    Name of the area that will be calculated
     * @param areaType Type of the area that will be calculated (Parish/Municipality/Island)
     * @return The Average Area of the {@link Property} considered in the case
     */
    public double calcArea3(String areaT, String areaType) {

        List<Property> filteredProperties = graph.vertexSet().stream().filter(property -> matchesLocal(property, areaT, areaType)).toList();


        if (filteredProperties.isEmpty()) {
            return 0;
        }
        double sum = 0;


        for (Property p : filteredProperties) {
            sum += p.getShapeArea();

        }


        return sum / filteredProperties.size();
    }

    /**
     * Calculates the Average Area of {@link Property} of  a geographic area indicated by user
     * considering that if adjacent properties have the same owner, they are just one
     *
     * @param input    Name of the area that will be calculated
     * @param areaType Type of the area that will be calculated (Parish/Municipality/Island)
     * @return The Average Area of the {@link Property} considered in the case
     */

    public double calcArea4(String input, String areaType) {
        List<Property> filteredProperties = graph.vertexSet().stream().filter(property -> matchesLocal(property, input, areaType)).toList();
        Set<Property> visited = new HashSet<>();
        List<Double> aggregatedArea = new ArrayList<>();

        for (Property property : filteredProperties) {
            if (!visited.contains(property)) {
                double totalA = exploreConnectedComp(graph, property, visited);
                aggregatedArea.add(totalA);

            }
        }

        double avg = aggregatedArea.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return avg;
    }

    /**
     * Checks whether a property matches a specific geographic area based on its type.
     *
     * @param property the property to check
     * @param areaT    the name of the geographic area
     * @param type     the type of the area (Parish/Municipality/Island)
     * @return true if the property matches the area, false otherwise
     */
    private boolean matchesLocal(Property property, String areaT, String type) {
        return switch (type) {
            case "Freguesia" -> property.getParish().equals(areaT);
            case "Municipio" -> property.getMunicipality().equals(areaT);
            case "Ilha" -> property.getIsland().equals(areaT);
            default -> false;
        };

    }

    /**
     * Explores a connected component of properties in the graph that are owned by the same owner,
     * starting from a given property.
     *
     * @param graph    the graph of properties
     * @param property the starting property
     * @param visited  a set of already visited properties
     * @return the total area of the connected component
     */
    private double exploreConnectedComp(Graph<Property, DefaultEdge> graph, Property property, Set<Property> visited) {
        double totalArea = 0;
        Queue<Property> queue = new LinkedList<>();
        queue.add(property);
        visited.add(property);

        while (!queue.isEmpty()) {
            Property p = queue.poll();
            totalArea += p.getShapeArea();

            for (DefaultEdge edge : graph.edgesOf(p)) {
                Property neighbor = graph.getEdgeTarget(edge);
                if (!visited.contains(neighbor) && neighbor.getOwnerID() == p.getOwnerID()) {
                    visited.add(neighbor);
                    queue.add(neighbor);

                }
            }
        }

        return totalArea;
    }

    /**
     * Changes the ownership of two properties in the graph and returns the updated graph structure.
     *
     * @param t  the first property to update
     * @param s  the second property to update
     * @param ns the new owner ID for the first property
     * @param mt the new owner ID for the second property
     * @return the updated graph structure
     */
    public GraphStructure changeProperty(Property t, Property s, int ns, int mt) {
        ArrayList<Property> n = toList(graph);
        for (Property i : n) {
            if (i.equals(t)) {
                i.setOwnerID(ns);
            } else if (i.equals(s)) {
                i.setOwnerID(mt);
            }

        }
        return new GraphStructure(n, 4);

    }

}
