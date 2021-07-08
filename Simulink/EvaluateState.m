function EvaluateState(command,ScopeID,SignalID,counter,gid,HouseID,CID,value,t)

if( strcmpi(command ,'GetState'))
 sendValue=GetLastDeviceData(ScopeID,SignalID,counter,gid);
 tcp_send_function("t="+ num2str(t,8) +", c="+ sendValue);
elseif( strcmpi(command ,'instr'))
     setControllerValue(HouseID,CID,value);
      tcp_send_function("t=" + num2str(t,8));
end