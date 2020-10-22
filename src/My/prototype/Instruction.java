package My.prototype;

import My.Project.Thesis.Ontology.CommandTypes;
import My.Project.Thesis.Ontology.ConversationTypes;
import jade.core.AID;

public class Instruction {
    private final ConversationTypes convType;
    public Integer time;
    String content;
    AID subject;
    public String getContent() {

        return content;
    }
    public Instruction(AID subject, String content, ConversationTypes type,int time)
    {
        this.subject=subject;
        this.content=content;
        this.convType=type;
        this.time=time;
    }
    public AID getSubject() {
        return subject;
    }
    public ConversationTypes getConvType()
    {
        return convType;
    }
}
