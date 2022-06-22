package Demo.Mapper;

import Demo.Entity.LoginRecord;

import java.util.Date;
import java.util.List;

public interface LoginRecordMapper {

    int updateStateByIp(String state,String ip);

    LoginRecord selectByUsername(String username);

    List<LoginRecord> selectAll();

    LoginRecord selectByIp(String ip);

    int deleteByPrimaryKey(Integer id);

    int insert(LoginRecord record);

    int insertSelective(LoginRecord record);

    LoginRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LoginRecord record);

    int updateTimesByIp(String ip);

    int updateDateByIp(Date date, String ip);
}