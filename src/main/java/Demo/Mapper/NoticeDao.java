package Demo.Mapper;

import Demo.Entity.Notice;
import Demo.Entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author Chenqiunian
 * Date 2022-05-26 15:20
 * Description
 */

@Repository
public class NoticeDao {
    @Autowired(required = false)
    NoticeMapper noticeMapper;

    public List<Notice> selectAll(){
        List<Notice> noticeList = noticeMapper.selectAll();
        return noticeList;
    }

    public int getNewId(){
        int id = 0;
        for(int i = 1;i<1000;i++){
            Notice notice = noticeMapper.selectByPrimaryKey(i);
            if(notice == null){
                id = i;
                System.out.println("获取到公告新ID:"+id);
                break;
            }
        }
        return id;
    }

    public int insertNotice(Notice notice){
        int i = noticeMapper.insertSelective(notice);
        return i;
    }

    public int updateNotice(Notice notice){
        int i = noticeMapper.updateByPrimaryKeySelective(notice);
        return i;
    }

    public int deleteNotice(int[] ids){
        int result = 0;
        for(int i = 0; i<ids.length;i++){
            if(noticeMapper.deleteByPrimaryKey(ids[i])>0)
                result++;
        }
        return result;
    }

}
