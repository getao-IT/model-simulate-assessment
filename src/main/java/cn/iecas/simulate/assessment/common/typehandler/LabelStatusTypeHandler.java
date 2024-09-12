package cn.iecas.simulate.assessment.common.typehandler;

import cn.iecas.simulate.assessment.entity.emun.LabelStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class LabelStatusTypeHandler extends BaseTypeHandler<LabelStatus> {


    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, LabelStatus labelStatus, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i,labelStatus.getValue());
    }


    @Override
    public LabelStatus getNullableResult(ResultSet resultSet, String s) throws SQLException {
        int labelStatusValue = resultSet.getInt(s);
        switch (labelStatusValue){
            case 0: return LabelStatus.AILABELING;
            case 1: return LabelStatus.UNAPPLIED;
            case 2: return LabelStatus.LABELING;
            case 3: return LabelStatus.UNCHECK;
            case 4: return LabelStatus.CHECKING;
            case 5: return LabelStatus.FINISH;
            case 6: return LabelStatus.FEEDBACK;
            case 7: return LabelStatus.DELETED;
        }
        return LabelStatus.UNAPPLIED;
    }


    @Override
    public LabelStatus getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int labelStatusValue = resultSet.getInt(i);
        switch (labelStatusValue){
            case 0: return LabelStatus.AILABELING;
            case 1: return LabelStatus.UNAPPLIED;
            case 2: return LabelStatus.LABELING;
            case 3: return LabelStatus.UNCHECK;
            case 4: return LabelStatus.CHECKING;
            case 5: return LabelStatus.FINISH;
            case 6: return LabelStatus.FEEDBACK;
            case 7: return LabelStatus.DELETED;
        }
        return LabelStatus.UNAPPLIED;
    }


    @Override
    public LabelStatus getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int labelStatusValue = callableStatement.getInt(i);
        switch (labelStatusValue){
            case 0: return LabelStatus.AILABELING;
            case 1: return LabelStatus.UNAPPLIED;
            case 2: return LabelStatus.LABELING;
            case 3: return LabelStatus.UNCHECK;
            case 4: return LabelStatus.CHECKING;
            case 5: return LabelStatus.FINISH;
            case 6: return LabelStatus.FEEDBACK;
            case 7: return LabelStatus.DELETED;
        }
        return LabelStatus.UNAPPLIED;
    }
}
