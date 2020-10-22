package My.prototype;

import My.Project.Thesis.Ontology.*;
import jade.core.AID;
import org.kie.api.runtime.rule.FactHandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static My.Project.Thesis.Ontology.ConversationTypes.acceptJoinProposal;

public class PolicyManagementPC extends PolicyManagementService {
    private List<FactHandle> LoadPropsalHandle;

    int lastChangeDownStepTime=0;
    int downstepTimes=0;


    @Override
    protected void InsertFact() {
        InitInsertFacts();

        LoadPropsalHandle.clear();

        if (agent.currentState != null && agent.currentState.isUsed == false) {
            currentStateHandle = kieSession.insert(agent.currentState);
            agent.currentState.isUsed = true;
        }

        if(agent.currentLoadProposals!=null && agent.currentLoadProposals.size()>0 && agent.currentLoadProposals.size()==agent.Parents.size())
            for (LoadProposal l :agent.currentLoadProposals   ) {
                l.selected=false;
                LoadPropsalHandle.add( kieSession.insert(l));
                kieSession.insert(agent.currentState);
            }



    }

    @Override
    public void setup(ControllerAgent controllerAgent, String pFileName) throws IOException {
        super.setup(controllerAgent,pFileName);
        LoadPropsalHandle=new ArrayList<>();
    }

    @Override
    public boolean canDoAdaption() {
        return false;
    }
    private void changePCDown(List<Instruction> li) {
        int fp=agent.upLevel==0?1:agent.upLevel;
        PCState c= (PCState)agent.currentState;

        float newMAx=agent.maxLoad-(downStep*downstepTimes);
        if(newMAx <=0) {
            newMAx = 0;
            downstepTimes--;
        }
        if (lastChangeDownStepTime<c.time) {
            downstepTimes++;
            lastChangeDownStepTime=c.time;
        }

        for(AID i:agent.Members)
        {
            ControllerState fh= agent.currentMemberMonitor.get(i).getLast();

            if(fh!=null)
                if(fh.type == GridMemberType.Consumer  && fh.priority>=fp) {
                    li.add(buildDeviceInstruct(CommandTypes.changeMax,newMAx,i,fh.time));
                }

        }
        agent.upLevel=fp-1;
    }

    private void changePCUp( List<Instruction> li) {

        int fp=agent.upLevel>4?4:agent.upLevel;
        PCState c= (PCState)agent.currentState;

        if (lastChangeDownStepTime<c.time) {
            downstepTimes--;
            lastChangeDownStepTime=c.time;
            if(downstepTimes<0)
                downstepTimes=0;
        }
        float newMAx=agent.maxLoad-(downStep*downstepTimes);
        if(newMAx <=0) {
            newMAx = 0;
            downstepTimes--;
        }

        for (AID i : agent.Members) {
            ControllerState fh = agent.currentMemberMonitor.get(i).getLast();
            if (fh != null)
                if (fh.type == GridMemberType.Consumer  && fh.priority <= fp)
                    li.add(buildDeviceInstruct(CommandTypes.changeMax, newMAx, i,fh.time));
        }

        agent.upLevel=fp+1;
    }
    protected Instruction buildDeviceInstruct(CommandTypes cm, float params, AID recv,int time) {
        DeviceInstruct instruct=new DeviceInstruct(recv.getLocalName(), cm,params,time);
        return new Instruction(recv,instruct.toString(),ConversationTypes.instr,time);
    }
    @Override
    public List<Instruction> plan() {
        List<Instruction> li=new ArrayList<Instruction>();

        if( currentStateHandle!=null)//monitoring event
        {
                if (fact.needsChangeUp)
                    changePCUp(li);
                else if (fact.needsChangeDown)
                    changePCDown(li);
        }

        if( LoadPropsalHandle.size()>0)
        {
            for (FactHandle f:LoadPropsalHandle ) {
                LoadProposal l= (LoadProposal)kieSession.getObject(f);
                if(l.selected)
                    for (LoadProposalItem i :l.items) {
                        i.time=l.time;
                        i.requester= l.ID;
                        i.accepted=false;
                        li.add(new Instruction(new AID(i.id, false), i.toString(), ConversationTypes.acceptLoadProposal, l.time));

                    }
            }
            LoadPropsalHandle.clear();
        }

        return li;
    }


}
