public enum Location_Sensor {
    TOP(0), TOP_RIGHT(1), RIGHT(2), BOTTOM_RIGHT(3), DOWN(4), BOTTOM_LEFT(5), LEFT(6), TOP_LEFT(7);

    int type = 0;

    // parameterized constructor to initialize packet_type of sensor (based on enum)
    private Location_Sensor(int exploration_type) {
        this.type = exploration_type;
    }

    // get value of sensor from enum mapping
    public int getTypeVal() {
        return this.type;
    }

    // update value
    public void setVal(int newVal) {
        type = newVal;
    }

};