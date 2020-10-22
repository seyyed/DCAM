package My.prototype;

import My.Project.Thesis.Ontology.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.apache.commons.collections4.Get;


import java.util.*;

public class HolonControllerAgent extends ControllerAgent {

    public Map<AID, PCState> currentEmergentRequest;
    public Map<AID, PCState> currentAcceptedJoinRequest;
    private PCState currentHolonState;

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new GetJoinRequestData(this));
        currentEmergentRequest=new HashMap<>();
        currentAcceptedJoinRequest=new HashMap<>();
        addBehaviour(new HolonMonitorManager((this)));
    }


    @Override
    protected void UpdateStatus(AID sender, String content, ConversationTypes ct) {
        switch(ct)
        {
            case monitoring:
                PCState d=fromJson(content,PCState.class);
                SliceWindow w= currentMemberMonitor.get(sender);
                if(w!=null && d!=null) {
                    currentMemberMonitor.get(sender).put(d);
                    PCState dh = ComputeHolonMonitor();
                    dh.type = GridMemberType.ProducerConsumer;
                    dh.convType =ct;
                    dh.ID = getLocalName();
                    currentState=dh;
                    currentMemberMonitor.get(this.getAID()).put(dh);

                    for (int i = 0; i < Members.size(); i++) {
                        AID idm = Members.get(i);
                        if (currentMemberMonitor.containsKey(idm) && !idm.equals(sender)) {
                            PCState pt = (PCState) currentMemberMonitor.get(idm).getLast();
                            if (pt!=null && (currentState.time-pt.time)/60>60) {
                                Members.remove(idm);
                                printMembers("remove "+ idm.getLocalName());
                            }
                            else if(pt==null) {
                                pt=new PCState();
                                pt.time=currentState.time;
                                pt.ID=idm.getLocalName();
                                currentMemberMonitor.get(idm).put(pt);
                           }
                        }
                    }
                }else
                    System.out.println("["+getLocalName()+"] error u="+(d==null?"":d.ID) + " c="+content);

                break;
            case holonMonitoring:
                PCState h=null;
                try {
                    h = fromJson(content, PCState.class);
                    h.convType= ConversationTypes.holonMonitoring;
                }catch (Exception ex)
                {
                    System.out.println(ex.getMessage()+";"+ content+";"+sender.getLocalName());
                }
                w= currentParentMonitor.get(sender);
                if((w!=null && h!=null)&&( w.getLast()==null|| w.getLast().time<=h.time)) {

                    w.put(h);
                    currentHolonState=h;
                    h.isUsed=false;
                }
                break;
            case instr:
                break;
            case acceptJoinProposal:
                PCState df=fromJson(content,PCState.class);
                df.convType=ct;
                df.isUsed=false;
                if( !Members.contains(sender)&&(  currentAcceptedJoinRequest.get(sender) == null || currentAcceptedJoinRequest.get(sender).time<df.time))
                    currentAcceptedJoinRequest.put(sender,df);

                break;
            case emergent:
                df=fromJson(content,PCState.class);
                df.convType=ct;
                df.isUsed=false;
                if((  currentEmergentRequest.get(sender) == null || currentEmergentRequest.get(sender).time<df.time)&& Members.contains(sender))
                    currentEmergentRequest.put(sender,df);

                break;
            case rejectJoinProposal:
                break;
            case confirmJoin:
                if(!Parents.contains(sender) && Parents.size()<2)
                    AddNewParent(sender);
                break;
            case emergentResponse:
                if(currentLoadProposals.size()==Parents.size())
                    currentLoadProposals.clear();
                LoadProposal p= fromJson(content,LoadProposal.class);

                currentLoadProposals.add(p);
                break;
        }

    }

    private PCState ComputeHolonMonitor() {
        PCState d= new PCState();
        for(AID c :Members)
        {
            PCState mc=(PCState) currentMemberMonitor.get(c).getLast();
            if(mc!=null) {
                d.totalGridLoads += mc.totalGridLoads;
                d.totalProduceLoads += mc.totalProduceLoads;
                d.totalStorageLoads +=mc.totalStorageLoads;
                d.sizeConsumers +=mc.sizeConsumers;
                d.sizeProducers +=mc.sizeProducers;
                d.sizeStorages += mc.sizeStorages;
                d.load+=mc.load;
                if(d.time<mc.time)
                    d.time=mc.time;

            }

        }
        d.totalMemebersCount= Members.size();


        return d;
    }
    private class GetJoinRequestData extends TickerBehaviour {
        private GetJoinRequestData(Agent a) {
            super(a, 4000);

        }

        public void onTick() {
            actiont();
        }

        public void actiont() {
            List<AID> l=GetRequestList();

            if(l.size()>0) {
                PCState f= (PCState)currentState;
                if(f!=null) {
                    f.convType= ConversationTypes.join;
                    SendMessage(l, f.toString());
                }else {
                    PCState fn=new PCState();
                    fn.convType= ConversationTypes.join;
                    fn.ID=getLocalName();
                    fn.type= GridMemberType.ProducerConsumer;
                    currentMemberMonitor.get(getAID()).put(fn);
                    SendMessage(l,fn.toString() );
                }
            }
//            Iterator itr = Members.iterator();
//            while (itr.hasNext())
//            {
//                AID m = (AID) itr.next();
//                ControllerState fm= currentMemberMonitor.get(m).getLast();
//                ControllerState fh= currentMemberMonitor.get(getAID()).getLast();
//                if(fm!=null)
//                    if(fh.time-fm.time>120)
//                        itr.remove();
//            }

        }
        public void SendMessage( List<AID> receiver,String content ) {

            try {
                if(receiver==null  )
                    return;
                ACLMessage singleInstruct = new ACLMessage(ACLMessage.REQUEST);
                singleInstruct.setConversationId(ConversationTypes.join.toString());
                singleInstruct.setContent(content);
                for(AID name:receiver)
                    if(!Members.contains(name))
                        singleInstruct.addReceiver(name);

                send(singleInstruct);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private List<AID> GetRequestList() {

            List<AID> list=new ArrayList<>() ;
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            if(regionID.isEmpty())
                return list;
            sd.setType(regionID);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                for (int i = 0; i < result.length; ++i) {
                    list.add( result[i].getName());
                }
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }

            Iterator itr = list.iterator();
            while (itr.hasNext())
            {
                AID name = (AID) itr.next();
                if (Members.contains(name))
                    itr.remove();
            }
            return list;
        }

    }
    private class HolonMonitorManager extends TickerBehaviour {
        private HolonMonitorManager (Agent a) {
            super(a, 2000);
        }

        public void onTick() {
                SendHolonMonitorData();
        }
        protected  void SendHolonMonitorData()
        {
            if(Members.size()>0 && currentMemberMonitor.get(myAgent.getAID()).getLast()!=null)
                for(AID m:Members) {
                    PCState pt = (PCState) currentMemberMonitor.get(m).getLast();
                    if (pt!=null)
                        SendSingleMessage(currentMemberMonitor.get(myAgent.getAID()).getLast().toString(), m, ConversationTypes.holonMonitoring, ACLMessage.INFORM);
                }
        }
    }


}
