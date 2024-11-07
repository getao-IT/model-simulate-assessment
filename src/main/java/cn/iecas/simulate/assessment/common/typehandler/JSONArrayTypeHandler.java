package cn.iecas.simulate.assessment.common.typehandler;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class JSONArrayTypeHandler extends BaseTypeHandler<JSONArray> {


    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, JSONArray array, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, array.toString());
    }


    @Override
    public JSONArray getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String string = resultSet.getString(s);
        if (string == null || string.equalsIgnoreCase(""))
            return new JSONArray();
        return JSONArray.parseArray(string);
    }


    @Override
    public JSONArray getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String string = resultSet.getString(i);
        if (string == null || string.equalsIgnoreCase(""))
            return new JSONArray();
        return JSONArray.parseArray(string);
    }


    @Override
    public JSONArray getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String string = callableStatement.getString(i);
        if (string == null || string.equalsIgnoreCase(""))
            return new JSONArray();
        return JSONArray.parseArray(string);
    }
}
