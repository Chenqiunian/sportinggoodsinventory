package Demo.Mapper;

import Demo.Entity.LoginRecord;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Author Chenqiunian
 * Create 2022-03-02 16:14
 */
@Repository
public class LoginRecordDao {
    @Autowired(required = false)
    LoginRecordMapper loginRecordMapper;
    @Autowired
    SqlSession session;

    public List<LoginRecord> selectAll(){
        return loginRecordMapper.selectAll();
    }

    public LoginRecord selectByIp(String ip){
        return loginRecordMapper.selectByIp(ip);
    }

    public int insertSelective(LoginRecord loginRecord){
        loginRecord.setId(getNewId());
        int i = loginRecordMapper.insertSelective(loginRecord);
        return i;
    }

    public int updateByPrimaryKeySelective(LoginRecord loginRecord){
        int i = loginRecordMapper.updateByPrimaryKeySelective(loginRecord);
        return i;
    }

    public LoginRecord selectByPrimaryKey(int id){
        return loginRecordMapper.selectByPrimaryKey(id);
    }

    public int getNewId(){
        for(int i = 1;i<50000;i++){
            if(selectByPrimaryKey(i)==null)
                return i;
        }
        return -1;
    }

    public int deleteIp(int[] ids){
        int result = 0;
        for(int i = 0;i<ids.length;i++){
            int j = loginRecordMapper.deleteByPrimaryKey(ids[i]);
            if(j>0)
                result++;
        }
        return result;
    }

    public LoginRecord selectByUsername(String username){
        LoginRecord loginRecord = loginRecordMapper.selectByUsername(username);
        return loginRecord;
    }

    public int updateStateByIp(String state,String ip){
        int i = loginRecordMapper.updateStateByIp(state, ip);
        return i;
    }

    public int updateTimesByIp(String ip){
        int i = loginRecordMapper.updateTimesByIp(ip);
        return i;
    }

    public int updateDateByIp(String ip){
        int i = loginRecordMapper.updateDateByIp(new Date(),ip);
        return i;
    }

}
