function y = tcp_receive_function()
% Thanks to Sifakis Nikolaos for imrovement suggestions 

while(1)
    try
  %t = tcpip('192.168.84.23', 8080);       
  t = tcpip('localhost', 1234); 
% Open connection to the server.
pause(0.1);
set(t,'InputBufferSize',4096);
fopen(t);

break;
    catch
        fclose(t); 
        delete(t); 
    end
end
%disp('Connection established')
message = '';
content = '';
 
% Implement a waiting process
i=0;
while t.BytesAvailable == 0
    i=i+1;
    pause(0.1)
    if(i>20)
        break;
    end
end
% Receive lines of data from server
while (get(t, 'BytesAvailable') > 0) 
DataReceived = fread(t,t.BytesAvailable);
message = strcat(message,DataReceived');
end
 
if(size(message)>0)
    content = textscan(message, '%*s %s %*s', 'delimiter', '"');    
end
 
y = content;
fclose(t); 
delete(t); 
clear t 