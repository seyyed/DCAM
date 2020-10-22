package My.Project.Thesis.Ontology;

import jade.core.AID;

public class supplyRequest {
    private float value;
    private boolean isAccepted;

    public supplyRequest( float value, boolean isAccepted) {
        setAccepted(isAccepted);
        setValue(value);
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }


}