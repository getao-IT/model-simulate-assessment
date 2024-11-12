package cn.iecas.simulate.assessment.entity.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;



@Data
@AllArgsConstructor
public class PageResult<T> {

    private long pageNo;

    private long totalCount;

    private List<T> result;

    private List<String> otherInfos;

    public PageResult(long pageNo, long totalCount, List<T> result) {
        this.pageNo = pageNo;
        this.totalCount = totalCount;
        this.result = result;
    }
}
