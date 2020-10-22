package My.prototype;


import My.Project.Thesis.Ontology.*;
import jade.core.AID;

import java.util.HashMap;
import java.util.Map;


public class HouseControllerAgent extends ControllerAgent {


    public ControllerHeadHolonFact currentJoinProposal;
    public  PCState currentHolonState;

    @Override
    protected void UpdateStatus(AID sender, String content, ConversationTypes ct) {
        switch(ct)
        {
            case monitoring:
                DeviceState d=null;
                try {
                    d = fromJson(content, DeviceState.class);
                }catch (Exception ex)
                {
                    System.out.println(ex.getMessage()+";"+ content+";"+sender.getLocalName());
                }
                SliceWindow w= currentMemberMonitor.get(sender);
                if((w!=null && d!=null)&&(currentState==null || currentState.time<=d.time)) {

                    currentMemberMonitor.get(sender).put(d);
                    PCState fh = ComputeHouseMonitor();
                    fh.type = GridMemberType.ProducerConsumer;
                    fh.ID = getLocalName();
                    fh.convType = ct;
//                    if (currentMemberMonitor.get(this.getAID()).getLast() != null)
//                        fh.currentSource = ((PCState) currentMemberMonitor.get(this.getAID()).getLast()).currentSource;
//                    else
//                        fh.currentSource = GridMemberType.Na;
                    fh.currentSource=AnalyseSource();
                    currentMemberMonitor.get(this.getAID()).put(fh);
                    fh.time=d.time;
                    fh.isUsed=false;
                    currentState=fh;
                }
                break;
            case holonMonitoring:
                PCState h=null;
                try {
                    h = fromJson(content, PCState.class);
                }catch (Exception ex)
                {
                    System.out.println(ex.getMessage()+";"+ content+";"+sender.getLocalName());
                }
                w= currentParentMonitor.get(sender);
                if((w!=null && h!=null)&&(w.getLast()==null|| w.getLast().time <h.time)) {
                    h.convType= ConversationTypes.holonMonitoring;
                    w.put(h);
                    currentHolonState = h;
                    h.isUsed = false;

                    for (int i = 0; i < Parents.size() && currentState!=null; i++) {
                        AID p= Parents.get(i);
                        w= currentParentMonitor.get(p);
                        if (w!=null && w.getLast()!=null ) {
                            if ( (currentState.time-w.getLast().time)/60>15) {
                                RemoveParent(p);
                            }
                        }
                    }
                }
                break;
            case instr:
                break;
            case join:
                PCState dh=fromJson(content,PCState.class);

                if(  !Parents.contains(sender) && (currentJoinProposal==null || currentJoinProposal.time<dh.time) && Parents.size()<2 )
                    currentJoinProposal = new ControllerHeadHolonFact(dh,(PCState) currentState,false,ct);

                break;
            case confirmJoin:
                if(!Parents.contains(sender) && Parents.size()<2) {
                    AddNewParent(sender);
                }
                break;
            case emergentResponse:
                LoadProposal p= fromJson(content,LoadProposal.class);

                if(currentLoadProposals!=null && currentLoadProposals.size()>0 && currentLoadProposals.size()==Parents.size()&& p.time==currentLoadProposals.get(0).time )
                    return;
                if(currentLoadProposals.size()>0 && p.time>currentLoadProposals.get(0).time)
                    currentLoadProposals.clear();
                int k=0;
                for(;k<currentLoadProposals.size();k++)
                    if(currentLoadProposals.get(k).sender.equals(p.sender))
                    {
                        currentLoadProposals.remove(k);
                        break;
                    }
                p.ID=getLocalName();
                currentLoadProposals.add(p);

                break;
            case acceptLoadProposal:

                LoadProposalItem i =fromJson(content,LoadProposalItem.class);
                if( isvalidLoadRequester(i)&&  !i.requester.equals(getLocalName())) {

                    currentacceptLoadProposal=i;
                    currentacceptLoadProposal.isPush = false;
                    currentacceptLoadProposal.accepted = false;
                    currentacceptLoadProposal.isUsed=false;
                    //printLoad(currentacceptLoadProposal.toString(),ct.toString(),i);
                }
                break;
            case pushLoad:
                i =fromJson(content,LoadProposalItem.class);
                if(isValidPushload(i))
                {
                    currentPushLoad=i;
                    currentPushLoad.isPush=true;
                    currentPushLoad.accepted=false;
                    currentPushLoad.isUsed=false;
                   //printLoad(currentPushLoad.toString(),ct.toString(),i);
                }
                break;
        }

    }


    Map<String, Integer> pushloadproposals=new HashMap<String, Integer>();
    private boolean isValidPushload(LoadProposalItem lp) {
        if(!pushloadproposals.containsKey(lp.id))
        {
            pushloadproposals.put(lp.id,lp.time);
            return true;
        }
        if( pushloadproposals.get(lp.id)<lp.time) {
            pushloadproposals.put(lp.id, lp.time);
            return true;
        }
        return false;
    }

    Map<String, Integer> loadproposals=new HashMap<String, Integer>();
    private boolean isvalidLoadRequester(LoadProposalItem lp) {

        if(!loadproposals.containsKey(lp.requester))
        {
            loadproposals.put(lp.requester,lp.time);
            return true;
        }
        if( loadproposals.get(lp.requester)<lp.time) {
            loadproposals.put(lp.requester, lp.time);
            return true;
        }
        return false;
    }

    private GridMemberType AnalyseSource() {
        boolean lcp = false, lcg = false, lcb = false;
        for(AID i:Members) {
            DeviceState fh = (DeviceState) currentMemberMonitor.get(i).getLast();

            if (fh != null && fh.lc == 1) {
                if (fh.type == GridMemberType.Producer )
                    lcp = true;
                else if (fh.type == GridMemberType.Storage )
                    lcb = true;
                else if (fh.type == GridMemberType.GRID )
                    lcg = true;
            }
        }
        if(lcg==false && lcb==true && lcp==false)
            return GridMemberType.Storage;
        if(lcg==false && lcb==true && lcp==true)
            return GridMemberType.Producer;
        if(lcg==true && lcb==false && lcp==false)
            return GridMemberType.GRID;
        return GridMemberType.Na;
    }

    private PCState ComputeHouseMonitor() {
        PCState d= new PCState();
        for(AID c :Members)
        {

            DeviceState mc=(DeviceState)currentMemberMonitor.get(c).getLast();
            if(mc!=null) {
                switch (mc.type)
                {
                    case Producer:
                        d.totalProduceLoads += mc.load;
                        d.sizeProducers++;
                        break;
                    case Consumer:
                       d.load += mc.load;
                       d.sizeConsumers ++;
                        break;
                    case Storage:
                        d.totalStorageLoads += mc.load;
                        d.sizeStorages++;
                        break;
                    case GRID:
                        d.totalGridLoads += mc.load;
                        break;
                }
                if(d.time<mc.time)
                    d.time=mc.time;

            }
        }
        d.totalMemebersCount=Members.size();
        return d;
    }

}
