package My.prototype;


import My.Project.Thesis.Ontology.*;
import My.prototype.ControllerAgent;
import My.prototype.SliceWindow;
import jade.content.ContentElementList;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.StringACLCodec;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Simulator extends Agent {
    ServerSocket srvr = null;
    Socket skt = null;
    BufferedReader in;
    PrintWriter out;
    int value = 0;
    Map<String, DeviceInstruct> deviceInstructList;
    ArrayList<String> devices;

    @Override
    protected void setup()
    {

        /** Registration with the DF */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("SimulatorAgent");
        sd.setName(getName());
        sd.setOwnership("SimulatorMatlab");
        dfd.addServices(sd);
        dfd.setName(getAID());
        dfd.addOntologies("Simulator_Ontology");
        try {
            DFService.register(this,dfd);
        } catch (FIPAException e) {
            System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
            doDelete();
        }

        addBehaviour(new CommunicateWithMatlab());
        addBehaviour(new GetMonitoringData(this));
        deviceInstructList =new HashMap<String, DeviceInstruct>();
        devices=new ArrayList<>();

    } // End setup


    private class CommunicateWithMatlab extends CyclicBehaviour {
        private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

        public void action() {
            reset();
            ACLMessage msg = myAgent.receive();

            if (msg != null) {

                String DeviceID = msg.getSender().getLocalName();
                ACLMessage reply = msg.createReply();
                String id=msg.getConversationId();

                if(id.equals(ConversationTypes.instr.toString())) {



                    DeviceInstruct i=ControllerAgent.fromJson( msg.getContent(), DeviceInstruct.class);
                    String key=i.reciverID;
                    if(deviceInstructList.get(key)==null)
                        deviceInstructList.put(key, i);
                    else if(deviceInstructList.get(key).cmd== CommandTypes.noCommand)
                        deviceInstructList.replace(key, i);
                    else if(i.cmd !=CommandTypes.noCommand) {
                        deviceInstructList.replace(key, i);
                        //System.out.println("key:" + key + "=" + i);
                    }

                }else if (id.equals(ConversationTypes.monitoringRequest.toString()))
                {
                    if(devices.add(msg.getSender().getLocalName()));
                }

                block();
            } else {
                block();
            }

        }


        public  String GetDeviceStateFromMatlab(String DeviceID){
          // System.out.println(getLocalName()+ " start getDevice");
            return SendInstruction("GetState, "+DeviceID);
        }

        private String SendInstruction(String s) {
            String ack="";
            while(!ack.contains("t="))
                ack=callMatlabSend(s);
            //System.out.println("Instruction  sent to Matlab: " + s);
            return ack;
        }
    }
    @Override
    protected void takeDown()
    {

        // Close writer and socket
        disposeConnection();
    }

    public String callMatlabRecv() {
        String matlabAnswer = "";

            initConnection();

            try {
                in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                matlabAnswer= in.readLine();

                //System.out.println(this.getLocalName() + " recived :" + matlabAnswer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            disposeConnection();

        return matlabAnswer;
    }

    private void disposeConnection() {
        try
        {
            if(out!=null)
                out.close();
            if(in!=null)
                in.close();
            if(skt!=null)
                skt.close();
            if(srvr!=null)
                srvr.close();
        }
        catch (IOException e) {	e.printStackTrace(); }


    }

    private void initConnection() {

        try {
            srvr = new ServerSocket(1234);
            skt = srvr.accept();
           // System.out.println("Server connection initiated");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String callMatlabSend(String msgContent)
    {
        String ack="";
        while(ack==null || ack.length()==0) {
            initConnection();
            try {
                out = new PrintWriter(skt.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("localhost:1234", AID.ISGUID));
                msg.setContent(msgContent);
                // Encode message to send as an ACL Message
                StringACLCodec codec = new StringACLCodec(in, out);
                codec.write(msg);
                //out.flush();
                if (out.checkError()) {
                    callMatlabSend(msgContent);
                }
               // System.out.println("Send " + this.getLocalName() + " :" + msgContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            disposeConnection();
            ack= callMatlabRecv();
        }



        return ack ;

    } // End callMatlab
    private class GetMonitoringData extends TickerBehaviour {
        private GetMonitoringData(Agent a) {
            super(a, 500);
        }

        public void onTick() {
            actiont();
        }

        public void actiont() {
            if(AllInstructIsSentToMatlab()) {

                String deviceList = GetDeviceList();
                Map<String, String> map = GetDevicesStateFromMatlab(deviceList);
                if (map != null)
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (!entry.getKey().equals("t")) {
                            String[] v=entry.getValue().split(":");
                            DeviceState d=new DeviceState(entry.getKey(),map.get("t"),v[0],v[1]);
                            SendMessage(new AID(entry.getKey(), false), d.toString());
                        }
                    }
            }
        }


        private boolean AllInstructIsSentToMatlab() {
            for (Map.Entry<String, DeviceInstruct> entry : deviceInstructList.entrySet()) {
                if (entry.getValue()==null) {
                    return false;
                }
            }
            SendAllInstructToMatlab();
            return true;
        }

        private void SendAllInstructToMatlab() {
            String instruct="";
            for (Map.Entry<String, DeviceInstruct> entry : deviceInstructList.entrySet()) {
                if(entry.getValue().cmd!=CommandTypes.noCommand)
                    instruct += entry.getValue().reciverID + "; " + entry.getValue().params+", ";
            }
            instruct= instruct.length()>1? instruct.substring(0,instruct.length()-2):"";
            if(instruct.isEmpty())
                return;
            SendInstruction("DoAllInstructs, "+instruct);
            //System.out.println("DoAllInstructs, "+instruct);
        }


        public void SendMessage( AID receiver,String content ) {

            try {
                if(receiver==null  )
                    return;
                ACLMessage singleInstruct = new ACLMessage(ACLMessage.INFORM);
                singleInstruct.setConversationId(ConversationTypes.monitoring.toString());
                singleInstruct.setContent(content);
                singleInstruct.addReceiver(receiver);
                send(singleInstruct);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private Map<String, String> GetDevicesStateFromMatlab(String deviceList) {
            if(deviceList.length()>1)
                return SendInstruction("GetAllState, "+deviceList);
            return null;
        }

        private Map<String, String> SendInstruction(String s) {
            if(s==null && s.length()<2)
                return null;
            String ack="";
            while(!ack.contains("t="))
                ack=callMatlabSend(s);

            //System.out.println("Monitor: " + ack);
            String[]stattes=ack.split(", ");
            Map<String, String> res=new HashMap<String, String>();
            for (String t:stattes) {
                String[] v=t.split("=");
                res.put(v[0],v[1]);
            }

            return res;
        }




    }
    private String GetDeviceList() {
        deviceInstructList.clear();
        String deviceList="";

            for (int i = 0; i < devices.size(); ++i) {
                deviceList+=  devices.get(i)+", ";
                deviceInstructList.put( devices.get(i),null);
            }
        return deviceList.length()>1? deviceList.substring(0,deviceList.length()-2):"";
    }
}
