package cn.iecas.simulate.assessment.common.typehandler;

import cn.iecas.simulate.assessment.entity.emun.LabelTaskType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class TaskTypeHandler extends BaseTypeHandler<LabelTaskType> {


    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, LabelTaskType labelTaskType, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i,labelTaskType.getValue());
    }


    @Override
    public LabelTaskType getNullableResult(ResultSet resultSet, String s) throws SQLException {
        int labelTaskTypeValue = resultSet.getInt(s);
        if (0 == labelTaskTypeValue)
            return LabelTaskType.LABEL;

        return LabelTaskType.CHECK;
    }


    @Override
    public LabelTaskType getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int labelTaskTypeValue = resultSet.getInt(i);
        if (0 == labelTaskTypeValue)
            return LabelTaskType.LABEL;

        return LabelTaskType.CHECK;
    }


    @Override
    public LabelTaskType getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int labelTaskTypeValue = callableStatement.getInt(i);
        if (0 == labelTaskTypeValue)
            return LabelTaskType.LABEL;

        return LabelTaskType.CHECK;
    }
}
