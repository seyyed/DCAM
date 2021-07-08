function [y]=GetLastDeviceData(ScopeID,SignalID,counter,gid)
global g
g(gid)=1;
y= num2str(ScopeID(1).signals(SignalID).values(counter),4);