package Demo.Mapper;

import Demo.Entity.UserOperation;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author Chenqiunian
 * Create 2022-03-24 14:34
 * Description
 */
@Repository
public class UserOperationDao {
    @Autowired(required = false)
    UserOperationMapper userOperationMapper;
    @Autowired
    SqlSession session;

    public int getNewId(){
        int i = 0;
        for(i = 1;i<1000;i++){
            UserOperation userOperation = userOperationMapper.selectByPrimaryKey(i);
            if(userOperation == null){
                break;
            }
        }
        return i;
    }

    public int insertSelective(UserOperation userOperation){
        int i = userOperationMapper.insertSelective(userOperation);
        return i;
    }

    public List<UserOperation> selectAll(){
        List<UserOperation> list = userOperationMapper.selectAll();
        return list;
    }

    public int deleteOperationById(int[] ids){
        int result = 0;
        for(int i = 0;i<ids.length;i++){
            if(userOperationMapper.deleteByPrimaryKey(ids[i])>0)
                result++;
        }
        return result;
    }
}
