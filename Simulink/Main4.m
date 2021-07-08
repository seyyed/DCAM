
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


TVID=8;
LightsID=7;
DishWasherID=6;
DryerID=5;
SolarID=4;
BatteryID=3;
GridID=1;
W1ID=9;
Fridge2ID=24;
HotWaterSystemID=25;
WashingID=27;
OvenID=26;
AirconID=20;
ComputerID=21;
FreezerID=22;
FridgeID=23;
BatteryChargeID=2;
bcwl=[];
for i=1:5
    for j=1:5
        setControllerValue(i*10+j,BatteryChargeID,90);
    end
end

i=1;
state=0;

set_param('a4','SimulationCommand','start','SimulationCommand','pause');

while(i<24*60*60-1)
    %%Check end simulation
    i=length(ConsumeDataH1R1(1).signals(6).values);
    t=ConsumeDataH1R1(1).signals(6).values(i);

    if(t==86400)
        break;
    end
    
    %%Get charge 
    if(i>1)
        for k=1:5
            for j=1:5
                if isempty(find(bcwl==k*10+j, 1))
                    setControllerValue(k*10+j,BatteryChargeID,eval(['LinkDataH',int2str(k),'R',int2str(j),'(1).signals(3).values(',num2str(i),')']));
                end 
            end
        end
    end 
    bcwl=[];

    
    %%Recive Next Request
    instr = tcp_receive_function();
    if(isempty(instr))
        continue;
    end
    
    %%Analyse Request
    instrStr=strjoin(instr{1}(3));
    it=strsplit(instrStr,', ');
     command=strjoin( it(1)); 
     
    if( strcmpi(command ,'GetAllState'))
        set_param('a4','SimulationCommand','continue','SimulationCommand','pause'); 
        i=length(ConsumeDataH1R1(1).signals(6).values);
        rstr="";
        for z=2:length(it)
            rstr= rstr+ it(z)+ "="+ getDeviceState2(it(z),i) +", ";
        end
         tcp_send_function(rstr + "t="+ num2str(t,8) );
          
    elseif( strcmpi(command ,'DoAllInstructs'))
         rstr="";
        for z=2:length(it)
            it2=strsplit(it(z)+"",'; ');
            newV=0;
            if(length(it2)==2)
                newV=str2double(it2(2));
            end
            agentid= strjoin( it2(1)); 
            dvn=strsplit(agentid,'-');
            dvnT=str2double( strjoin( dvn(1)));
            hid= str2double( dvn(2)+"");
            setControllerValue( hid ,dvnT,newV);
            if(dvnT==BatteryChargeID)
                bcwl=[bcwl ,hid];
            end
        end
         tcp_send_function(rstr + "t="+ num2str(t,8) );
         
        
   
       
       end

end




%% End the TCP connection

% Disconnect and clean up the server connection. 

