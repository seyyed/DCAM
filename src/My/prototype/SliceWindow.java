package My.prototype;

import My.Project.Thesis.Ontology.ControllerState;

public class SliceWindow {
    int ct = 0;
    ControllerState[] storage;
    boolean hasData=false;
    public SliceWindow(int size)
    {
        storage=new ControllerState[size];
    }
    public void put(ControllerState d)
    {
        storage[ct%storage.length]=d;
        ct= (ct+1)%storage.length;
    }
    public ControllerState  getLast()
    {
        return get(0);
    }
    public ControllerState get(int index)
    {
        if(ct-index-1>=0)
            return storage[(ct-index-1)%storage.length];
        else if(ct-index-1+storage.length>=0)
            return storage[ct-index-1+storage.length];
        return null;
    }

}
