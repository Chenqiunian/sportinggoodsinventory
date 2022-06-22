package Demo.Mapper;

import Demo.Entity.UserOperation;

import java.util.List;

public interface UserOperationMapper {

    List<UserOperation> selectAll();

    int deleteByPrimaryKey(Integer id);

    int insert(UserOperation record);

    int insertSelective(UserOperation record);

    UserOperation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserOperation record);

    int updateByPrimaryKey(UserOperation record);
}