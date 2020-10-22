package My.Project.Thesis.Ontology;

import jade.core.AID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class requestRecord {
    public Map<AID,supplyRequest> requests = new HashMap<>();
    public void AddRequest(AID recID, supplyRequest r){
        requests.put(recID,r);
    }
    public boolean isAllrequestsAccepted()
    {
       return  false;
    }

    public void setRequestAccepted(AID sender, float amount) {
        requests.get(sender).setAccepted(true);
        requests.get(sender).setValue(amount);
    }

    public boolean isAllpropsalsAccepted() {

        for (Map.Entry<AID,supplyRequest> pair : requests.entrySet()){
            if(! pair.getValue().isAccepted())
                return false;
        }
        return true;
    }
}
