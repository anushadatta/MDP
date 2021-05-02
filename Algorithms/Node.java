import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable {

    // Node variables
    Node left;
    Node right;
    Node up;
    Node down;
    Node parent_node_in_path;
    List list_of_neighbors = new ArrayList<Node>();

    // Cost of path computation
    float tot_cost;
    float heuristic_cost;

    // Grid variables
    boolean isObs;
    int space;

    // Directional/Positional coordinates
    Facing face_dir;
    final int x;
    final int y;

    // Compare node costs
    public int compareTo(Object other_node) {
        float current_value = this.getCost();
        float other_value = ((Node) other_node).getCost();

        float v = current_value - other_value;
        return (v > 0) ? 1 : (v < 0) ? -1 : 0;
    }

    // GET method: Cost
    public float getCost() {
        return tot_cost + heuristic_cost;
    }

    // Initialise node coordinates
    public Node() {
        x = 0;
        y = 0;
    }

    // Assign node coordinates
    public Node(int x_coordinate, int y_coordinate) {
        this.x = x_coordinate;
        this.y = y_coordinate;
    }

    public void addNeighbors(Node node) {
        list_of_neighbors.add(node);
    }

    // Compute path cost
    public float getCost(Node node, Node end_node, boolean is_start_node) {
        return this.tot_cost + getWeight(is_start_node, node);
    }

    public List getNeighbors() {
        return list_of_neighbors;
    }

    // Compute node cost
    public float pathCost(Node node) {
        Node goal = (Node) node;

        float dx = Math.abs(this.x - goal.x);
        float dy = Math.abs(this.y - goal.y);
        return (dx + dy);
    }

    // Compute node cost
    public float getWeight(boolean is_start_node, Node some_node) {
        Node node = (Node) some_node;
        setFacing();

        if ((compareY(node) == 1 && face_dir == Facing.UP || compareX(node) == 1 && face_dir == Facing.RIGHT
                || compareX(node) == -1 && face_dir == Facing.LEFT || compareY(node) == -1 && face_dir == Facing.DOWN)
                && !is_start_node) {
            return 100;
        }
        return 1500;
    }

    public void setFacing(Facing face) {
        this.face_dir = face;
    }

    // Initialise robot's orientation
    public void setFacing() {

        if (this.parent_node_in_path == null) {
            this.face_dir = Facing.RIGHT;
            return;
        }

        else if (compareX((Node) this.parent_node_in_path) == -1) {
            this.face_dir = Facing.RIGHT;
        } else if (compareY((Node) this.parent_node_in_path) == 1) {
            this.face_dir = Facing.DOWN;
        } else if (compareX((Node) this.parent_node_in_path) == 1) {
            this.face_dir = Facing.LEFT;
        } else if (compareY((Node) this.parent_node_in_path) == -1) {
            this.face_dir = Facing.UP;
        }
    }

    public int getClearance() {
        return space;
    }

    public void setIfClear(int space) {
        this.space = space;
    }

    // Grid map navigation methods
    public void setObs(boolean val) {
        this.isObs = val;
    }

    public boolean isObs() {
        return isObs;
    }

    // Compare node coordinates
    public int compareX(Node node) {
        return node.x > this.x ? 1 : node.x < this.x ? -1 : 0;
    }

    public int compareY(Node node) {
        return node.y > this.y ? 1 : node.y < this.y ? -1 : 0;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setDown(Node down) {
        this.down = down;
    }

    public Node getDown() {
        return down;
    }

    public Node getUp() {
        return up;
    }

    // GET methods: Directions
    public Node getLeft() {
        return left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public void setUp(Node up) {
        this.up = up;
    }

    // GET methods: X & Y coordinates
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
