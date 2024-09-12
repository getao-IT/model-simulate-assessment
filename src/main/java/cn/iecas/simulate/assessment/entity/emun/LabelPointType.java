package cn.iecas.simulate.assessment.entity.emun;



public enum LabelPointType {

    GEODEGREE(0),PIXEL(1);

    private int value;

    LabelPointType(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public void setValue(int value){
        this.value = value;
    }
}
