package My.prototype;

import My.Project.Thesis.Ontology.*;
import jade.core.AID;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.conf.ConstraintJittingThresholdOption;
import org.kie.internal.io.ResourceFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

public abstract class PolicyManagementService {
    ControllerAgent agent;
    float changeStep=1f;
    float downStep=1f;

    KieSession kieSession;
    InternalKnowledgeBase kbase;
    ControllerFact fact;
    protected FactHandle currentInstructHandle;
    protected FactHandle currentStateHandle;

    public boolean isGoodCondition() {


        return fact.isGoodCondition;
    }

    protected void InitInsertFacts(){
        if(kieSession!=null)
            kieSession.destroy();
        kieSession = kbase.newKieSession();

        fact.leftHolon= fact.needsChangeLoad= fact.needsChangeDown=fact.needsChangeUp=fact.needsChangeSource=fact.needsChangeStoage=false;
        fact.isGoodCondition=true;
        fact.maxLoad=agent.maxLoad;
        fact.isacceptedProposal=false;
        kieSession.insert(fact);
        currentInstructHandle = null;
        currentStateHandle = null;
    }
    protected  void EndInsertFacts(){
        kieSession.fireAllRules();
        kieSession.dispose();
    }

    protected abstract void InsertFact();

    public void setup(ControllerAgent controllerAgent, String pFileName) throws IOException {
        agent=controllerAgent;
        fact= new ControllerFact();
        fact.ID=agent.getLocalName();

        if(agent.deviceType == DeviceTypes.dimmer)
            downStep=0.2f;
        String rules=new String(Files.readAllBytes( FileSystems.getDefault().getPath( pFileName)));
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource( rules.getBytes()), ResourceType.DRL );
        if ( kbuilder.hasErrors() ) {
            System.out.println( kbuilder.getErrors().toString() );
        }


        Collection<KiePackage> pkgs = kbuilder.getKnowledgePackages();
        kbase.addPackages( pkgs );


        Collection<KiePackage> kiePackages = kbase.getKiePackages();
        for( KiePackage kiePackage: kiePackages ){
            for( Rule rule: kiePackage.getRules() ){
                System.out.println(agent.getLocalName()+" : "+ rule.getName() );
            }
        }
    }

    public boolean canDoAdaption() {
        return true;
    }

    public boolean canDoInstruct(AID sender) {

        return !fact.isGoodCondition;
    }

    public abstract List<Instruction> plan();




    protected Instruction getSimInstruct(DeviceInstruct instruct) {

        return new Instruction(agent.SimulatorID,instruct.toString(),ConversationTypes.instr,instruct.time);
    }



}
