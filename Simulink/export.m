filename='LinkData980612-1.xlsx';
for k=1:6
    A=[];
    for i=1:5
        for j=1:5   
            A=[A,eval(['LinkDataH',int2str(i),'R',int2str(j),'(1).signals(',int2str(k),').values'])];
        end
    end
    xlswrite(filename,A,int2str(k));
end
