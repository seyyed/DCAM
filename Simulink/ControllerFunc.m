function y=ControllerFunc(u)
global MappingControllers
 y=double(MappingControllers(MappingControllers(:,1)==u(2) & MappingControllers(:,2)==u(1),3));


