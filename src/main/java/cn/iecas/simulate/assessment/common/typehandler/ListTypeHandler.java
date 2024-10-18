package cn.iecas.simulate.assessment.common.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;



public class ListTypeHandler extends BaseTypeHandler<List> {


    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List list, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, list.toString());
    }


    @Override
    public List getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String string = resultSet.getString(s);
        if (string == null || string.equalsIgnoreCase(""))
            return new ArrayList();
        List<Integer> result = Arrays.stream(string.replace(" ", "").replace("[", "").replace("]", "")
                .split(",")).map(Integer::parseInt).collect(Collectors.toList());
        return result;
    }


    @Override
    public List getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String string = resultSet.getString(i);
        if (string == null || string.equalsIgnoreCase(""))
            return new ArrayList();
        List<Integer> result = Arrays.stream(string.replace(" ", "").replace("[", "").replace("]", "")
                .split(",")).map(Integer::parseInt).collect(Collectors.toList());
        return result;
    }


    @Override
    public List getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String string = callableStatement.getString(i);
        if (string == null || string.equalsIgnoreCase(""))
            return new ArrayList();
        List<Integer> result = Arrays.stream(string.replace(" ", "").replace("[", "").replace("]", "")
                .split(",")).map(Integer::parseInt).collect(Collectors.toList());
        return result;
    }
}
