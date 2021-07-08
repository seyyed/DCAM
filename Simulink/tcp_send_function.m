function tcp_send_function(text)
% Thanks to Sifakis Nikolaos for imrovement suggestions 
 %t = tcpip('192.168.84.23', 8080);
 t = tcpip('localhost', 1234);
while(1)
% Open connection to the server.
try
pause(0.1);
set(t,'OutputBufferSize',4096);
fopen(t);

break;
catch Me
    disp(Me);
end
end
%disp('Connection established')
% Transmit data to the server (or a request for data from the server). 
fprintf(t, text); 

fclose(t); 
delete(t); 
clear t 
end