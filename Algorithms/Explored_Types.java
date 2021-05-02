public enum Explored_Types {
    // Exploration ENUMs
    EMPTY(0), OBSTACLE(1), UN_EMPTY(2), UN_OBSTACLE(3);

    // ENUM selector, set initially to all unexplored
    int type = 2;

    // Constructor for ENUM
    // Required accessibility to be private
    private Explored_Types(int type) {
        this.type = type;
    }

    public static int convertTypeToInt(String a) {
        switch (a) {
        case "UN_OBSTACLE":
            return 3;
        case "UN_EMPTY":
            return 2;
        case "OBSTACLE":
            return 1;
        case "EMPTY":
            return 0;
        }
        return -1;
    };

    // GET method: type
    public int getTypeVal() {
        return this.type;
    }

};