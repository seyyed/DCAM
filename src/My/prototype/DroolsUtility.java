package My.prototype;

import My.Project.Thesis.Ontology.ControllerFact;
import My.Project.Thesis.Ontology.LoadProposal;
import org.drools.core.spi.KnowledgeHelper;

public class DroolsUtility {
    public static void help(final KnowledgeHelper drools, final String message){
       // System.out.println("\n["+message+"] :rule triggered: " + drools.getRule().getName());
    }
    public static < E > void  help(final KnowledgeHelper drools, final String message,final E fact){
    //    System.out.println("\n["+message+"] :rule triggered: " + drools.getRule().getName()+" fact="+fact);
    }
    public static void helper(final KnowledgeHelper drools){
      //  System.out.println("\nrule triggered: " + drools.getRule().getName());
    }
    public static < E > void  help(final KnowledgeHelper drools,final String message, final E fact1,final E fact2){
        System.out.println("\n["+message+"] :rule triggered: " + drools.getRule().getName()+"\nfact1="+fact1+"\nfact2="+fact2);
    }
}
