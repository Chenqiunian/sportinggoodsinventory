package Demo.Mapper;

import Demo.Entity.User;

import java.util.List;

public interface UserMapper {

    User selectByEmailAddress(String email);

    User userLogin(String username,String password);

    User checkExist(String username);

    List<User> selectall();

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updatePasswordById(int id,String password);

    int updateByPrimaryKey(User record);

    User selectByEmail(String email);
}