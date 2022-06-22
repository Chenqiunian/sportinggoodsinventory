package Demo.Mapper;

import Demo.Entity.ChatCache;

import java.util.List;

public interface ChatCacheMapper {

    int deleteByAcceptUserId(int acceptUserId);

    List<ChatCache> selectByAcceptUserId(int acceptUserId);

    List<ChatCache> selectAll();

    int deleteByPrimaryKey(Integer id);

    int insert(ChatCache record);

    int insertSelective(ChatCache record);

    ChatCache selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ChatCache record);

    int updateByPrimaryKey(ChatCache record);
}