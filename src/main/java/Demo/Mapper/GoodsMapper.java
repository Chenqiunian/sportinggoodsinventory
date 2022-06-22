package Demo.Mapper;

import Demo.Entity.Goods;

import java.util.List;

public interface GoodsMapper {

    int setGoodsState(String state,int[] ids);

    List<Goods> selectAll();

    Goods selectByNameAndSupplier(String name,String supplier);

    int deleteByPrimaryKey(Integer id);

    int insert(Goods record);

    int insertSelective(Goods record);

    Goods selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Goods record);
}