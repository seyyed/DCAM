function y=LookupTable(u)
global MonitoringData
 t=MonitoringData(MonitoringData(:,1)==u(1)& MonitoringData(:,2)==u(2) & MonitoringData(:,3)<=u(3),4);
  y=double(t(end));
end 

