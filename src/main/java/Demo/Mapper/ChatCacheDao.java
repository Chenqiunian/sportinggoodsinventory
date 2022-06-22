package Demo.Mapper;

import Demo.Entity.ChatCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author Chenqiunian
 * Date 2022-06-09 14:59
 * Description
 */
@Repository
public class ChatCacheDao {
    @Autowired(required = false)
    ChatCacheMapper chatCacheMapper;

    public List<ChatCache> selectAll(){
        List<ChatCache> list = chatCacheMapper.selectAll();
        return list;
    }

    public int getNewId(){
        int i = 1;
        boolean bool = true;
        while(bool){
            if(chatCacheMapper.selectByPrimaryKey(i)==null)
                bool = false;
            else
                i++;
        }
        return i;
    }

    public int insertSelective(ChatCache chatCache){
        return chatCacheMapper.insertSelective(chatCache);
    }

    public List<ChatCache> selectByAcceptUserId(String id){
        return chatCacheMapper.selectByAcceptUserId(Integer.parseInt(id));
    }

    public int deleteByAcceptUserId(String id){
        return chatCacheMapper.deleteByAcceptUserId(Integer.parseInt(id));
    }

}
