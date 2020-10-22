package My.prototype;

import My.Project.Thesis.Ontology.*;
import jade.core.AID;


public class DeviceControllerAgent extends ControllerAgent {


    public DeviceInstruct currentInstruct;

    @Override
    protected void UpdateStatus(AID sender, String content, ConversationTypes ct) {
        if(content.contains("(action"))//invalid data
            return;
        switch(ct)
        {
            case monitoring:
                DeviceState d=fromJson(content,DeviceState.class);
                if(currentState==null || currentState.time<d.time) {
                    d.type = GetDevicetype(getLocalName());
                    d.priority = this.priority;
                    d.isUsed=false;
                    currentMemberMonitor.get(this.getAID()).put(d);
                    currentState=d;

                    SendAgentMonitorData();
                }
                break;
            case instr:

                DeviceInstruct i=fromJson(content,DeviceInstruct.class);
                //if(currentInstruct == null || currentInstruct.time<i.time) {
                    currentInstruct=i;
                    currentInstruct.isUsed=false;
                    if(i.cmd==CommandTypes.changeMax)
                        maxLoad=i.params;
                //}

                break;
        }

    }

    public static GridMemberType GetDevicetype(String localName) {
        int type=Integer.parseInt( localName.substring(0,localName.indexOf("-")));
        switch(type){
            case 1:
                return GridMemberType.GRID;
            case 4:
                return GridMemberType.Producer;
            case 3:
                return GridMemberType.Storage;
            default:
                return GridMemberType.Consumer;
        }
    }


}
