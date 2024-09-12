package cn.iecas.simulate.assessment.entity.common;



/**
 * @author vanishrain
 * 数据集类型
 */
public enum DatasetType {

    IMAGE("IMAGE"), VIDEO("VIDEO"), TEXT("TEXT"), AUDIO("AUDIO"), ELEC("ELEC");

    private String value;

    DatasetType(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }

    public void setValue(String value){
        this.value = value;
    }
}
