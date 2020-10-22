package My.prototype;

import My.Project.Thesis.Ontology.ConversationTypes;
import My.Project.Thesis.Ontology.LoadProposal;
import My.Project.Thesis.Ontology.LoadProposalItem;
import My.Project.Thesis.Ontology.PCState;
import jade.core.AID;
import org.kie.api.runtime.rule.FactHandle;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PolicyManagementHolon extends PolicyManagementPC {
    private FactHandle currentEmergentRequestHandle;
    private FactHandle currentAcceptedJoinRequestHandle;
    HolonControllerAgent b;

    @Override
    public void setup(ControllerAgent controllerAgent, String pFileName) throws IOException {
        super.setup(controllerAgent,pFileName);
        b=(HolonControllerAgent)agent;
    }
    @Override
    protected void InsertFact() {
        super.InsertFact();
        currentEmergentRequestHandle=null;
        currentAcceptedJoinRequestHandle = null;

        HolonControllerAgent b=(HolonControllerAgent) agent;
        for (Map.Entry<AID, PCState> entry : b.currentEmergentRequest.entrySet()) {
            PCState c=entry.getValue();
            if(c!=null && c.isUsed ==false)
            {
                c.isUsed=true;
                currentEmergentRequestHandle = kieSession.insert(c);
            }
        }
        for (Map.Entry<AID, PCState> entry : b.currentAcceptedJoinRequest.entrySet()) {
            PCState c=entry.getValue();
            if(c!=null && c.isUsed ==false)
            {
                c.isUsed=true;
                currentAcceptedJoinRequestHandle = kieSession.insert(c);
            }
        }
        EndInsertFacts();
    }
    @Override
    public List<Instruction> plan()
    {
        List<Instruction> li=super.plan();

        if(fact.isacceptedProposal)
        {
            if(currentAcceptedJoinRequestHandle!=null)
            {
                PCState jr=(PCState) kieSession.getObject(currentAcceptedJoinRequestHandle);
                AID sender=new AID(jr.ID,false);
                if(!agent.Members.contains(sender)) {
                    agent.AddNewMember(sender);
                    li.add(new Instruction(sender, "", ConversationTypes.confirmJoin, jr.time));
                }
                currentAcceptedJoinRequestHandle=null;
            }
            else if(currentEmergentRequestHandle!=null && b.currentState!=null)
            {
                PCState er=(PCState) kieSession.getObject(currentEmergentRequestHandle);
                float need=er.load;
                float minR=10;
                LoadProposal p=new LoadProposal();
                p.time=er.time;
                p.sender=agent.getLocalName();

                if(need>0 && ( (PCState)(b.currentState)).totalStorageLoads > need)
                {

                    for(AID m :agent.Members)
                    {
                        PCState fm=(PCState) agent.currentMemberMonitor.get(m).getLast();
                        if( fm!=null && fm.totalStorageLoads>minR+fm.load && er!=null && !m.getLocalName().equals(er.ID))
                        {
                            float availible=fm.totalStorageLoads-minR-fm.load;
                            float amount= need>availible?availible:need;
                            need-=amount;
                            p.items.add(new LoadProposalItem(m.getLocalName(),amount));
                            if(need<=0)
                                break;
                        }
                    }

                    li.add(new Instruction(new AID(er.ID,false), p.toString() , ConversationTypes.emergentResponse, er.time));
                }else{
                    li.add(new Instruction(new AID(er.ID,false), p.toString() , ConversationTypes.emergentResponse, er.time));
                }
                currentEmergentRequestHandle=null;
            }

        }
        else if(currentAcceptedJoinRequestHandle!=null )
        {
            PCState er=(PCState) kieSession.getObject(currentEmergentRequestHandle);
            AID sender=new AID(er.ID,false);
            li.add(new Instruction(sender,"",ConversationTypes.rejectJoinProposal,er.time));
            currentAcceptedJoinRequestHandle=null;
        }
        return li;
    }



}
