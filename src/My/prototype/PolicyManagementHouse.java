package My.prototype;

import My.Project.Thesis.Ontology.*;
import jade.core.AID;
import org.kie.api.runtime.rule.FactHandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static My.Project.Thesis.Ontology.ConversationTypes.acceptJoinProposal;

public class PolicyManagementHouse extends PolicyManagementPC {
    HouseControllerAgent b;
    private FactHandle currentJoinProposalHandle;
    private FactHandle currentHolonStateHandle;
    private FactHandle currentacceptLoadProposalHandle;
    private FactHandle currentPushLoadHandle;
    @Override
    public void setup(ControllerAgent controllerAgent, String pFileName) throws IOException {
        super.setup(controllerAgent,pFileName);
        b=(HouseControllerAgent)agent;
    }

    @Override
    protected void InsertFact() {
        super.InsertFact();
        currentJoinProposalHandle = null;
        currentacceptLoadProposalHandle=null;
        currentPushLoadHandle=null;

        if (b.currentJoinProposal != null && b.currentJoinProposal.isUsed == false) {
            currentJoinProposalHandle = kieSession.insert(b.currentJoinProposal);
            b.currentJoinProposal.isUsed = true;

            if(agent.currentState!=null) {
                currentStateHandle = kieSession.insert(agent.currentState);
                agent.currentState.isUsed = true;
            }
        }
        if (b.currentHolonState != null && b.currentHolonState.isUsed == false) {
            currentHolonStateHandle = kieSession.insert(b.currentHolonState);
            b.currentHolonState.isUsed = true;
            if(agent.currentState!=null) {
                currentStateHandle = kieSession.insert(agent.currentState);
                agent.currentState.isUsed = true;
            }
        }
        if(agent.currentacceptLoadProposal!=null && agent.currentacceptLoadProposal.isUsed==false)
        {
            currentacceptLoadProposalHandle= kieSession.insert(agent.currentacceptLoadProposal);
            kieSession.insert(agent.currentState);
            agent.currentacceptLoadProposal.isUsed=true;
        }

        if(agent.currentPushLoad!=null && agent.currentPushLoad.isUsed==false)
        {
            currentPushLoadHandle= kieSession.insert(agent.currentPushLoad);
            agent.currentPushLoad.isUsed=true;
        }

        EndInsertFacts();
    }
    @Override
    public List<Instruction> plan()
    {
        List<Instruction> li=super.plan();
        if( currentStateHandle!=null)//monitoring event
        {
            PCState i = (PCState) kieSession.getObject(currentStateHandle);
            if (fact.needsChangeSource) {

                changeHouseSource(i, li);
            }
            if( i.currentSource == GridMemberType.GRID && i.load>0 &&  i.time>agent.LastEmergentTime) {
               requestEmergent(i,li);
            }
            currentStateHandle=null;
        }
        if (currentJoinProposalHandle!=null)
        {

            ControllerHeadHolonFact hfact=(ControllerHeadHolonFact)kieSession.getObject(currentJoinProposalHandle);

            if(fact.isacceptedProposal)
            {
                boolean accept=true;
                AID newParent=new AID(hfact.propsedFact.ID,false);
                switch(hfact.convType){
                    case acceptJoinProposal:
                        break;
                    case join:

                        for(AID p:agent.Parents)
                        {
                            if(p.equals(newParent))
                                accept=false;
                        }
                        if (accept && hfact.currentFact!=null) {
                            li.add(new Instruction(newParent, hfact.currentFact.toString(), acceptJoinProposal, hfact.currentFact.time));

                        }
                        break;
                }
            }
            else
            {
                li.add(new Instruction(new AID(hfact.propsedFact.ID,false),"",ConversationTypes.rejectJoinProposal,hfact.propsedFact.time));
            }
            currentJoinProposalHandle=null;
        }
        if(currentHolonStateHandle!=null)
        {
            if(fact.leftHolon)
            {
                PCState i = (PCState) kieSession.getObject(currentHolonStateHandle);
                AID pi=agent.getName(i.ID);
                if(agent.Parents.contains(pi)) {
                    agent.RemoveParent(pi);
                }
            }
            currentHolonStateHandle=null;
        }
        if(currentacceptLoadProposalHandle!=null) {
            LoadProposalItem i = (LoadProposalItem) kieSession.getObject(currentacceptLoadProposalHandle);
            if (i.accepted) {
                Instruction r=changeStorage(-i.load,i);
                ((PCState)(agent.currentState)).totalStorageLoads-=i.load;

                if(r==null)
                    i.accepted=false;
                else {
                    li.add(r);
                    li.add(new Instruction(new AID(i.requester, false), i.toString(), ConversationTypes.pushLoad, i.time));
                }
            }


            currentacceptLoadProposalHandle=null;
        }
        if(currentPushLoadHandle!=null) {
            LoadProposalItem i = (LoadProposalItem) kieSession.getObject(currentPushLoadHandle);
            if (i.accepted) {
                Instruction r=changeStorage(i.load,i);

                if(r==null)
                    i.accepted=false;
                else {
                    li.add(r);

                }
            }
            currentPushLoadHandle=null;
        }
        return  li;
    }

    private void requestEmergent(PCState i,List<Instruction> li) {
        for (AID p : agent.Parents)
            li.add(new Instruction(p, i.toString(), ConversationTypes.emergent, i.time));
        agent.LastEmergentTime=i.time;
    }

    private void changeHouseSource(PCState hf, List<Instruction> li) {
        if(hf.currentSource== GridMemberType.Producer)
        {
            for(AID i:agent.Members)
            {
                DeviceState fh=(DeviceState) agent.currentMemberMonitor.get(i).getLast();
                if(fh!=null ) {
                    if ( fh.type == GridMemberType.Producer && fh.lc < 1)
                        li.add(buildDeviceInstruct(CommandTypes.change, (changeStep), i,fh.time));
                    else if (fh.type == GridMemberType.Storage  && fh.lc<1)
                        li.add(buildDeviceInstruct(CommandTypes.change, (changeStep), i,fh.time));
                    else if ( fh.type == GridMemberType.GRID && fh.lc > 0)
                        li.add(buildDeviceInstruct(CommandTypes.change, 0, i,fh.time));
                }else {
                    GridMemberType t=DeviceControllerAgent.GetDevicetype(i.getLocalName());
                    if ( t == GridMemberType.Producer )
                        li.add(buildDeviceInstruct(CommandTypes.change, (changeStep), i,hf.time));
                    else if (t == GridMemberType.Storage  )
                        li.add(buildDeviceInstruct(CommandTypes.change, (changeStep), i,hf.time));
                    else if (t == GridMemberType.GRID )
                        li.add(buildDeviceInstruct(CommandTypes.change, 0, i,hf.time));
                }

            }
            //((ControllerFactHouse)agent.currentMemberMonitor.get(agent.getAID()).getLast()).currentSource=GridMemberType.Producer;
            //agent.currentMemberMonitor.get(agent.getAID()).put(hf);
        }else if(hf.currentSource== GridMemberType.Storage)
        {
            for(AID i:agent.Members)
            {
                DeviceState fh=(DeviceState) agent.currentMemberMonitor.get(i).getLast();
                if(fh!=null) {
                    if (fh.type == GridMemberType.Storage && fh.lc < 1)
                        li.add(buildDeviceInstruct(CommandTypes.change, (changeStep), i,fh.time));
                    else if ((fh.type == GridMemberType.Producer || fh.type == GridMemberType.GRID )&& fh.lc > 0)
                        li.add(buildDeviceInstruct(CommandTypes.change, 0, i,fh.time));
                }else {
                    GridMemberType t=DeviceControllerAgent.GetDevicetype(i.getLocalName());
                    if (t == GridMemberType.Storage  )
                        li.add(buildDeviceInstruct(CommandTypes.change, (changeStep), i,hf.time));
                    else if (t == GridMemberType.Producer || t == GridMemberType.GRID )
                        li.add(buildDeviceInstruct(CommandTypes.change, 0, i,hf.time));
                }
            }
            //((ControllerFactHouse)agent.currentMemberMonitor.get(agent.getAID()).getLast()).currentSource=GridMemberType.Storage;
            //agent.currentMemberMonitor.get(agent.getAID()).put(hf);
        }else if(hf.currentSource== GridMemberType.GRID)
        {
            for(AID i:agent.Members)
            {
                DeviceState fh=(DeviceState) agent.currentMemberMonitor.get(i).getLast();
                if(fh!=null &&fh.type == GridMemberType.GRID && fh.lc<1)
                    li.add(buildDeviceInstruct(CommandTypes.change,(changeStep),i,fh.time));
                else if (fh!=null &&(fh.type == GridMemberType.Producer || fh.type == GridMemberType.Storage )&& fh.lc>0)
                    li.add(buildDeviceInstruct(CommandTypes.change,0,i,fh.time));
                else {
                    GridMemberType t=DeviceControllerAgent.GetDevicetype(i.getLocalName());
                    if (t == GridMemberType.GRID  )
                        li.add(buildDeviceInstruct(CommandTypes.change, (changeStep), i,hf.time));
                    else if (t == GridMemberType.Producer || t == GridMemberType.Storage )
                        li.add(buildDeviceInstruct(CommandTypes.change, 0, i,hf.time));
                }

            }
            //((ControllerFactHouse)agent.currentMemberMonitor.get(agent.getAID()).getLast()).currentSource=GridMemberType.GRID;

            if( hf.time>agent.LastEmergentTime) {
                requestEmergent(hf,li);
            }
        }
    }
    private Instruction changeStorage(float load,LoadProposalItem p) {
        for(AID i:agent.Members)
        {
            ControllerState fh= agent.currentMemberMonitor.get(i).getLast();
            if(fh!=null)
                if (fh.type == GridMemberType.Storage &&
                        ((fh.load > -load && load<=0) ||(load>0))) {
                    if(load>0)
                        agent.printLoad(load + " from "+ fh.ID,"AddloadF",p);
                    return buildDeviceInstruct(CommandTypes.changeStorage, load, i, fh.time);
                }
        }
        return null;
    }

}
