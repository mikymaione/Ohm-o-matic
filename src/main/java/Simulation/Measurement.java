package Simulation;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("b6f689bc-3c19-459d-8f18-6a870df223bb")
public class Measurement implements Comparable<Measurement> {
    @objid ("34273501-e4d2-456c-9e63-e02d52248f8b")
    private String id;

    @objid ("ba375ae7-cf12-411e-b081-4665d3743376")
    private String type;

    @objid ("6d48c1de-f5a6-4ed1-bbeb-72de19137f48")
    private double value;

    @objid ("96239922-6c8e-4c83-bf69-762b15f0f01f")
    private long timestamp;

    @objid ("3339c634-1054-40f4-a493-5f046153e657")
    public Measurement(String id, String type, double value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
        this.id = id;
        this.type = type;
    }

    @objid ("f61f3ccd-c565-4e31-92c1-ba9a2e352c34")
    public double getValue() {
        return value;
    }

    @objid ("707acfbf-add4-4379-8d63-e8ed2e880e3a")
    public void setValue(double value) {
        this.value = value;
    }

    @objid ("4c531cc9-0b73-4a15-a122-c12fdc417f56")
    public long getTimestamp() {
        return timestamp;
    }

    @objid ("b17e8ebe-33e7-47b5-8287-4ac643dbc1cb")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @objid ("da45d937-9c8d-41ab-9575-498a50965549")
    public String getId() {
        return id;
    }

    @objid ("d2e14692-6da4-4867-ac7d-b6be054e50b9")
    public void setId(String type) {
        this.id = type;
    }

    @objid ("ff497c08-d0bc-4766-83c4-5540694de417")
    public String getType() {
        return type;
    }

    @objid ("72b01eef-4658-433e-858c-865fc480b306")
    public void setType(String type) {
        this.type = type;
    }

    @objid ("439bd39a-aadf-4ea0-afff-c9a6226b3f68")
    @Override
    public int compareTo(Measurement m) {
        Long thisTimestamp = timestamp;
        Long otherTimestamp = m.getTimestamp();
        return thisTimestamp.compareTo(otherTimestamp);
    }

    @objid ("c4c6c369-7332-4fb9-bf20-d7708e021569")
    public String toString() {
        return value + " " + timestamp;
    }

}
