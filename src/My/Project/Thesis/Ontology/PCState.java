package My.Project.Thesis.Ontology;


import com.google.gson.Gson;

public class PCState extends ControllerState {
    public float totalProduceLoads =0;
    public float totalStorageLoads =0;
    public float totalGridLoads;
    public int totalMemebersCount=0;
    public ConversationTypes convType;
    public int sizeProducers;
    public int sizeConsumers;
    public int sizeStorages;
    public GridMemberType currentSource;

    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
