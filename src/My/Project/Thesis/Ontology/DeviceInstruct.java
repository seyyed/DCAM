package My.Project.Thesis.Ontology;

import com.google.gson.Gson;

public class DeviceInstruct {
    public final int time;
    public float params;
    public final CommandTypes cmd;
    public final String reciverID;
    public boolean isUsed;

    public DeviceInstruct(String recvLocalName, CommandTypes cmd, float params, int time) {
        this.reciverID=recvLocalName;
        this.cmd=cmd;
        this.params=params;
        this.time=time;
    }
    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
