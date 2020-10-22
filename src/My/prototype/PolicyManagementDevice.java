package My.prototype;

import My.Project.Thesis.Ontology.*;

import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PolicyManagementDevice extends PolicyManagementService {
    DeviceControllerAgent b;
    Map<Integer,Float> lcTimeLine;

    @Override
    public void setup(ControllerAgent controllerAgent, String pFileName) throws IOException {
        super.setup(controllerAgent,pFileName);
        b=(DeviceControllerAgent)agent;
        lcTimeLine=new HashMap<>();
    }
    @Override
    protected void InsertFact() {

        InitInsertFacts();

        currentInstructHandle = null;
        currentStateHandle = null;

        if (b.currentInstruct != null && b.currentInstruct.isUsed == false) {
            currentInstructHandle = kieSession.insert(b.currentInstruct);
            kieSession.insert(b.currentState);
            b.currentInstruct.isUsed = true;
        }
        if (b.currentState != null && b.currentState.isUsed == false) {
            currentStateHandle = kieSession.insert(b.currentState);
            b.currentState.isUsed = true;
        }
        if(b.currentInstruct==null)
            kieSession.insert(new DeviceInstruct("", CommandTypes.noCommand,0,b.currentState.time));

        EndInsertFacts();
    }



    @Override
    public List<Instruction> plan() {
        List<Instruction> li=new ArrayList<Instruction>();
        Instruction i=null;
        if( currentStateHandle!=null)//monitoring event
        {
            if (fact.needsChangeUp)
                i=changeLoadDevice((DeviceState) b.currentState);
            else if (fact.needsChangeDown)
                i=changeLoadDevice((DeviceState) b.currentState);
            if(i!=null)
                li.add(i);
            currentStateHandle=null;
        }
        if( currentInstructHandle!=null)//monitoring event
        {

            if(b.currentState!=null) {

                if (fact.needsChangeUp)
                    i=changeLoadDevice((DeviceState) b.currentState);
                else if (fact.needsChangeDown)
                    i=changeLoadDevice((DeviceState) b.currentState);
                else if (fact.needsChangeStoage)
                    li.add( changeStorageDevice(b));
                else if (fact.needsChangeLoad )
                    i=changeLoadDevice((DeviceState) b.currentState);
                if(i!=null)
                    li.add(i);
            }
            currentInstructHandle=null;

        }
        return li;
    }

    private Instruction changeLoadDevice(DeviceState b) {
        if(lcTimeLine.containsKey(b.time) )
            return null;
        else{
            if (agent.deviceType == DeviceTypes.dimmer) {
                if(fact.needsChangeDown)
                    b.lc -= downStep;
                else {
                    if (b.load == 0)
                        b.lc = 1;
                    else {
                        float newlc = (agent.maxLoad * b.lc) / b.load + 0.001f;

                        b.lc = newlc;
                    }
                }
            } else if (agent.deviceType == DeviceTypes.regular)
                if (fact.needsChangeUp) {
                    b.lc += downStep;
                }else if(fact.needsChangeDown)
                    b.lc -= downStep;
                else
                    b.lc=1;

            if (b.lc > 1)
                b.lc = 1;
            else if (b.lc < 0)
                b.lc = 0;
            lcTimeLine.put(b.time,b.lc);
        }
        return getSimInstruct(new DeviceInstruct(b.ID, CommandTypes.change, lcTimeLine.get(b.time), b.time));

    }


    private Instruction changeStorageDevice(DeviceControllerAgent b) {
        b.currentState.load=b.currentInstruct.params + b.currentState.load;
        return getSimInstruct( new DeviceInstruct( DeviceNames.BatteryChargeID.getValue()+"-"+ b.getHid(),CommandTypes.change,
                b.currentState.load ,b.currentState.time));
    }


}
