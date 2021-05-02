import java.util.*;

public class A_star_search {
    // A* variables
    // Default accessibility: Private
    Node initial_node;
    Node end_node;
    Robot robot;
    int cost;

    // A* constructor to initialise values
    public A_star_search(Node start, Node end) {
        initial_node = start;
        end_node = end;
        cost = 0;
    }

    // GET method: Cost
    public int getCost() {
        return cost;
    };

    // Calculate fastest path from start to goal node
    public Stack<Node> retrieve_fastest_path() {
        return findPath(initial_node, end_node);
    }

    // Return path of nodes to given goal node as stack
    protected Stack<Node> constructPath(Node node) {
        Stack path = new Stack();

        while (node.parent_node_in_path != null) {
            path.push(node);
            node = node.parent_node_in_path;
            cost++;
        }

        return path;
    }

    // Find robot direction
    public Facing getRobotDirection(Node node, Facing face_dir) {

        Node parent_node = (Node) node.parent_node_in_path;

        if (parent_node == null) {
            return face_dir;
        }

        // if nodeA of current node < nodeA of parent, then move to left
        if (node.compareX(parent_node) == 1) {
            return Facing.LEFT;
        } else if (node.compareX(parent_node) == -1) {
            return Facing.RIGHT;
        } else if (node.compareY(parent_node) == 1) {
            return Facing.DOWN;
        } else if (node.compareY(parent_node) == -1) {
            return Facing.UP;
        }

        return null;
    }

    // Nested class: PriorityList
    public static class PriorityList extends LinkedList {

        // Add nodes to Priority List
        public void add(Comparable object) {

            // Implement Insertion Sort
            for (int i = 0; i < size(); i++) {
                if (object.compareTo(get(i)) <= 0) {
                    add(i, object);
                    return;
                }
            }
            addLast(object);
        }

    }

    // Discover fastest path from start to goal node
    public Stack<Node> findPath(Node initial_node, Node end_node) {

        // TODO: check for hardcoded value 3
        int size = 3;
        boolean is_start_node = true;

        PriorityList priority_list = new PriorityList();
        LinkedList linked_list = new LinkedList();

        // Initialise start node
        initial_node.total_path_cost = 0;
        initial_node.cost_estimated_to_goal_node = initial_node.pathCost(end_node);
        initial_node.parent_node_in_path = null;
        priority_list.add(initial_node);

        // Begin exploration of grid map
        while (!priority_list.isEmpty()) {

            // Get node from head of list
            Node node = (Node) priority_list.removeFirst();

            // Determine if node is Start node
            if (node == initial_node)
                is_start_node = true;
            else
                is_start_node = false;

            ((Node) node).setFacing();

            // Determine if node is goal node
            if (node == end_node) {
                return constructPath(end_node);
            }

            // Get list of node neighbours
            List neighbors_list = node.getNeighbors();

            // Iterate through list of node neighbours
            for (int i = 0; i < neighbors_list.size(); i++) {

                // Extract neighbour node information
                Node node_neighbour = (Node) neighbors_list.get(i);
                boolean isOpen = priority_list.contains(node_neighbour);
                boolean isClosed = linked_list.contains(node_neighbour);
                boolean isObs = (node_neighbour).isObs();
                int clearance = node_neighbour.getClearance();
                float total_path_cost = node.getCost(node_neighbour, end_node, is_start_node) + 1;

                // Check 1. if node neighbours have not been explored OR 2. if shorter path to
                // neighbour node exists
                if ((!isOpen && !isClosed) || total_path_cost < node_neighbour.total_path_cost) {
                    node_neighbour.parent_node_in_path = node;
                    node_neighbour.total_path_cost = total_path_cost;
                    node_neighbour.cost_estimated_to_goal_node = node_neighbour.pathCost(end_node);

                    // Add neighbour node to priority_list if 1. node not in
                    // priority_list/linked_list AND 2.
                    // robot can reach
                    if (!isOpen && !isObs && size == clearance) {
                        priority_list.add(node_neighbour);
                    }
                }
            }
            linked_list.add(node);
        }

        // priority_list empty; no path found

        return new Stack<Node>();
    }

}
