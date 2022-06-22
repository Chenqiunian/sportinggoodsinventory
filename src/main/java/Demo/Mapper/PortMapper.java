package Demo.Mapper;

import Demo.Entity.Port;

import java.util.List;

public interface PortMapper {

    int setPortState(int[] ids);

    List<Port> selectAll();

    List<Port> selectImport();

    List<Port> selectExport();

    int deleteByPrimaryKey(Integer id);

    int insert(Port record);

    int insertSelective(Port record);

    Port selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Port record);

    int updateByPrimaryKey(Port record);
}