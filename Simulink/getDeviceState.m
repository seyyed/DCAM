function [s]= getDeviceState(agentid,counter,ConsumeDataH1,ConsumeDataH2,ConsumeDataH3,ConsumeDataH4,LinkDataH1,LinkDataH2,LinkDataH3,LinkDataH4)
global House1ID;
global House2ID;
global House3ID;
global House4ID;
global TVID;
global LightsID;
global DishWasherID;
global DryerID;
global SolarID;
global BatteryID;
global GridID;
global LinkInID;
global W1ID;
global LinkOutID;
switch(agentid+"")
        case 'TVH1'
               s=GetLastDeviceData(ConsumeDataH1,1,counter,1);
               s=s+":"+ getControllerValue(House1ID,TVID);
        case 'LightsH1'
              s=GetLastDeviceData(ConsumeDataH1,2,counter,1);
              s=s+":"+ getControllerValue(House1ID,LightsID);
        case 'DishWasherH1'
              s=GetLastDeviceData(ConsumeDataH1,3,counter,1);
              s=s+":"+ getControllerValue(House1ID,DishWasherID);
        case 'DryerH1'
              s=GetLastDeviceData(ConsumeDataH1,4,counter,1);
              s=s+":"+ getControllerValue(House1ID,DryerID);
        case 'SolarH1'
              s=GetLastDeviceData(LinkDataH1,6,counter,1);
              s=s+":"+ getControllerValue(House1ID,SolarID);
        case 'BatteryH1'
              s=GetLastDeviceData(LinkDataH1,3,counter,1);
              s=s+":"+ getControllerValue(House1ID,BatteryID);
        case 'GridH1'
              s=GetLastDeviceData(LinkDataH1,4,counter,1);
              s=s+":"+ getControllerValue(House1ID,GridID);
        case 'TVH2'
              s=GetLastDeviceData(ConsumeDataH2,1,counter,1);
               s=s+":"+ getControllerValue(House2ID,TVID);
        case 'LightsH2'
              s=GetLastDeviceData(ConsumeDataH2,2,counter,1);
               s=s+":"+ getControllerValue(House2ID,LightsID);
        case 'DishWasherH2'
              s=GetLastDeviceData(ConsumeDataH2,3,counter,1);
               s=s+":"+ getControllerValue(House2ID,DishWasherID);
        case 'DryerH2'
              s=GetLastDeviceData(ConsumeDataH2,4,counter,1);
               s=s+":"+ getControllerValue(House2ID,DryerID);
         case 'SolarH2'
               s=GetLastDeviceData(LinkDataH2,6,counter,1);
                s=s+":"+ getControllerValue(House2ID,SolarID);
        case 'BatteryH2'
              s=GetLastDeviceData(LinkDataH2,3,counter,1);
               s=s+":"+ getControllerValue(House2ID,BatteryID);
        case 'GridH2'
              s=GetLastDeviceData(LinkDataH2,4,counter,1);
               s=s+":"+ getControllerValue(House2ID,GridID);
        case 'W1H3'
              s=GetLastDeviceData(ConsumeDataH3,1,counter,1);
               s=s+":"+ getControllerValue(House3ID,W1ID);
       case 'SolarH3'
             s=GetLastDeviceData(LinkDataH3,6,counter,1);
              s=s+":"+ getControllerValue(House3ID,SolarID);
        case 'BatteryH3'
              s=GetLastDeviceData(LinkDataH3,3,counter,1);
               s=s+":"+ getControllerValue(House3ID,BatteryID);
        case 'GridH3'
              s=GetLastDeviceData(LinkDataH3,4,counter,1);
               s=s+":"+ getControllerValue(House3ID,GridID);
        case 'LinkInH3'
              s=GetLastDeviceData(LinkDataH3,7,counter,1);
               s=s+":"+ getControllerValue(House3ID,LinkInID);
        case 'LinkOutH3'
              s=GetLastDeviceData(ConsumeDataH3,3,counter,1);
               s=s+":"+ getControllerValue(House3ID,LinkOutID);
        case 'W1H4'
              s=GetLastDeviceData(ConsumeDataH4,1,counter,1);
               s=s+":"+ getControllerValue(House4ID,W1ID);
       case 'SolarH4'
             s=GetLastDeviceData(LinkDataH4,6,counter,1);
              s=s+":"+ getControllerValue(House4ID,SolarID);
        case 'BatteryH4'
              s=GetLastDeviceData(LinkDataH4,3,counter,1);
               s=s+":"+ getControllerValue(House4ID,BatteryID);
        case 'GridH4'
              s=GetLastDeviceData(LinkDataH4,4,counter,1);
               s=s+":"+ getControllerValue(House4ID,GridID);
        case 'LinkInH4'
              s=GetLastDeviceData(LinkDataH4,7,counter,1);
               s=s+":"+ getControllerValue(House4ID,LinkInID);
        case 'LinkOutH4'
              s=GetLastDeviceData(ConsumeDataH4,3,counter,1);
               s=s+":"+ getControllerValue(House4ID,LinkOutID);
        otherwise
            disp('Invalid Device ID')
end