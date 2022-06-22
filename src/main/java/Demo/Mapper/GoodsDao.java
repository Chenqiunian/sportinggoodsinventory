package Demo.Mapper;

import Demo.Entity.Goods;
import Demo.Tool.GetTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author Chenqiunian
 * Create 2022-03-25 20:05
 * Description
 */
@Repository
public class GoodsDao {
    @Autowired(required = false)
    GoodsMapper goodsMapper;

    private String imageFolderPath = System.getProperty("user.dir")+"\\src\\main\\resources\\static\\images\\";;

    @Autowired
    GetTime getTime;

    private static GoodsDao goodsDao;

    @PostConstruct
    public void init(){
        goodsDao = this;
        goodsDao.getTime = this.getTime;
    }

    public List<Goods> selectAll(){
        List<Goods> list = goodsMapper.selectAll();
        return list;
    }

    public Goods selectByNameAndSupplier(String name,String supplier){
        Goods goods = goodsMapper.selectByNameAndSupplier(name,supplier);
        return goods;
    }

    public int insertGoods(Goods goods){
        int i = goodsMapper.insertSelective(goods);
        return i;
    }

    public int getNewId(){
        int i;
        for(i = 1;i<1000;i++){
            Goods goods = goodsMapper.selectByPrimaryKey(i);
            if(goods == null){
                break;
            }
        }
        return i;
    }

    public Goods selectByPrimaryKey(int id){
        Goods goods = goodsMapper.selectByPrimaryKey(id);
        return goods;
    }

    public int updateGoods(Goods goods){
        int i = goodsMapper.updateByPrimaryKeySelective(goods);
        return i;
    }

    public int deleteGoods(int[] ids){
        int result = 0;
        //记录商品图片名
        List<String> imageList = new ArrayList<>();
        for(int i = 0;i<ids.length;i++){
            String imageString = selectByPrimaryKey(ids[i]).getImage();
            if(goodsMapper.deleteByPrimaryKey(ids[i])>0) {
                imageList.add(imageString.substring(imageString.lastIndexOf("/")+1));
                result++;
            }
        }
        //把删除掉的商品图片标记为30天后删除
        for(int j = 0;j<imageList.size();j++){
            //新文件名：旧文件名+删除时间
            String fileName = imageList.get(j).substring(0, imageList.get(j).lastIndexOf("."));
            String suffix = imageList.get(j).substring(imageList.get(j).lastIndexOf("."));
            File newFile = new File(imageFolderPath,fileName+goodsDao.getTime.Gettime()+suffix);
            File oldFile = new File(imageFolderPath,imageList.get(j));
            oldFile.renameTo(newFile);
        }

        //把删除超过30天的图片删掉
        File dir = new File(imageFolderPath);
        String[] children = dir.list();
        if(children.length>0){
            for(int k = 0;k<children.length;k++){
                if(children[k].length()>42){
                    System.out.println(children[k]);
                    //获取时间
                    String date = children[k].substring(children[k].lastIndexOf(".")-14, children[k].lastIndexOf("."));
                    //转换为时间格式
                    Date date1 = goodsDao.getTime.getNumberToDate(date);
                    //获取过去了多久,30天=2592000秒
                    long pastTime = goodsDao.getTime.pasttime(date1);
                    if(pastTime>=2592000){
                        File image = new File(imageFolderPath, children[k]);
                        image.delete();
                    }
                }
            }
        }


        return result;
    }

    public int setGoodsDelete(String state,int[] ids){
        int i = goodsMapper.setGoodsState(state,ids);
        return i;
    }
}
