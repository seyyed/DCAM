function [y]=GetLastDeviceData2(hid,SignalID,counter,sName)

y=num2str(evalin('base',[sName,'H',int2str(floor(hid/10)),'R',int2str(mod(hid,10)),'(1).signals(',num2str( SignalID),').values(',num2str(counter),')']),4);
