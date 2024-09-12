package cn.iecas.simulate.assessment.common.typehandler;

import cn.iecas.simulate.assessment.entity.emun.LabelProjectStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class LabelProjectStatusTypeHandler extends BaseTypeHandler<LabelProjectStatus> {


    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, LabelProjectStatus labelProjectStatus, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i,labelProjectStatus.getValue());
    }


    @Override
    public LabelProjectStatus getNullableResult(ResultSet resultSet, String s) throws SQLException {
        int labelProjectStatusValue = resultSet.getInt(s);
        switch (labelProjectStatusValue){
            case 0: return LabelProjectStatus.AILABELING;
            case 1: return LabelProjectStatus.LABELING;
            case 2: return LabelProjectStatus.FINISH;
        }

        return LabelProjectStatus.FINISH;
    }


    @Override
    public LabelProjectStatus getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int labelProjectStatusValue = resultSet.getInt(i);
        switch (labelProjectStatusValue){
            case 0: return LabelProjectStatus.AILABELING;
            case 1: return LabelProjectStatus.LABELING;
            case 2: return LabelProjectStatus.FINISH;
        }

        return LabelProjectStatus.FINISH;
    }


    @Override
    public LabelProjectStatus getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int labelProjectStatusValue = callableStatement.getInt(i);
        switch (labelProjectStatusValue){
            case 0: return LabelProjectStatus.AILABELING;
            case 1: return LabelProjectStatus.LABELING;
            case 2: return LabelProjectStatus.FINISH;
        }

        return LabelProjectStatus.FINISH;
    }
}
