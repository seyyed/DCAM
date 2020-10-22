package My.Project.Thesis.Ontology;

import com.google.gson.Gson;
import jade.core.AID;

public class LoadProposalItem {
    public String id;
    public float load;
    public int time;
    public String requester;
    public boolean accepted;
    public boolean isPush;
    public boolean isUsed;

    public LoadProposalItem(String id, float amount) {
        this.id=id;
        this.load=amount;
    }
    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
