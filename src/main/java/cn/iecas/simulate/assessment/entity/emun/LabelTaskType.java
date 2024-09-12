package cn.iecas.simulate.assessment.entity.emun;



/**
 * 任务类型
 * 标注、审核
 */

public enum LabelTaskType {

    LABEL(0),CHECK(1);

    private int value;

    LabelTaskType(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public void setValue(int value){
        this.value = value;
    }
}
