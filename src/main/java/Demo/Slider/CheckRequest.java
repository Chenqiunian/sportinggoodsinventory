package Demo.Slider;

import Demo.Entity.LoginRecord;
import Demo.Mapper.LoginRecordDao;
import Demo.Tool.DeviceUtil;
import Demo.Tool.GetTime;
import Demo.Tool.IpUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Component;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Console;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * Author Chenqiunian
 * Create 2022-03-01 22:35
 */

@Component
public class CheckRequest {

    @Autowired
    private LoginRecordDao loginRecordDao;
    @Autowired
    private IpUtil ipUtil;
    @Autowired
    private DeviceUtil deviceUtil;
    @Autowired
    private GetTime getTime;

    public HashMap<String,Object> check(HttpServletRequest request, Device d,Boolean bool) throws ScriptException {
        HashMap<String,Object> map = new HashMap<>();
        //获取ip地址
        String ipAddress = ipUtil.getIpAddr(request);
        //ip所属地
        String address = "";
        if(ipAddress.equals("127.0.0.1")) {
            address = "保留地址";
        }
        else{
            if(ipUtil.isIntranetIp(ipAddress)){
                address = "本地局域网";
            }
            else{
                //获取ip所在地,次数有限
                try{
                    address = ipUtil.getIpAddress(ipAddress);
                }
                catch (NullPointerException e){
                    address = "查询次数已用完";
                }
            }
        }

        //获取请求设备
        String device = deviceUtil.getdevice(d);
        //距离上次请求过去的时间
        long pastTime = 0;

        //数据库没有该ip的数据
        if(loginRecordDao.selectByIp(ipAddress)==null){
            System.out.println("数据库中没有该ip");
            LoginRecord loginRecord = new LoginRecord();
            loginRecord.setIp(ipAddress);
            loginRecord.setAddress(address);
            loginRecord.setClient(device);
            loginRecord.setDatetime(new Date());
            loginRecord.setSliderfailtimes(0);
            loginRecord.setSliderrequesttimes(1);
            loginRecord.setPwfailtimes(0);
            loginRecordDao.insertSelective(loginRecord);
            map.put("bool",true);
            map.put("loginRecord",loginRecord);
        }
        else{//数据库有该ip数据
            LoginRecord loginRecord = loginRecordDao.selectByIp(ipAddress);
            //更新请求设备
            loginRecord.setClient(device);

            //IP被永久禁用
            if(loginRecord.getState().equals("永久禁用")){
                map.put("bool", false);
                map.put("msg", "该IP已被永久禁用");
            }
            else{
                //获取距离上次请求的时间
                pastTime = getTime.pasttime(loginRecord.getDatetime());
                //ip状态禁用
                if(loginRecord.getState().equals("禁用5分钟")){
                    //等待时间够
                    if(pastTime>=300){
                        loginRecord.setDatetime(new Date());
                        loginRecord.setPwfailtimes(0);
                        loginRecord.setSliderrequesttimes(1);
                        loginRecord.setSliderfailtimes(0);
                        loginRecord.setState("可用");
                        map.put("bool",true);
                        map.put("loginRecord",loginRecord);
                    }
                    //等待时间不够
                    else{
                        //计算剩余的时间
                        long time2 = 300-pastTime;
                        map.put("bool",false);
                        map.put("msg","操作频繁，请在"+(time2/60)+"分"+(time2%60)+"秒后再进行操作");
                    }
                }
                //ip状态可用
                else {
                    //距离上次请求在5分钟内
                    if(pastTime<=300){
                        //刷新滑块>10次或验证失败>10次或密码错误>5次，禁止刷新和验证
                        if (loginRecord.getSliderrequesttimes() >= 10 || loginRecord.getSliderfailtimes() >= 5 || loginRecord.getPwfailtimes() >= 5) {
                            loginRecord.setState("禁用5分钟");
                            loginRecord.setDatetime(new Date());
                            map.put("bool", false);
                            map.put("msg", "操作频繁，请在5分钟后再进行操作");
                        }
                        else {
                            if (bool) {//bool表示是否需要记录本次请求次数
                                loginRecord.setSliderrequesttimes(loginRecord.getSliderrequesttimes() + 1);
                            }
                            loginRecord.setDatetime(new Date());
                            map.put("bool", true);
                            map.put("loginRecord", loginRecord);
                        }
                    }
                    else{//距离上次请求超过5分钟
                        //初始化数据
                        loginRecord.setDatetime(new Date());
                        loginRecord.setSliderrequesttimes(1);
                        loginRecord.setPwfailtimes(0);
                        loginRecord.setSliderfailtimes(0);

                        map.put("bool", true);
                        map.put("loginRecord", loginRecord);
                    }
                }
                String pastTime2 = "";
                if(pastTime>86400){
                    long day = pastTime/86400;
                    long hour = pastTime%86400/3600;
                    long minute = pastTime%86400%3600/60;
                    long second = pastTime%86400%3600%60;
                    pastTime2 += day+"天"+hour+"小时"+minute+"分"+second+"秒";
                }
                else if(pastTime>3600){
                    pastTime2 += "0天"+pastTime/3600+"小时";
                    long a = pastTime%3600;
                    if(a>60){
                        pastTime2 += a/60+"分"+a%60+"秒";
                    }
                    else{
                        pastTime2 += "0分"+a+"秒";
                    }
                }
                else if(pastTime>60){
                    pastTime2 += "0天0小时"+pastTime/60+"分"+pastTime%60+"秒";
                }
                else {
                    pastTime2 = "0天0小时0分"+pastTime+"秒";
                }
                System.out.println("请求ip:"+ipAddress+","+address+"\n"+"请求设备:"+device+"\n距上次请求:"+pastTime2);
            }
            //更新请求记录
            loginRecordDao.updateByPrimaryKeySelective(loginRecord);
            loginRecordDao.updateStateByIp(loginRecord.getState(), loginRecord.getIp());
        }
        return map;
    }


    public void responseJson(HttpServletResponse response, JSONObject jsonObject){
        try {
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(jsonObject.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
