function setControllerValue(HouseID,DeviceID,value)
global MappingControllers
MappingControllers(MappingControllers(:,1)==HouseID & MappingControllers(:,2)==DeviceID,3)=value;