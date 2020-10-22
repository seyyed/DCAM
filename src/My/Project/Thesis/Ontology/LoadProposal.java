package My.Project.Thesis.Ontology;

import com.google.gson.Gson;
import org.kie.soup.commons.util.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class LoadProposal {
    public List<LoadProposalItem> items;
    public int time;
    public String sender;
    public String ID;
    public boolean selected;

    public  LoadProposal()
    {
        items=new ArrayList<>();
    }
    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
