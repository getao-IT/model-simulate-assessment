package cn.iecas.simulate.assessment.common.typehandler;

import cn.iecas.simulate.assessment.entity.emun.LabelPointType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class LabelPointTypeHandler extends BaseTypeHandler<LabelPointType> {


    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, LabelPointType labelPointType, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i,labelPointType.getValue());
    }


    @Override
    public LabelPointType getNullableResult(ResultSet resultSet, String s) throws SQLException {
        int labelTaskTypeValue = resultSet.getInt(s);
        if (0 == labelTaskTypeValue)
            return LabelPointType.GEODEGREE;

        return LabelPointType.PIXEL;
    }


    @Override
    public LabelPointType getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int labelTaskTypeValue = resultSet.getInt(i);
        if (0 == labelTaskTypeValue)
            return LabelPointType.GEODEGREE;

        return LabelPointType.PIXEL;
    }


    @Override
    public LabelPointType getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int labelTaskTypeValue = callableStatement.getInt(i);
        if (0 == labelTaskTypeValue)
            return LabelPointType.GEODEGREE;

        return LabelPointType.PIXEL;
    }
}
