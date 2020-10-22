package My.Project.Thesis.Ontology;


public class ControllerFactMember {
    public PCState proposal;
    public boolean acceptedProposal =false;
    public boolean haveNewProposal=false;
    public String ID="";

    public void AddPropsal(PCState dh) {
        proposal=dh;
        haveNewProposal=true;

    }
    public void AcceptProposal() {
        haveNewProposal=false;
        acceptedProposal =true;
    }
    public void RejectProposal(){
        haveNewProposal=false;
        acceptedProposal =false;
    }
}
