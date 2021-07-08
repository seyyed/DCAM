function y=ProcessBatteryOutFunc(u,max)
bin= u(3);
if bin==0 && u(1)>0
    bin=u(1);
end
if bin+u(2)>=max
    y=max-u(2);
elseif u(2)+bin<0
    y=-u(2);
else
    y=bin;
end

 


