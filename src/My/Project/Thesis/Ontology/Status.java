/**
 * Section 5.1.3.2 Page 85
 * Java class representing a Book 
 **/

// Class associated to the BOOK schema
package My.Project.Thesis.Ontology;

import jade.content.Concept;
import jade.util.leap.List;

public class Status implements Concept {
    private float currentLoad;
    private float maxLoad;
    private float currentProduce;


    public float getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(float currentLoad) {
        this.currentLoad = currentLoad;
    }

    public float getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(float maxLoad) {
        this.maxLoad = maxLoad;
    }

    public float getCurrentProduce() {
        return currentProduce;
    }

    public void setCurrentProduce(float currentProduce) {
        this.currentProduce = currentProduce;
    }


}
