package Demo;

import Demo.Entity.Goods;
import Demo.Entity.LoginRecord;
import Demo.Entity.User;
import Demo.Mapper.LoginRecordDao;
import Demo.Tool.GetTime;
import Demo.Tool.IpUtil;
import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.servlet.jsp.tagext.TryCatchFinally;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Author Chenqiunian
 * Create 2022-03-02 13:19
 */
public class TestJava {

    public static void main(String[] args) {
        boolean bool = true;
        int i = 1;
        while(bool){
            i++;
            if(i == 100)
                bool = false;
        }
        System.out.println(i);
    }
}
