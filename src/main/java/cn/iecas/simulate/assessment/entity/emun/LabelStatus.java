package cn.iecas.simulate.assessment.entity.emun;



/**
 * 影像标注状态
 * 待标注、审核中、完成、审核未通过
 */
public enum LabelStatus {

    AILABELING(0),UNAPPLIED(1),LABELING(2),UNCHECK(3),CHECKING(4),FINISH(5), FEEDBACK(6), DELETED(7);

    private int value;

    LabelStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public void setValue(int value){
        this.value = value;
    }
}
