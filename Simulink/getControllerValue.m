function [s] = getControllerValue(HouseID,DeviceID)
global MappingControllers
s= MappingControllers(MappingControllers(:,1)==HouseID & MappingControllers(:,2)==DeviceID,3);