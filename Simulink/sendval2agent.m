function sendval2agent(s,agentid, i) 
if (strcmpi( s,agentid)==1)
  tcp_send_function(num2str(ScopeLampData(1).get(1).Values(i).Data));
 fprintf('Send to%s',agentid)
end

