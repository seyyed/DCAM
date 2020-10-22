package My.Project.Thesis.Ontology;

import com.google.gson.Gson;

public class DeviceState extends ControllerState {
    public float lc;

    public DeviceState(String ID, String t, String value,String LC) {
        time=Integer.parseInt(t);
        load=Float.parseFloat(value);
        lc=Float.parseFloat(LC);
        this.ID=ID;
    }

    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
