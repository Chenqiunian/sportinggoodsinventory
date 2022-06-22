package Demo.Mapper;

import Demo.Entity.Notice;

import java.util.List;

public interface NoticeMapper {

    List<Notice> selectAll();

    int deleteByPrimaryKey(Integer noticeid);

    int insert(Notice record);

    int insertSelective(Notice record);

    Notice selectByPrimaryKey(Integer noticeid);

    int updateByPrimaryKeySelective(Notice record);

    int updateByPrimaryKey(Notice record);
}