package My.Project.Thesis.Ontology;

public enum DeviceNames {
    TVID(8),
    LightsID(7),
    DishWasherID(6),
    DryerID(5),
    SolarID(4),
    BatteryID(3),
    GridID(1),
    W1ID(9),
    Fridge2ID(24),
    HotWaterSystemID(25),
    WashingID(27),
    OvenID(26),
    AirconID(20),
    ComputerID(21),
    FreezerID(22),
    FridgeID(23),
    BatteryChargeID(2);
    private final int value;
    DeviceNames(final int newValue){
        value=newValue;
    }
    public int getValue(){
        return value;
    }
}
