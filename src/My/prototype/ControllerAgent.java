package My.prototype;

import My.Project.Thesis.Ontology.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.drools.compiler.compiler.AnalysisResult;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.*;

public class ControllerAgent extends Agent {

    public int upLevel;
    public float maxLoad=1;
    String ruleFilesPath="src/My/rules/";
    private String ID;
    public Vector<AID> Members=new Vector<>();
    Vector<AID> Parents=new Vector<>();
    Map<AID, SliceWindow> currentMemberMonitor=new HashMap<AID, SliceWindow>();
    Map<AID, SliceWindow> currentParentMonitor=new HashMap<AID, SliceWindow>();
    String PFileName="";
    private static final long serialVersionUID = 1L;
    AID SimulatorID;
    int SlidSize=5;
    PolicyManagementService pm;
    PolicyManagerTypes pmType=PolicyManagerTypes.NA;
    private boolean NeedAdaption;
    public DeviceTypes deviceType;
    public int priority;
    private String serviceRequest;
    protected String regionID="";
    protected ArrayList<LoadProposal> currentLoadProposals;
    protected ArrayList<LoadProposalItem> currentLoadProposalItems;
    protected LoadProposalItem currentacceptLoadProposal;
    protected LoadProposalItem currentPushLoad;
    protected int LastEmergentTime=0;
    public ControllerState currentState;

    @Override
    protected void setup()
    {
        addStaticMembers();
        addBehaviour(new Monitoring());

            addBehaviour(new MonitorManager(this));
        //addBehaviour(new PlanAndExecute(this));



        ID= getLocalName();
        currentLoadProposals=new ArrayList<>();
        currentLoadProposalItems=new ArrayList<>();

    } // End setup

    protected void RegisterService(String serviceName) {

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        sd.setName(getName());
        sd.setOwnership(getLocalName());
        dfd.addServices(sd);
        dfd.setName(getAID());
        try {
            DFService.register(this,dfd);
            System.out.println(getLocalName()+" registration: "+ serviceName);
        } catch (FIPAException e) {
            System.err.println(getLocalName()+" registration "+ serviceName +" unsucceeded. Reason: "+e.getMessage());
            doDelete();
        }
    }


    protected void addStaticMembers() {
        Object[] args = getArguments();
        serviceRequest="";
        if (this.getClass() == My.prototype.DeviceControllerAgent.class )
            pm=new PolicyManagementDevice();
        else if (this.getClass() == My.prototype.HouseControllerAgent.class )
            pm=new PolicyManagementHouse();
        else if (this.getClass() == My.prototype.HolonControllerAgent.class )
            pm=new PolicyManagementHolon();

        if(args!=null&& args.length>0)
            if(!currentMemberMonitor.containsKey(this.getAID()))
                currentMemberMonitor.put(this.getAID(),new SliceWindow(SlidSize));
            for (int i = 0; i < args.length; i++)
            {
                String n= (String)args[i];
                if(n.charAt(0) == 'C') {
                    AID t=getName(n.substring(2));
                   AddNewMember(t);
                }else if(n.charAt(0) == 'P') {
                    AID pid=getName(n.substring(2));
                    AddNewParent(pid);
                }else if(n.charAt(0) == 'F') {
                    PFileName = ruleFilesPath+ n.substring(2);

                }else if(n.charAt(0) == 'M') {
                    maxLoad = Float.parseFloat( n.substring(2));
                }

                else if(n.charAt(0) == 'S') {
                    SimulatorID = getName(n.substring(2));
                    currentMemberMonitor.put(this.getAID(),new SliceWindow(SlidSize));
                }
                else if(n.charAt(0) == 'T') {
                    if(n.substring(2).equals(DeviceTypes.dimmer.toString()))
                        deviceType=DeviceTypes.dimmer;

                }
                else if(n.charAt(0) == 'R') {
                   if(n.substring(2).equals(PolicyManagerTypes.Drools.toString()))
                       pmType=PolicyManagerTypes.Drools;
                   else if(n.substring(2).equals(PolicyManagerTypes.Jess.toString()))
                       pmType=PolicyManagerTypes.Jess;
                }
                else if(n.contains("priority=")) {
                    priority=Integer.parseInt(n.substring(9));
                }
                else if(n.contains("service=")) {
                    serviceRequest=n.substring(8);
                }
                else if(n.contains("region=")) {
                    regionID=n.substring(7);
                }else if(n.contains("downstep=")) {
                    pm.downStep=Float.parseFloat(n.substring(9));
                }
            }


        try {
            pm.setup(this,PFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.getClass() == My.prototype.DeviceControllerAgent.class) {
            //serviceRequest = ServiceRequestTypes.DeviceMonitoring.toString();
            SendSingleMessage("DeviceMonitoring",SimulatorID,ConversationTypes.monitoringRequest,ACLMessage.INFORM);
        }
        if(deviceType==null)
            deviceType= DeviceTypes.regular;
        if(!serviceRequest.isEmpty())
            RegisterService(serviceRequest);
    }

    public void AddNewMember(AID t) {

        if(t==null || Members.contains(t))
            return;
        Members.addElement(t);
        currentMemberMonitor.put(t,new SliceWindow(SlidSize));
        printMembers(t.getLocalName());
    }
    public void AddNewParent(AID t) {
        if(t==null || Parents.contains(t))
            return;
        Parents.addElement(t);
        printParents("Add "+t.getLocalName());
        if(!currentMemberMonitor.containsKey(t))
            currentParentMonitor.put(t,new SliceWindow(SlidSize));
    }
    public void RemoveParent(AID t)
    {
        Parents.remove(t);
        printParents("remove " + t.getLocalName());
    }
    AID getName(String l) {
        return  new AID(l, false);
    }

    public String getHid() {
        return getLocalName().substring(getLocalName().indexOf("-")+1);
    }

    public void printMembers(String last) {
        System.out.println("ch:\t"+ (currentState==null?0:currentState.time)+"\t"+getLocalName()+"\tlast: "+last+"\t");
        for (AID m:Members) {
            System.out.print(m.getLocalName()+"\t");
        }
        System.out.println("\n"+getLocalName()+":"+ currentState+"\n");
    }


    protected class Monitoring extends CyclicBehaviour {
        public void action() {
            reset();
            ACLMessage rvMesage = myAgent.receive();
            if (rvMesage != null && rvMesage.getContent()!=null ) {
                String content = rvMesage.getContent();
                try {
                    ConversationTypes ct = ConversationTypes.valueOf(rvMesage.getConversationId());

                    UpdateStatus(rvMesage.getSender(), content, ct);
                    AnalysisStatus(rvMesage.getSender(), content, ct);
                    PlanAndExecute();
                }
                catch(Exception ex)
                {
                    System.out.println("["+getLocalName()+"]: error:"+ex.getMessage()+"\n");
                    ex.printStackTrace();
                }
            }

        }
    }
    public  void printParents(String act)
    {
        System.out.println("pt:\t"+ (currentState==null?0:currentState.time)+"\t"+getLocalName()+"\t"+act+"\t"+ (Parents.size()>0? Parents.get(0).getLocalName():" ")+ "\t"+ (Parents.size()>1?Parents.get(1).getLocalName():""));
    }
    public  void  printLoad(String in,String comment,LoadProposalItem p)
    {
        System.out.println("lt:\t"+(currentState==null?0:currentState.time)+ "\t"+ p.id +"\t"+p.requester+"\t"+p.load+"\t"+p.isPush);

    }
    private void AnalysisStatus(AID sender, String content, ConversationTypes ct) {
        NeedAdaption=false;

        try {
            pm.InsertFact();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }
        switch ( ct)
        {
            case monitoring:
                if(GoodCondition())
                    return;

                break;
            case join:
            case acceptJoinProposal:
                if(!isNewRequest(sender,content,ct))
                    return;
                break;
            case instr:
                if(!canDoInstruct(sender,content))
                    return;
                break;
//            case emergent:
//                if(!canDoEmergent(sender,content)) {
//                    AddEmergentRequest(sender, content);
//                    return;
//                }
//                break;
        }
        NeedAdaption=true;
    }

    private boolean isNewRequest(AID sender, String content, ConversationTypes ct) {
        switch (ct)
        {
            case join:
                if(Parents.contains(sender))
                    return false;
                break;
            case acceptJoinProposal:
                if(Members.contains(sender))
                    return false;
                break;
        }

        return !pm.isGoodCondition();
    }

    //    private class PlanAndExecute extends TickerBehaviour {
//        private PlanAndExecute(Agent a) {
//            super(a, 2000);
//        }
//
//        public void onTick() {
//            actiont();
//        }
//
//        public void actiont() {
    private void PlanAndExecute(){
            List<Instruction> instructs =new ArrayList<Instruction>();


            if(NeedAdaption) {
                instructs=prepareInstructions();

            }
            if(SimulatorID!=null)
            {
                boolean hasSimulator=false;
                for(Instruction p:instructs)
                    if(p.getSubject().equals(SimulatorID)) {
                        hasSimulator=true;
                        break;
                    }
                if(!hasSimulator && currentState!=null )
                    instructs.add(new Instruction(SimulatorID,new DeviceInstruct(getLocalName(),CommandTypes.noCommand,0,currentState.time).toString(),ConversationTypes.instr,0));
            }
            if (instructs != null && instructs.size() > 0)
                Execute(instructs);

        }
   // }


    protected void Execute(List<Instruction> instructions) {

        for (Instruction instr:instructions) {
            SendSingleInstruct(instr);
        }

    }

    private List<Instruction> prepareInstructions() {

            return pm.plan();
    }



    protected boolean canDoAdaption() {

        return pm.canDoAdaption();
    }

    private boolean randomBoolean() {
        return (Math.random()>0.5);
    }

    private String getAdaptionNeed() {
        return "A"+ID;
    }

    private void AddEmergentRequest(AID sender, String content) {
        if(Parents.size()>0)
        SendSingleEmergentNeed("E"+ID,Parents.get(0));
    }

    private boolean canDoEmergent(AID sender, String content) {
        return randomBoolean();
    }


    protected boolean GoodCondition() {
       return pm.isGoodCondition();
    }



    protected boolean canDoInstruct(AID sender, String content) {
        return pm.canDoInstruct(sender);
    }

    protected void UpdateStatus(AID sender, String content, ConversationTypes ct) {

    }

    public static  <T> T fromJson(String json, Class<T> classOfT)  {
        Gson gson = new Gson();
        T  t= gson.fromJson(json,(Type)classOfT);
        return t;
    }

    private String getString(String content, String starPattern, String endPattern) {
        if(endPattern.length()>0)
            return content.substring(content.indexOf(starPattern)+starPattern.length(),content.indexOf(endPattern));
        return content.substring(content.indexOf(starPattern)+starPattern.length());
    }
    public void SendSingleInstruct(Instruction instr) {
        SendSingleMessage(instr.content,instr.subject, instr.getConvType(), ACLMessage.REQUEST);
    }

    public void SendSingleEmergentNeed( String request ,AID  receiver) {
        SendSingleMessage(request,receiver, ConversationTypes.emergent, ACLMessage.REQUEST);
    }

    public void SendSingleMonitor( String status ,AID  receiver) {
        SendSingleMessage(status,receiver, ConversationTypes.monitoring,ACLMessage.INFORM);
    }

    public void SendSingleMessage( String command ,AID  receiver,ConversationTypes covID,int type ) {

        try {
            if(receiver==null  )
                return;
            ACLMessage singleInstruct = new ACLMessage(type);
            singleInstruct.setConversationId(covID.toString());
            singleInstruct.setContent(command);
            singleInstruct.addReceiver(receiver);
            send(singleInstruct);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void SendMessage( String command ,Vector<AID>  receivers,ConversationTypes covID,int type ) {

        try {
            if(receivers==null || receivers.size()==0  )
                return;
            ACLMessage singleInstruct = new ACLMessage(type);
            singleInstruct.setConversationId(covID.toString());
            singleInstruct.setContent(command);
            for(AID r:receivers)
                singleInstruct.addReceiver(r);
            send(singleInstruct);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class MonitorManager extends TickerBehaviour {
        private MonitorManager(Agent a) {
            super(a, 2000);
        }

        public void onTick() {
            if(myAgent.getClass() != My.prototype.DeviceControllerAgent.class)
                SendAgentMonitorData();
        }
    }

    protected  void SendAgentMonitorData()
    {
        if(Parents.size()>0 && currentState!=null)
            for(AID p:Parents)
                SendSingleMonitor(currentState.toString(),p);
    }


}
