package My.Project.Thesis.Ontology;


import com.google.gson.Gson;

public class ControllerFact {

    public boolean needsChangeUp;
    public boolean needsChangeDown;
    public boolean isGoodCondition;
    public boolean needsChangeStoage;
    public String ID;
    public boolean needsChangeSource;
    public boolean isacceptedProposal;
    public float maxLoad;
    public boolean needsChangeLoad;
    public boolean leftHolon;
    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
