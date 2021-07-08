function [s]= getDeviceState2(agentid,counter)
global TVID;
global LightsID;
global DishWasherID;
global DryerID;
global SolarID;
global BatteryID;
global GridID;
global W1ID;
global Fridge2ID;
global HotWaterSystemID;
global WashingID;
global OvenID;
global AirconID;
global ComputerID;
global FreezerID;
global FridgeID;
global BatteryChargeID;

s1='ConsumeData';
s2='LinkData';
dvn=strsplit(agentid+"",'-');
dvnT=str2double(strjoin( dvn(1)));
if(length(dvn)==1 || strlength(dvn(2))<2)
    disp("Invalid Device ID: " +agentid);
    s='';
    return;
end
hid= str2double( dvn(2)+"");

switch(dvnT)
        case TVID
              s=GetLastDeviceData2(hid,1,counter,s1);
        case LightsID
              s=GetLastDeviceData2(hid,2,counter,s1);
        case DishWasherID
              s=GetLastDeviceData2(hid,3,counter,s1);
        case DryerID
              s=GetLastDeviceData2(hid,4,counter,s1);
        case SolarID
              s=GetLastDeviceData2(hid,2,counter,s2);
        case BatteryID
              s=GetLastDeviceData2(hid,3,counter,s2);
        case GridID
              s=GetLastDeviceData2(hid,6,counter,s2);
        case W1ID
              s=GetLastDeviceData2(hid,1,counter,s1);
        case Fridge2ID
              s=GetLastDeviceData2(hid,10,counter,s1);
        case HotWaterSystemID
              s=GetLastDeviceData2(hid,9,counter,s1);
        case WashingID
              s=GetLastDeviceData2(hid,8,counter,s1);
        case OvenID
              s=GetLastDeviceData2(hid,7,counter,s1);
        case AirconID
              s=GetLastDeviceData2(hid,1,counter,s1);
        case ComputerID
              s=GetLastDeviceData2(hid,2,counter,s1);
        case FreezerID
              s=GetLastDeviceData2(hid,3,counter,s1);
        case FridgeID
              s=GetLastDeviceData2(hid,4,counter,s1);
        case BatteryChargeID
              s=GetLastDeviceData2(hid,3,counter,s2);
        otherwise
            disp("Invalid Device ID:" + agentid);
            s='';
            return;
end
s=s+":"+ getControllerValue(hid,dvnT);