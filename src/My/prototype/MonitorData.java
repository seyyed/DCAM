package My.prototype;

import My.Project.Thesis.Ontology.GridMemberType;

public class MonitorData {
    float load;
    int time;
     float LC;
     float produceLC;
     float produce;
     GridMemberType type;

    @Override
    public String toString()
    {
        return "l="+load+" ,t="+time+", lc="+LC+", p="+produce+", type="+type+", plc="+produceLC;
    }
}
