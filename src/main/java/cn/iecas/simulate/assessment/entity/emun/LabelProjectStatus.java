package cn.iecas.simulate.assessment.entity.emun;



public enum LabelProjectStatus {

    AILABELING(0),LABELING(1),FINISH(2);

    private int value;

    LabelProjectStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public void setValue(int value){
        this.value = value;
    }
}
