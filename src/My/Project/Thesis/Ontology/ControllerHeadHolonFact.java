package My.Project.Thesis.Ontology;


import com.google.gson.Gson;

public class ControllerHeadHolonFact {
    public PCState currentFact;
    public PCState propsedFact;
    public int time;
    public boolean isUsed;
    public ConversationTypes convType;

    public ControllerHeadHolonFact(PCState propsed, PCState currentState, boolean isUsed, ConversationTypes ct) {
        propsedFact=propsed;
        currentFact=currentState;
        this.isUsed=isUsed;
        convType=ct;
    }

    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
