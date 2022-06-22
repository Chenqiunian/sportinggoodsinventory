package Demo.Mapper;

import Demo.Entity.Port;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author Chenqiunian
 * Create 2022-03-25 20:49
 * Description
 */
@Repository
public class PortDao {
    @Autowired(required = false)
    PortMapper portMapper;

    public List<Port> selectAll(){
        List<Port> list = portMapper.selectAll();
        return list;
    }

    public List<Port> selectImport(){
        List<Port> list = portMapper.selectImport();
        return list;
    }

    public List<Port> selectExport(){
        List<Port> list = portMapper.selectExport();
        return list;
    }

    public int getNewId(){
        int i;
        for(i = 1;i<1000;i++){
            Port port = portMapper.selectByPrimaryKey(i);
            if(port == null){
                break;
            }
        }
        return i;
    }

    public int insertRecord(Port port){
        int i = portMapper.insertSelective(port);
        return i;
    }

    public int deletePortRecord(int[] ids){
        int result = 0;
        for(int i = 0;i<ids.length;i++){
            if(portMapper.deleteByPrimaryKey(ids[i])>0)
                result++;
        }
        return result;
    }

    public int setPortState(int[] ids){
        int i = portMapper.setPortState(ids);
        return i;
    }

    public Port selectByPrimaryKey(int id){
        Port port = portMapper.selectByPrimaryKey(id);
        return port;
    }

    public int updatePort(Port port){
        int i = portMapper.updateByPrimaryKeySelective(port);
        return i;
    }


}
