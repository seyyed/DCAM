package My.Project.Thesis;

import My.Project.Thesis.Ontology.DeviceNames;
import My.Project.Thesis.Ontology.PolicyManagerTypes;
import My.Project.Thesis.Ontology.ServiceRequestTypes;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.tools.rma.rma;
import jade.tools.sniffer.Sniffer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.concurrent.TimeUnit;

public class LaunchJade
{

    static ContainerController cController;

    public static void main(String[] args)
    {
        try
        {
            runJade();
        }
        catch (StaleProxyException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void runJade() throws StaleProxyException
    {
        Profile profile=new ProfileImpl("localhost", 1099, Profile.PLATFORM_ID);

        profile.setParameter(Profile.PLATFORM_ID, "GridPlatform");

        // this is not working , why ? =>
        profile.setParameter("gui", "ture");


        // Launch JADE platform
        Runtime rt = Runtime.instance();
//        Profile p;
//        p = new ProfileImpl();
        cController = rt.createMainContainer(profile);
        rt.setCloseVM(true);
        Object[] args = new Object[1];
        args[0] = 1;
        String simulatorID="Simulator";
        addAgent(simulatorID, "My.prototype.Simulator",args);
        String simulator="S="+simulatorID;
        addAgent("HCA-1","My.prototype.HolonControllerAgent",new String[]{"F=HCASimple.drl","R="+ PolicyManagerTypes.Drools,"region="+ServiceRequestTypes.joinR1,"C=HOCA-11","M=10"});
        addAgent("HCA-2","My.prototype.HolonControllerAgent",new String[]{"F=HCASimple.drl","R="+ PolicyManagerTypes.Drools,"region="+ServiceRequestTypes.joinR1,"C=HOCA-41","M=100"});
        addAgent("HCA-3","My.prototype.HolonControllerAgent",new String[]{"F=HCASimple.drl","R="+ PolicyManagerTypes.Drools,"region="+ServiceRequestTypes.joinR1,"C=HOCA-31","M=300"});
        //addAgent("HCA-2","My.prototype.HolonControllerAgent",new String[]{"F=HCASimple.drl","R="+ PolicyManagerTypes.Drools,"region="+ServiceRequestTypes.joinR1,"C=HOCA-31","M=10"});
        //addAgent("HCA-3","My.prototype.HolonControllerAgent",new String[]{"F=HCASimple.drl","R="+ PolicyManagerTypes.Drools,"region="+ServiceRequestTypes.joinR1,"C=HOCA-43","M=10"});
        //addAgent("HCA-2","My.prototype.HolonControllerAgent",new String[]{"F=HCASimple.drl","R="+ PolicyManagerTypes.Drools,"region="+ServiceRequestTypes.joinR1,"C=HOCA-32","C=HOCA-51"});
        //addAgent("HCA-3","My.prototype.HolonControllerAgent",new String[]{"F=HCASimple.drl","R="+ PolicyManagerTypes.Drools,"region="+ServiceRequestTypes.joinR1,"C=HOCA-42","C=HOCA-52"});
        int r=1;
        String parentH = "P=HCA-1";
        for(int i=1;i<=5;i++) {
            int hid=r*10+i;
            String parent= "P=HOCA-"+hid;
            addAgent(DeviceNames.LightsID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=LDevice.drl", "T=dimmer", "priority=1"});
            addAgent(DeviceNames.DishWasherID.getValue()+ "-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=HDevice.drl", "priority=3"});
            addAgent(DeviceNames.DryerID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=HDevice.drl", "priority=4","T=dimmer" });
            addAgent(DeviceNames.TVID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl", "priority=2"});
            addAgent(DeviceNames.SolarID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            addAgent(DeviceNames.BatteryID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            addAgent(DeviceNames.GridID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            if(hid==11)
                addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{
                    "C="+DeviceNames.LightsID.getValue()+"-"+hid, "C="+DeviceNames.DishWasherID.getValue()+"-"+hid,
                    "C="+DeviceNames.DryerID.getValue()+"-"+hid, "C="+DeviceNames.TVID.getValue()+"-"+hid,
                    "C="+DeviceNames.SolarID.getValue()+"-"+hid, "C="+DeviceNames.BatteryID.getValue()+"-"+hid, "C="+DeviceNames.GridID.getValue()+"-"+hid,
                    "F=HOCASimple.drl","M=0.08","downstep=0.005",parentH});
            else
                addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{"service=" + ServiceRequestTypes.joinR1,
                        "C="+DeviceNames.LightsID.getValue()+"-"+hid, "C="+DeviceNames.DishWasherID.getValue()+"-"+hid,
                        "C="+DeviceNames.DryerID.getValue()+"-"+hid, "C="+DeviceNames.TVID.getValue()+"-"+hid,
                        "C="+DeviceNames.SolarID.getValue()+"-"+hid, "C="+DeviceNames.BatteryID.getValue()+"-"+hid, "C="+DeviceNames.GridID.getValue()+"-"+hid,
                        "F=HOCASimple.drl","M=0.08","downstep=0.005"});
        }
        /*
        r++;
        for(int i=1;i<=1;i++) {
            int hid = r * 10 + i;
            String parent="P=HOCA-"+hid;
            addAgent(DeviceNames.Fridge2ID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=W1.drl"});
            addAgent(DeviceNames.HotWaterSystemID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=HDevice.drl"});
            addAgent(DeviceNames.WashingID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=DryerH1.drl"});
            addAgent(DeviceNames.OvenID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=TVH1.drl"});
            addAgent(DeviceNames.AirconID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=TVH1.drl"});
            addAgent(DeviceNames.BatteryID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            addAgent(DeviceNames.SolarID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=SolarH1.drl"});
            addAgent(DeviceNames.ComputerID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=TVH1.drl"});
            addAgent(DeviceNames.FreezerID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=TVH1.drl"});
            addAgent(DeviceNames.FridgeID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=TVH1.drl"});
            addAgent(DeviceNames.GridID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=GridH1.drl"});
            addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{"service=" + ServiceRequestTypes.joinR1,
                    "C="+DeviceNames.Fridge2ID.getValue()+"-"+hid , "C="+DeviceNames.HotWaterSystemID.getValue()+"-"+hid,
                    "C="+DeviceNames.WashingID.getValue()+"-"+hid, "C="+DeviceNames.OvenID.getValue()+"-"+hid,"C="+DeviceNames.AirconID.getValue()+"-"+hid,
                    "C="+DeviceNames.SolarID.getValue()+"-"+hid, "C="+DeviceNames.BatteryID.getValue()+"-"+hid,
                    "C="+DeviceNames.GridID.getValue()+"-"+hid,"C="+DeviceNames.ComputerID.getValue()+"-"+hid, "C="+DeviceNames.FreezerID.getValue()+"-"+hid,
                    "C="+DeviceNames.FridgeID.getValue()+"-"+hid,"F=HOCASimple.drl"});
        }
        r++;
        int k=0;
        for(r=3;r<=5;r++)
            for(int i=1;i<=2;i++) {
                int hid = r * 10 + i;

                String parent= "P=HOCA-"+hid;
                addAgent(DeviceNames.W1ID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=W1.drl"});
                addAgent(DeviceNames.SolarID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=SolarH1.drl"});
                addAgent(DeviceNames.BatteryID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
                addAgent(DeviceNames.GridID.getValue()+"-"+hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=GridH1.drl"});
                String parentH= "P=HCA-"+(i+((k>=3)?1:0));k++;
                addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{"C="+DeviceNames.W1ID.getValue()+"-"+hid,
                        "C="+DeviceNames.SolarID.getValue()+"-"+hid, "C="+DeviceNames.BatteryID.getValue()+"-"+hid,
                        "C="+DeviceNames.GridID.getValue()+"-"+hid, "F=HOCASimple.drl",parentH});
            }*/
        for (int hid=31;hid<=33;hid++) {
            String parent = "P=HOCA-" + hid;
            addAgent(DeviceNames.W1ID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=WDevice.drl", "T=dimmer", "priority=1"});
            addAgent(DeviceNames.SolarID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            addAgent(DeviceNames.BatteryID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            addAgent(DeviceNames.GridID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            //String parentH = "P=HCA-"+hid%10;
            if(hid==31)
                addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{"C=" + DeviceNames.W1ID.getValue() + "-" + hid,
                    "C=" + DeviceNames.SolarID.getValue() + "-" + hid, "C=" + DeviceNames.BatteryID.getValue() + "-" + hid,
                    "C=" + DeviceNames.GridID.getValue() + "-" + hid, "F=HOCASimple31.drl", "M=300","P=HCA-3", "downstep=10"});
            else
                addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{"C=" + DeviceNames.W1ID.getValue() + "-" + hid,
                        "C=" + DeviceNames.SolarID.getValue() + "-" + hid, "C=" + DeviceNames.BatteryID.getValue() + "-" + hid,
                        "C=" + DeviceNames.GridID.getValue() + "-" + hid, "F=HOCASimple3.drl", "M=300", "downstep=10","service=" + ServiceRequestTypes.joinR1});
        }
        for (int hid=41;hid<=41;hid++) {
            String parent = "P=HOCA-" + hid;
            addAgent(DeviceNames.W1ID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=WDevice.drl", "T=dimmer", "priority=1"});
            addAgent(DeviceNames.SolarID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            addAgent(DeviceNames.BatteryID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            addAgent(DeviceNames.GridID.getValue() + "-" + hid, "My.prototype.DeviceControllerAgent", new String[]{parent, simulator, "F=simpleDevice.drl"});
            parentH = "P=HCA-2";//+hid%10;
            if(hid==41)
                addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{"C=" + DeviceNames.W1ID.getValue() + "-" + hid,
                    "C=" + DeviceNames.SolarID.getValue() + "-" + hid, "C=" + DeviceNames.BatteryID.getValue() + "-" + hid,
                    "C=" + DeviceNames.GridID.getValue() + "-" + hid, "F=HOCASimple.drl", parentH, "M=10", "downstep=0.1"});
            else
                addAgent(parent.substring(2), "My.prototype.HouseControllerAgent", new String[]{"C=" + DeviceNames.W1ID.getValue() + "-" + hid,
                        "C=" + DeviceNames.SolarID.getValue() + "-" + hid, "C=" + DeviceNames.BatteryID.getValue() + "-" + hid,
                        "C=" + DeviceNames.GridID.getValue() + "-" + hid, "F=HOCASimple.drl", "M=10", "downstep=0.1","service=" + ServiceRequestTypes.joinR1});

        }

        AgentController ac = cController.createNewAgent("sn", Sniffer.class.getName(), null);
        ac.start();
        AgentController ac2 = cController.createNewAgent("rma", rma.class.getName(), null);
        ac2.start();

    }

    public static AgentController addAgent(String name, String type,Object[] args) throws StaleProxyException
    {
        AgentController ac = cController.createNewAgent( name, type, args);
        ac.start();
        return ac;
    }


} // End class
