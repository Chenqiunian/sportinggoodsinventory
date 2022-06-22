package Demo.Controlller;

import Demo.Entity.*;
import Demo.Mapper.*;
import Demo.Service.MailService;
import Demo.Slider.CheckRequest;
import Demo.Tool.DeviceUtil;
import Demo.Tool.ExportExcel;
import Demo.Tool.GetTime;
import Demo.Tool.IpUtil;
import cloud.tianai.captcha.slider.CaptchaImageType;
import cloud.tianai.captcha.slider.SliderCaptchaApplication;
import cloud.tianai.captcha.template.slider.validator.SliderCaptchaTrack;
import cloud.tianai.captcha.vo.CaptchaResponse;
import cloud.tianai.captcha.vo.SliderCaptchaVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FilenameUtils;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@Controller
public class TestController {
    @Autowired
    private UserDao userDao;
    @Autowired
    private LoginRecordDao loginRecordDao;
    @Autowired
    private UserOperationDao operationDao;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private PortDao portDao;
    @Autowired
    private NoticeDao noticeDao;
    @Autowired
    private ChatCacheDao chatCacheDao;
    @Autowired(required = false)
    private MailService mailService;
    @Autowired
    private GetTime timeTool;
    @Autowired
    private IpUtil ipUtil;
    @Autowired
    private DeviceUtil deviceUtil;
    @Value("${spring.mail.nickname}")
    private String nickname;
    @Autowired
    private SliderCaptchaApplication application;
    @Autowired
    private CheckRequest checkRequest;

    private String projectBasePath = System.getProperty("user.dir");

    private String imageFolderPath = projectBasePath+"\\src\\main\\resources\\static\\images\\";


    @RequestMapping("/")
    public String index(HttpServletRequest request,HttpServletResponse response){
        System.out.println("test...");
        return "test";
    }

    @RequestMapping("/tiaozhuan")
    public String tiaozhuan(HttpServletResponse response, HttpServletRequest request, @RequestParam String a,Device device) throws IOException {
        System.out.println("tiaozhuanController...");

        System.out.println(timeTool.gettime()+"  ip地址："+ipUtil.getIpAddr(request)+"，设备："+deviceUtil.getdevice(device));

        switch (a){
            case "vue":
               return "vue";

            case "test":
                return "test";
            case "calendar":
                return "calendar";
            case "test2":
                return "test2";

            default: return "index";
        }
    }

    public HashMap<String,Object> getNewRequestMap(String state){
        HashMap<String,Object> map = new HashMap<>();
        map.put("lastTime",new Date());
        map.put("times",1);
        map.put("state",state);
        return map;
    }

    @RequestMapping("/testdb")
    void testdb(HttpServletRequest request,HttpServletResponse response){
        System.out.println("testdbController..");
        List<ChatCache> list = chatCacheDao.selectAll();

        String detail = "";

        for (ChatCache a:list){
            detail += a.toString()+"\n\n";
        }
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        result.put("msg",detail);
        checkRequest.responseJson(response,result);
    }

    @GetMapping("/refreshCaptcha")
    @ResponseBody
    public CaptchaResponse<SliderCaptchaVO> genCaptcha(HttpServletRequest request,HttpServletResponse response,Device d) throws Exception {
        System.out.println("refreshCaptcha...");

        HashMap<String,Object> map = checkRequest.check(request,d,true);

        //允许刷新滑块图片
        if((Boolean)map.get("bool")){
            CaptchaResponse<SliderCaptchaVO> res = application.generateSliderCaptcha(CaptchaImageType.WEBP);
            return res;
        }
        //操作频繁，不允许刷新滑块图片
        else{
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            jsonObject.put("msg",map.get("msg"));
            checkRequest.responseJson(response,jsonObject);
        }
        return null;
    }

    @PostMapping("/checkCaptcha")
    @ResponseBody
    public void checkCaptcha(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam("id") String id,
                             @RequestBody SliderCaptchaTrack sliderCaptchaTrack,
                             Device d) throws ScriptException {
        System.out.println("checkCaptcha...");
        HashMap<String,Object> map = checkRequest.check(request,d,false);
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        //允许验证
        if((Boolean)map.get("bool")){
            Boolean bool = application.matching(id,sliderCaptchaTrack);
            if(bool==false){//滑块验证失败，记录
                result.put("msg","false");
                LoginRecord loginRecord = (LoginRecord) map.get("loginRecord");
                if(loginRecord.getSliderfailtimes()+1>10){
                    loginRecord.setState("禁用5分钟");
                }
                loginRecord.setSliderfailtimes(loginRecord.getSliderfailtimes()+1);
                loginRecordDao.updateByPrimaryKeySelective(loginRecord);
            }
            else//滑块验证成功
                result.put("msg","true");
        }
        //操作频繁，不允许验证
        else
            result.put("msg",map.get("msg"));

        checkRequest.responseJson(response,result);
    }

    @RequestMapping("/checkUser")
    public void checkUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             HttpServletRequest request,Device d,HttpServletResponse response) throws ScriptException {
        System.out.println("checkUser...");
        HashMap<String,Object> map = checkRequest.check(request,d,false);
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        LoginRecord loginRecord = (LoginRecord)map.get("loginRecord");
        if((Boolean) map.get("bool")==false){
            result.put("msg",map.get("msg"));
            result.put("msgState","warning");
        }
        else{
            //验证用户
            Boolean isExist = userDao.checkExist(username);
            if(isExist){
                User user = userDao.userLogin(username,password);
                if(user==null){
                    result.put("msg", "用户名或密码错误");
                    result.put("msgState","error");
                    loginRecord.setPwfailtimes(loginRecord.getPwfailtimes()+1);
                    loginRecordDao.updateByPrimaryKeySelective(loginRecord);
                }
                else{
                    if(user.getUserstate().equals("禁用")){
                        result.put("msg", "该用户已被禁用");
                        result.put("msgState","error");
                    }
                    else{
                        //将user转为JSON字符串
                        user.setUserList(null);
                        String userJSONString = com.alibaba.fastjson.JSONObject.toJSONString(user);
                        result.put("msg","登录成功");
                        result.put("msgState","success");
                        result.put("user",userJSONString);
                        request.getSession().setAttribute("user",JSONObject.toJSONString(user));
                        System.out.println("sessionid:"+request.getSession().getId());
                        //添加用户登录记录
                        LoginRecord loginRecord1 = loginRecordDao.selectByUsername(user.getUsername());
                        loginRecord.setUsername(user.getUsername());
                        loginRecord.setSliderfailtimes(0);
                        loginRecord.setSliderrequesttimes(0);
                        loginRecord.setPwfailtimes(0);
                        if(loginRecord1 == null){
                            loginRecord.setId(loginRecordDao.getNewId());
                            loginRecordDao.insertSelective(loginRecord);
                        }
                        else{
                            loginRecord.setId(loginRecord1.getId());
                            loginRecordDao.updateByPrimaryKeySelective(loginRecord);
                        }
                    }
                }
            }
            else{
                result.put("msg","该用户不存在");
                result.put("msgState","error");
            }
        }
        try {
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(result.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
    @RequestMapping("/sendmailcode")
    public void SendMailYzm(HttpServletResponse response,HttpServletRequest request,@RequestParam(value="emailaddress") String emailAddress){
        System.out.println("sendmailcode...");
        //记录请求时间，请求次数，以便禁止频繁操作
        Map<String, Object> map;
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        String msg = "";
        String msgState = "";
        if(request.getSession().getAttribute("requestMap")!=null){
            map = (HashMap) request.getSession().getAttribute("requestMap");
            //获取从上一次请求到这次请求过去了多久
            long pastTime = timeTool.pasttime((Date)map.get("lastTime"));
            //判断state是enabled还是disabled
            if(pastTime >= 600){//距离上次请求过去了10分钟
                map = getNewRequestMap("enabled");
            }
            else{//距离上次请求没超过10分钟
                if(pastTime<60){//距离上次请求没超过1分钟
                    msg = "操作频繁，请"+(60-pastTime)+"秒后再发送";
                    msgState = "error";
                    map.put("state", "disabled");
                }
                else{//距离上次请求超过1分钟
                    if((int) map.get("times") < 5){//10分钟内请求没超过5次
                        map.put("times", (int) map.get("times") + 1);
                        map.put("lastTime", new Date());
                        map.put("state", "enabled");
                    }
                    else{//10分钟内请求超过5次
                        long waitingTime = 600 - pastTime;
                        msg = "操作频繁，请"+waitingTime/60+"分"+waitingTime%60+"秒后再发送";
                        msgState = "error";
                        map.put("state", "disabled");
                    }
                }
            }
        }
        else
            map = getNewRequestMap("enabled");

        //请求状态为enabled
        if(map.get("state").equals("enabled")){
            //判断该邮箱是否正确（数据库中是否有该邮箱）
            User user = userDao.selectByEmailAddress(emailAddress);
            if(user!=null){
                if(user.getUserstate().equals("禁用")){
                    msg = "该邮箱绑定的用户已被封禁";
                    msgState = "error";
                }
                else{//开始发送邮件
                    Boolean bool = sendMail(request,emailAddress, user, "重置密码");
                    if (bool) {
                        msg = "发送成功";
                        msgState = "success";
                    }
                    else{
                        msg = "发送失败";
                        msgState = "error";
                    }
                }
            }
            else{
                msg = "该邮箱未绑定任何用户";
                msgState = "error";
            }
        }
        request.getSession().setAttribute("requestMap",map);
        result.put("msg",msg);
        result.put("msgState",msgState);
        checkRequest.responseJson(response,result);
    }

    @RequestMapping("/verifyEmail")
    public void verifyEmail(HttpServletRequest request,HttpServletResponse response,@RequestParam String emailAddress){
        System.out.println("verifyEmail...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }

        JSONObject result = new JSONObject();
        if(request.getSession().getAttribute("user")!=null){
            //检查邮箱是否被别人绑定
            Boolean bool = userDao.selectByEmail(emailAddress);
            if(bool == true){
                result.put("msg", "该邮箱已被其他用户绑定");
                result.put("msgState", "error");
            }
            else{

                User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
                Boolean bool2 = sendMail(request,emailAddress,user,"绑定邮箱");
                if(bool2){
                    result.put("msg", "true");
                    result.put("msgState", "success");
                }
                else{
                    result.put("msg", "验证码发送失败！");
                    result.put("msgState", "error");
                }
            }
        }
        else{
            result.put("msg", "登录已失效，请重新登录！");
            result.put("msgState", "error");
        }

        checkRequest.responseJson(response, result);
    }

    public Boolean sendMail(HttpServletRequest request,String emailAddress,User user,String subject){
        //获取6为随机验证码
        String[] letters = new String[]{
                "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m",
                //"A","W","E","R","T","Y","U","I","O","P","A","S","D","F","G","H","J","K","L","Z","X","C","V","B","N","M",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String stringBuilder = "";
        for (int i = 0; i < 5; i++) {
            stringBuilder = stringBuilder + letters[(int) Math.floor(Math.random() * letters.length)];
        }
        Mail mail = new Mail();
        mail.setFrom(nickname + '<' + "2809205039@qq.com" + '>');
        mail.setTo(emailAddress);
        mail.setSubject(subject);
        //加图片的话，<img src="cid:rmb"></img>
        mail.setText("<!DOCTYPE html><html lang=\"en\"><body><table><tr><td colspan=\"3\">用户【"+user.getUsername()+"】你好！</td></tr><tr><td style=\"width:90px;\">验证码为：</td><td style=\"color: #007AFF;width: 58px\">"+stringBuilder+"</td><td>，有效时间为5分钟</td></tr><tr><td height=\"30px\" colspan=\"3\"></td></tr><tr><td colspan=\"3\">请勿将验证码告诉他人！<br>系统发信，请勿回复！</td></tr></table></body></html>");
        mailService.sendMail(mail);
        if(mail.getStatus().equals("ok")) {
            request.getSession().setAttribute("emailAddress", emailAddress);
            request.getSession().setAttribute("user", user);
            request.getSession().setAttribute("sendTime", new Date());
            request.getSession().setAttribute("code", stringBuilder);
            return true;
        }
        else
            return false;
    }

    @RequestMapping("/checkCode")
    public void checkCode(HttpServletRequest request,HttpServletResponse response,@RequestParam("code") String yzm){
        System.out.println("checkCode...");

        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        String msg = "";
        String code = (String)request.getSession().getAttribute("code");
        if(code==null){
            result.put("msg","未发送验证码或验证码无效");
            result.put("msgState","error");
        }
        else{
            code = (String)request.getSession().getAttribute("code");
            long pastTime = timeTool.pasttime((Date)request.getSession().getAttribute("sendTime"));
            if(pastTime<=300&&code.equals(yzm)){
                result.put("msg","true");
                result.put("msgState","success");
            }
            else if(pastTime>300){
                result.put("msg","验证码已过期，请重新发送");
                result.put("msgState","error");
            }
            else{
                result.put("msg","验证码错误");
                result.put("msgState","error");
            }
        }
        checkRequest.responseJson(response,result);
    }

    @RequestMapping("/changeEmail")
    public void changeEmail(HttpServletRequest request,HttpServletResponse response,@RequestParam String emailAddress){
        System.out.println("changeEmail...");
        JSONObject result = new JSONObject();
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(user == null){
            result.put("msg", "请重新登录");
            result.put("msgState", "error");
        }
        else{
            user.setEmail(emailAddress);
            int bool = userDao.updateByPrimaryKeySelective(user);
            if(bool>0){
                result.put("msg","邮箱绑定成功");
                result.put("msgState", "success");
            }
            else{
                result.put("msg", "邮箱绑定失败");
                result.put("msgState", "error");
            }
        }

        checkRequest.responseJson(response, result);
    }

    @PostMapping("/changePassword")
    public void changePassword(HttpServletRequest request,HttpServletResponse response,
                               @RequestParam String password){
        System.out.println("changePassword...");

        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(user==null){
            if(request.getSession().getAttribute("emailAddress")==null){
                result.put("msg","用户信息丢失，请重新验证！");
                result.put("msgState","error");
            }
            else{
                String emailAddress = (String)request.getSession().getAttribute("emailAddress");
                user = userDao.selectByEmailAddress(emailAddress);
                user.setPassword(password);
                result = updateUser(user,result);
            }
        }
        else {
            user.setPassword(password);
            result = updateUser(user,result);
        }

        checkRequest.responseJson(response,result);
    }

    @RequestMapping("/getUser")
    public void getUser(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getUser...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }

        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(user != null) {
            //获取user最新的数据
            user = userDao.selectByPrimaryKey(user.getId());
//            System.out.println(user.toString());
            user.setUserList(null);
            result.put("user",JSONObject.toJSONString(user));
        }
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/checkUsername")
    public void checkUsername(HttpServletRequest request,HttpServletResponse response,@RequestParam String username){
        System.out.println("checkUsername...");
        JSONObject result = new JSONObject();
        Boolean bool = userDao.checkExist(username);
        if(bool == false){
            result.put("msg","false");
        }
        else{
            result.put("msg", "true");
        }
        checkRequest.responseJson(response, result);
    }

    public com.alibaba.fastjson.JSONObject updateUser(User user, com.alibaba.fastjson.JSONObject result) {
        int i = userDao.updateByPrimaryKeySelective(user);
        if (i > 0) {
            result.put("msg", "true");
        } else {
            result.put("msg", "未知错误，密码修改失败！");
            result.put("msgState", "error");
        }
        return result;
    }
    @RequestMapping("/quit")
    public void quit(HttpServletRequest request,HttpServletResponse response){
        System.out.println("quit...");
        HttpSession session = request.getSession();
        session.removeAttribute("user");
        session.invalidate();
    }

    @PostMapping("/changeHeadImage")
    @ResponseBody
    public void changeHeadImage(HttpServletRequest request,HttpServletResponse response,@RequestParam MultipartFile imageFile,@RequestParam(required = false) String userId){
        System.out.println("changeHeadImage...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        JSONObject result = new JSONObject();
        //获取图片名字
        String fileName = imageFile.getOriginalFilename();
        //获取图片后缀
        String suffix = FilenameUtils.getExtension(fileName);
        //通过UUID生成唯一文件名
        fileName = UUID.randomUUID().toString().replaceAll("-","") + "." + suffix;

        User user;
        User loginUser = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(userId != null){
            user = userDao.selectByPrimaryKey(Integer.parseInt(userId));
        }
        else
            user = loginUser;
        int i = 0;
        if(user!=null){
            //将原来的图片删掉
            String imageUrl = user.getHeadimage();
            String oldImageFileName = imageUrl.replace("http://localhost:81/images/", "");
            try {
                //删除原来的文件
                new File(imageFolderPath+oldImageFileName).delete();
                //将文件保存指定目录
                imageFile.transferTo(new File(imageFolderPath,fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
            user.setHeadimage("http://localhost:81/images/"+fileName);
            i = userDao.updateByPrimaryKeySelective(user);
        }
        if(i>0){
            result.put("msg", "修改成功");
            result.put("msgState", "success");
            result.put("user",user);
            //更新session用户
            if(user == loginUser){//修改的用户和当前登录的用户是同一个人才更新session，不是同一个人session不用变
                request.getSession().setAttribute("user", user);
            }
        }
        else {
            result.put("msg", "修改失败未知错误");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/changeUser")
    public void changeUser(HttpServletRequest request,HttpServletResponse response,@RequestBody String data){
        System.out.println("changeUser...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        JSONObject result = new JSONObject();
        User loginUser = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(loginUser != null){
            User user = JSON.parseObject(data, User.class);
            int i = userDao.updateByPrimaryKeySelective(user);
            if(i>0){
                result.put("msg", "修改成功");
                result.put("msgState","success");
                if(loginUser.getId() == user.getId())
                    request.getSession().setAttribute("user",user);
            }
            else{
                result.put("msg", "修改失败");
                result.put("msgState","error");
            }
        }
        else{
            result.put("msg", "登录已失效，请重新登录");
            result.put("msgState","error");
        }
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getUsers")
    public void getUsers(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getUsers...");
        JSONObject result = new JSONObject();
        String jsonArray = getJsonString("user");
        result.put("userList", jsonArray);
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getIps")
    public void getIps(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getIps...");
        JSONObject result = new JSONObject();
        String jsonArray = getJsonString("ip");
        result.put("ipList", jsonArray);
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getOperations")
    public void getOperations(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getOperations...");
        JSONObject result = new JSONObject();
        String jsonArray = getJsonString("operation");
        result.put("operationList", jsonArray);
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getGoods")
    public void getGoods(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getGoods...");
        JSONObject result = new JSONObject();
        String jsonArray = getJsonString("goods");
        result.put("goodsList", jsonArray);
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getImportGoods")
    public void getImportGoods(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getImportGoods...");
        JSONObject result = new JSONObject();
        String jsonArray = getJsonString("import");
        result.put("importList", jsonArray);
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getExportGoods")
    public void getExportGoods(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getExportGoods...");
        JSONObject result = new JSONObject();
        String jsonArray = getJsonString("export");
        result.put("exportList", jsonArray);
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getPorts")
    public void getPorts(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getPorts...");
        JSONObject result = new JSONObject();
        String jsonArray = getJsonString("port");
        result.put("portsList", jsonArray);
        checkRequest.responseJson(response, result);
    }

    public String getJsonString(String type){
        String jsonString = "";
        switch (type){
            case "user":{
                List<User> list = userDao.selectall();
                for(User o : list)
                    o.setUserList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
            case "ip":{
                List<LoginRecord> list = loginRecordDao.selectAll();
                for(LoginRecord o : list)
                    o.setLoginRecordList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
            case "operation":{
                List<UserOperation> list = operationDao.selectAll();
                for(UserOperation o : list)
                    o.setOperationList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
            case "goods":{
                List<Goods> list = goodsDao.selectAll();
                for(Goods o : list)
                    o.setGoodsList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
            case "port":{
                List<Port> list = portDao.selectAll();
                for(Port o : list)
                    o.setPortList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
            case "import":{
                List<Port> list = portDao.selectImport();
                for(Port o : list)
                    o.setPortList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
            case "export":{
                List<Port> list = portDao.selectExport();
                for(Port o : list)
                    o.setPortList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
            case "notice":{
                List<Notice> list = noticeDao.selectAll();
                for(Notice o : list)
                    o.setNoticeList(null);
                jsonString = JSONArray.toJSONString(list);
                break;
            }
        }
        return jsonString;
    }

    @PostMapping("/addUser")
    @ResponseBody
    public void addUser(HttpServletRequest request,HttpServletResponse response,
                        @RequestParam(required = false) MultipartFile imageFile,
                        @RequestParam String name,@RequestParam String username,
                        @RequestParam String password,@RequestParam String gender,
                        @RequestParam String age,@RequestParam String email,
                        @RequestParam String userState,@RequestParam String usertype){
        System.out.println("addUser...");
        JSONObject result = new JSONObject();

        User user = new User();
        //上传头像
        if(imageFile != null){
            user.setHeadimage(uploadImage(imageFile, null));
        }
        System.out.println(name+"\n"+username+"\n"+password+"\n"+gender+"\n"+age+"\n"+email+"\n"+userState+"\n"+usertype+"\n");
        user.setId(userDao.getNewId());
        user.setPassword(password);
        user.setEmail(email);
        if(age.indexOf("null")!=-1)
            age = "0";
        user.setAge(Integer.parseInt(age));
        user.setGender(gender);
        user.setUsername(username);
        user.setName(name);
        user.setUsertype(usertype);
        user.setUserstate(userState);

        int i = userDao.insertSelective(user);

        if(i>0){
            result.put("msg", "添加成功");
            result.put("msgState","success");
        }
        else{
            result.put("msg", "添加失败");
            result.put("msgState","error");
        }
        checkRequest.responseJson(response, result);
    }

    public String uploadImage(MultipartFile imageFile,String oldImageFileUrl){
        //获取图片文件名
        String fileName = imageFile.getOriginalFilename();
        //获取图片后缀
        String suffix = FilenameUtils.getExtension(fileName);
        //通过UUID生成唯一文件名
        fileName = UUID.randomUUID().toString().replaceAll("-","") + "." + suffix;
        try {
            //替换原来的文件
            if(oldImageFileUrl != null){
                String oldImageFileName = oldImageFileUrl.replace("http://localhost:81/images/", "");
                new File(imageFolderPath+oldImageFileName).delete();
                fileName = oldImageFileName;
            }
            //将文件保存指定目录
            imageFile.transferTo(new File(imageFolderPath,fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "http://localhost:81/images/"+fileName;
    }

    @RequestMapping("/checkEmail")
    public void checkEmail(HttpServletRequest request,HttpServletResponse response,@RequestParam String emailAddress){
        System.out.println("checkEmail...");
        JSONObject result = new JSONObject();
        Boolean bool = userDao.selectByEmail(emailAddress);
        System.out.println(bool);
        if(bool == false){
            result.put("msg", "false");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "true");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);

    }

    @PostMapping("/batchSetPassword")
    @ResponseBody
    public void batchSetPassword(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids,@RequestParam String password){
        System.out.println("batchSetPassword...");
        JSONObject result = new JSONObject();
        //批量修改密码
        int i = userDao.updatePasswordById(ids,password);
        if(i>0){
            result.put("msg", "修改了"+i+"个用户的密码");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "修改失败");
            result.put("msgState", "error");
        }

        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteUsers")
    @ResponseBody
    public void deleteUsers(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteUsers...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //检查用户权限
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //删除用户
        int i = userDao.deleteByPrimaryKey(ids);
        if(i>0){
            result.put("msg", "成功删除"+i+"个用户");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "删除失败");
            result.put("msgState", "error");
        }

        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteOperation")
    @ResponseBody
    public void deleteOperation(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteOperation...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //检查用户权限
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //删除操作记录
        int i = operationDao.deleteOperationById(ids);
        if(i>0){
            result.put("msg", "成功删除"+i+"条记录");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "删除失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deletePortRecord")
    @ResponseBody
    public void deletePortRecord(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deletePortRecord...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //检查用户权限
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //删除进出货记录
        int i = portDao.deletePortRecord(ids);
        if(i>0){
            result.put("msg", "成功删除"+i+"条记录");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "删除失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deletePort")
    @ResponseBody
    public void deletePort(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deletePort...");
        JSONObject result = new JSONObject();
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //检查用户权限
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        //取消进货或取消出货
        int i = portDao.deletePortRecord(ids);
        if(i>0){
            result.put("msg", "取消成功");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "取消失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteIp")
    @ResponseBody
    public void deleteIp(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteIp...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //检查用户权限
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //删除ip记录
        int i = loginRecordDao.deleteIp(ids);
        if(i>0){
            result.put("msg", "成功删除"+i+"条IP记录");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "删除失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteGoods")
    @ResponseBody
    public void deleteGoods(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteGoods...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //检查用户权限
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //删除商品
        System.out.println(ids.length);
        int i = goodsDao.deleteGoods(ids);
        if(i>0){
            result.put("msg", "删除成功");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "删除失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    public int addOperation(int userid, String name, String operation,int number,String operationObject,String details){
        UserOperation userOperation = new UserOperation();
        userOperation.setId(operationDao.getNewId());
        userOperation.setOperation(operation);
        userOperation.setOperationobject(operationObject);
        userOperation.setUserid(userid);
        userOperation.setName(name);
        userOperation.setNumber(number);
        userOperation.setDate(new Date());
        userOperation.setDetails(details);
        int i = operationDao.insertSelective(userOperation);
        return i;
    }

    public String getNameDetail(int[] array,String prefix){
        String details = prefix;
        for(int j = 0;j<array.length;j++){
            User user = userDao.selectByPrimaryKey(array[j]);
            if(j == array.length-1){
                details += user.getUsername();
            }
            else{
                details += user.getName()+"、";
            }

        }
        return details;
    }

    @PostMapping("/updateIpState")
    @ResponseBody
    public void updateIpState(HttpServletRequest request,HttpServletResponse response,@RequestParam String ip,@RequestParam String state){
        System.out.println("updateIpState...");
        JSONObject result = new JSONObject();
        if(state.equals("可用")){
            loginRecordDao.updateTimesByIp(ip);
        }
        if(state.equals("禁用5分钟")){
            loginRecordDao.updateDateByIp(ip);
        }
        if(loginRecordDao.updateStateByIp(state,ip)>0){
            result.put("msg", "设置成功");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "设置失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/batchChangeIpState")
    @ResponseBody
    public void batchChangeIpState(HttpServletRequest request,HttpServletResponse response,@RequestParam String[]ips ,@RequestParam String state){
        System.out.println("batchChangeIpState...");
        JSONObject result = new JSONObject();
        int ii = 0;
        String operation = "";
        if(state.equals("可用")){
            operation = "启用";
        }
        else{
            operation = "禁用";
        }
        for(int i = 0;i<ips.length;i++){
            if(state.equals("可用")){
                loginRecordDao.updateTimesByIp(ips[i]);
            }
            if(state.equals("禁用5分钟")){
                loginRecordDao.updateDateByIp(ips[i]);
            }
            if(loginRecordDao.updateStateByIp(state, ips[i])>0){
                ii++;
            }
        }
        if(ii>0){
            result.put("msg",operation+"了"+ii+"个IP");
            result.put("msgState", "success");
        }
        else{
            result.put("msg",operation+"失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    public User getSessionUser(HttpServletRequest request){
        System.out.println("sessionid:"+request.getSession().getId());
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        return user;
    }

    @PostMapping("/addGoodsRecord")
    @ResponseBody
    public void addGoodsRecord(HttpServletRequest request,HttpServletResponse response,@RequestParam(required = false) MultipartFile imageFile,
                               @RequestParam String name,@RequestParam int number,@RequestParam double unitPrice,@RequestParam double totalPrice,
                               @RequestParam String trader,@RequestParam String details,@RequestParam(required = false) String goodsDetails,@RequestParam String operation,
                               @RequestParam(required = false) String image,@RequestParam(required = false) String id,@RequestParam String state){
        System.out.println("addGoodsRecord...");
        JSONObject result = new JSONObject();
        User user = getSessionUser(request);
        if(user == null){
            System.out.println("user null");
        }
        String msg = "";
        String op = "";
        if(details.equals("null")){
            details = "";
        }
        //图片
        String imagePath = "";
        //判断有没有该商品(判定标准：商品名相同，供应商相同)
        Goods goods = goodsDao.selectByNameAndSupplier(name,trader);
        if(imageFile!=null&&goods!=null){
            imagePath = uploadImage(imageFile, goods.getImage());
        }
        else if(imageFile!=null&&goods == null){
            imagePath = uploadImage(imageFile, null);
        }
        else if(goods!=null && imageFile == null){
            imagePath = goods.getImage();
        }
        else{
            imagePath = "../../static/goodsdefault.png";
        }

        //判断是进货还是出货
        if(operation.equals("进货")){
            if(state.equals("有效")){
                //没有的话添加商品
                if(goods == null){
                    System.out.println("没有该商品");
                    goods = new Goods();
                    goods.setId(goodsDao.getNewId());
                    goods.setName(name);
                    goods.setImage(imagePath);
                    goods.setInventory(number);
                    goods.setPrice(unitPrice);
                    goods.setSupplier(trader);
                    goods.setDetails(goodsDetails);
                    int addGoods = goodsDao.insertGoods(goods);
                    if(addGoods > 0){
                        System.out.println("商品添加成功");
                    }
                }
                else{//有的话更新库存
                    //缺货
                    if(goods.getLack()>0){
                        int lack = goods.getLack();
                        int newNumber = 0;
                        if(lack > number){//进货量小于缺货量
                            goods.setLack(lack-number);
                        }
                        else{//进货量大于缺货量
                            goods.setLack(0);
                            newNumber = number - lack;
                            //更新库存
                            goods.setInventory(goods.getInventory()+newNumber);
                        }
                    }
                    else{
                        //不缺货直接更新库存
                        goods.setInventory(goods.getInventory()+number);
                    }
                    goods.setImage(imagePath);//更新图片
                    goodsDao.updateGoods(goods);
                }
            }

            //添加进货记录
            Port port = new Port();
            port.setDate(new Date());
            port.setTrader(trader);
            port.setId(portDao.getNewId());
            int goodsId = 0;
            if(goods != null){
                goodsId = goods.getId();
            }
            port.setGoodsid(goodsId);
            port.setImage(imagePath);
            port.setName(name);
            port.setOperation(operation);
            port.setNumber(number);
            port.setUnitprice(unitPrice);
            port.setTotalprice(totalPrice);
            port.setDetails(details);
            port.setState(state);
            int addImRecord = portDao.insertRecord(port);
            if(addImRecord > 0){
                msg = "进货成功";
                op = "进货";
                if(state.equals("待进")){
                    msg = "已添加进货计划";
                    op = "添加进货计划";
                }
                result.put("msg",msg);
                result.put("msgState", "success");
                String operationDetails = "供货商:"+trader+",单价:"+unitPrice+"元,总价:"+totalPrice+"元";
                System.out.println(user.getId()+","+user.getName()+","+op+","+number+","+name+","+operationDetails);
                addOperation(user.getId(), user.getName(), op, number,name,operationDetails );
            }
            else{
                msg = "进货失败";
                if(state.equals("待进")){
                    msg = "进货计划添加失败";
                }

                result.put("msg",msg);
                result.put("msgState", "error");
            }
        }
        else{//出货
            Goods goods2 = goodsDao.selectByPrimaryKey(Integer.parseInt(id));
            if(goods2!=null){
                if(state.equals("有效")) {
                    //更新库存
                    goods2.setInventory(goods2.getInventory() - number);
                    goodsDao.updateGoods(goods2);
                }
                //添加出货记录
                Port port = new Port();
                port.setGoodsid(goods2.getId());
                port.setId(portDao.getNewId());
                port.setTrader(trader);
                port.setTotalprice(totalPrice);
                port.setUnitprice(unitPrice);
                port.setNumber(number);
                port.setName(name);
                port.setState(state);
                port.setDetails(details);
                port.setImage(image);
                port.setDate(new Date());
                port.setOperation(operation);
                int i = portDao.insertRecord(port);
                if(i > 0){
                    msg = "出货成功";
                    op = "出货";
                    if(state.equals("待出")){
                        msg = "已添加出货计划";
                        op = "添加出货计划";
                    }
                    result.put("msg",msg);
                    result.put("msgState", "success");
                    String operationDetails = "交易方:"+trader+",单价:"+unitPrice+"元,总价:"+totalPrice+"元";
                    System.out.println(user.getId()+","+user.getName()+","+op+","+number+","+name+","+operationDetails);
                    addOperation(user.getId(), user.getName(), op, number,name,operationDetails);
                }
                else{
                    msg = "出货失败";
                    if(state.equals("待出")){
                        msg = "出货计划添加失败";
                    }
                    result.put("msg",msg);
                    result.put("msgState", "error");
                }
            }
        }
        checkRequest.responseJson(response, result);

    }

    @PostMapping("/changeGoods")
    @ResponseBody
    public void changeGoods(HttpServletRequest request,HttpServletResponse response,@RequestParam(required = false) MultipartFile imageFile,
                            @RequestParam int id,@RequestParam String name,@RequestParam double price,@RequestParam int inventory,
                            @RequestParam int lack,@RequestParam String supplier,@RequestParam String details){
        System.out.println("changeGoods...");
        JSONObject result = new JSONObject();
        Goods goods = goodsDao.selectByPrimaryKey(id);
        if(goods != null){
            //图片
            if(imageFile != null){
                String newImage = uploadImage(imageFile, goods.getImage());
                goods.setImage(newImage);
            }
            goods.setName(name);
            goods.setPrice(price);
            goods.setInventory(inventory);
            goods.setLack(lack);
            goods.setSupplier(supplier);
            goods.setDetails(details);
            int i = goodsDao.updateGoods(goods);
            if(i>0){
                result.put("msg", "修改成功");
                result.put("msgState", "success");
            }
            else{
                result.put("msg", "修改失败");
                result.put("msgState", "error");
            }
        }
        else{
            result.put("msg", "该商品不存在");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/setGoodsState")
    @ResponseBody
    public void setGoodsState(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids,@RequestParam String state){
        System.out.println("setGoodsState...");
        JSONObject result = new JSONObject();
        int ii = goodsDao.setGoodsDelete(state,ids);
        if(ii>0){
            if(state.equals("删除")){
                result.put("msg", "已删除商品,可在已删除处恢复");
            }
            else {
                result.put("msg", "已恢复商品");
            }
            result.put("msgState","success");
        }
        else{
            if(state.equals("删除")){
                result.put("msg", "删除失败");
            }
            else {
                result.put("msg", "恢复失败");
            }
            result.put("msgState","error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/setPortState")
    @ResponseBody
    public void setPortState(HttpServletRequest request,HttpServletResponse response,
                             @RequestParam int[] ids,@RequestParam(required = false) int[] goodsIds,@RequestParam String operation){
        System.out.println("setPortState...");
        JSONObject result = new JSONObject();
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        Goods goods = null;
        Port port;
        int ii = 0;
        int j = 0;
        int kk = 0;
        String lackName = "";
        String portIds = portDao.selectByPrimaryKey(ids[0]).getOperation()+"记录ID:";
        for(int i = 0;i<ids.length;i++){
            port = portDao.selectByPrimaryKey(ids[i]);
            if(goodsIds!=null){
                goods = goodsDao.selectByPrimaryKey(goodsIds[i]);
            }
            if(operation.equals("进货")){
                if(goods == null){//没有的话就添加商品
                    goods = new Goods();
                    goods.setDetails("");
                    goods.setSupplier(port.getTrader());
                    goods.setInventory(port.getNumber());
                    goods.setName(port.getName());
                    goods.setImage(port.getImage());
                    goods.setPrice(port.getUnitprice());
                    goods.setId(goodsDao.getNewId());
                    j = goodsDao.insertGoods(goods);
                }
                else{//有的话就更新库存
                    //更新缺货信息
                    if(goods.getLack()>port.getNumber()){
                        goods.setLack(goods.getLack()-port.getNumber());
                    }
                    else{
                        goods.setLack(0);
                    }
                    goods.setInventory(goods.getInventory() + port.getNumber());
                    j = goodsDao.updateGoods(goods);
                }
                //更新进出货表
                port.setState("有效");
            }
            if(operation.equals("出货")){
                if(goods.getInventory()<port.getNumber()){//记录库存不足的商品
                    if(i!=ids.length-1) {
                        lackName+=port.getName()+",";
                    }
                    else{
                        lackName+=port.getName();
                    }
                }
                else{//库存充足直接出货
                    goods.setInventory(goods.getInventory()-port.getNumber());
                    j = goodsDao.updateGoods(goods);
                    if(j>0){
                        ii++;
                        //更新进出货表
                        port.setState("有效");
                    }
                }

            }
            if(operation.equals("有效")||operation.equals("无效")){
                Boolean isSetInvalid = false;
                if(port.getState().indexOf("待")!=-1 && operation.equals("无效")){
                    isSetInvalid = true;
                }
                //更新进出货表
                if(port.getState().indexOf("待")==-1 || isSetInvalid){//记录状态不是待进货和待出货的才可设置状态
                    port.setState(operation);
                    if(i!=ids.length-1){
                        portIds += port.getId()+"、";
                    }
                    else{
                        portIds += port.getId();
                    }
                }

            }

            if(port.getState().indexOf("待")==-1)
                j = portDao.updatePort(port);
            if(j>0)
                kk++;
        }
        if(operation.equals("有效")||operation.equals("无效")){
            if(kk>0){
                result.put("msg","操作成功");
                result.put("msgState", "success");
                addOperation(user.getId(),user.getName(),"设置货表"+operation,kk,"货表", portIds);
            }
            else{
                result.put("msg","操作失败");
                result.put("msgState", "error");
            }
        }
        else{
            if(!lackName.equals("")){
                lackName+="库存不足,出货失败";
                result.put("msg2",lackName);
                result.put("msgState2", "error");
            }
            if(kk>0){
                result.put("msg", operation+"成功");
                result.put("msgState", "success");
            }
            else{
                result.put("msg", operation+"失败");
                result.put("msgState", "error");
            }
        }
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/exportExcel")
    private void exportExcel(HttpServletRequest request,HttpServletResponse response,String beanName) throws IOException{

        System.out.println("exportExcel...,beanName="+beanName);
        //检查用户是否登录
//        if(!checkUserState(request, response, "isLogin")){
//            return;
//        }
//        //检查用户权限
//        if(!checkUserState(request, response, "isManager")){
//            return;
//        }

        request.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.setContentType("application/OCTET-STREAM;charset=UTF-8");
        String filename = "";

        if(beanName.equals("goods")){
            String[] headers = {"ID","图片","商品名","单价","库存","缺货","供货商","备注","状态"};  //表格的标题栏
            filename= new String(("商品表"+timeTool.Gettime()+".xls").getBytes(),"iso-8859-1");//中文文件名必须使用此句话
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            List<Goods> list = goodsDao.selectAll();
            //把goods的list信息去除
            for(int i = 0;i<list.size();i++){
                list.get(i).setGoodsList(null);
            }
            try {
                ExportExcel<Goods> ex = new ExportExcel<Goods>();  //构造导出类
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("商品表",headers, list, out);  //title是excel表中底部显示的表格名，如Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "导出成功!");
                // System.out.println("excel导出成功！");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(beanName.equals("export")) {
            filename = new String(("出货表" + timeTool.Gettime() + ".xls").getBytes(), "iso-8859-1");//中文文件名必须使用此句话
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            String[] headers = {"ID","商品ID","时间","图片","商品名","数量","单价","总价","操作","交易方","备注","状态"};  //表格的标题栏
            List<Port> list = portDao.selectExport();
            //把goods的list信息去除
            for(int i = 0;i<list.size();i++){
                list.get(i).setPortList(null);
            }
            try {
                ExportExcel<Port> ex = new ExportExcel<Port>();  //构造导出类
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("出货表",headers, list, out);  //title是excel表中底部显示的表格名，如Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "导出成功!");
                // System.out.println("excel导出成功！");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(beanName.equals("import")) {
            filename = new String(("进货表" + timeTool.Gettime() + ".xls").getBytes(), "iso-8859-1");//中文文件名必须使用此句话
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            String[] headers = {"ID","商品ID","时间","图片","商品名","数量","单价","总价","操作","供货商","备注","状态"};  //表格的标题栏
            List<Port> list = portDao.selectImport();
            //把goods的list信息去除
            for(int i = 0;i<list.size();i++){
                list.get(i).setPortList(null);
            }
            try {
                ExportExcel<Port> ex = new ExportExcel<Port>();  //构造导出类
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("进货表",headers, list, out);  //title是excel表中底部显示的表格名，如Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "导出成功!");
                // System.out.println("excel导出成功！");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(beanName.equals("operation")){
            filename= new String(("用户操作记录表"+timeTool.Gettime()+".xls").getBytes(),"iso-8859-1");//中文文件名必须使用此句话
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            String[] headers = {"ID","时间","操作者ID","操作者姓名","操作","数量","操作对象","详情"};  //表格的标题栏
            List<UserOperation> list = operationDao.selectAll();
            //把goods的list信息去除
            for(int i = 0;i<list.size();i++){
                list.get(i).setOperationList(null);
            }
            try {
                ExportExcel<UserOperation> ex = new ExportExcel<UserOperation>();  //构造导出类
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("操作记录表",headers, list, out);  //title是excel表中底部显示的表格名，如Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "导出成功!");
                // System.out.println("excel导出成功！");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @PostMapping("/recoverGoods")
    @ResponseBody
    private void recoverGoods(HttpServletRequest request,HttpServletResponse response,@RequestParam MultipartFile excelFile) throws FileNotFoundException, IOException{
        System.out.println("recoverGoods...");
        //检查用户是否登录
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //检查用户权限
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        List<Goods> list = new ArrayList<>();//记录excel里的商品
        int number = 0;//恢复的商品数量
        List<String> nameList = new ArrayList<>();//记录不恢复的商品名
        String names = "";//用于回复前端哪些商品没恢复
        if(!excelFile.isEmpty()){
            //获取服务器存放上传文件的文件夹路径
            String path = projectBasePath+"\\src\\main\\resources\\static\\excelcache";

            //获取上传文件的文件名(原文件名)
            String fileName = excelFile.getOriginalFilename();

            //获取文件名后缀
            String suffix = FilenameUtils.getExtension(fileName);

            File targetFile = new File(path,fileName);

            //创建文件
            targetFile.mkdirs();

            try {
                //向文件中写入数据
                excelFile.transferTo(targetFile);

            }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                //1、获取文件输入流
                InputStream inputStream = new FileInputStream(targetFile);
                //2、获取Excel工作簿对象
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                //3、得到Excel工作表对象
                HSSFSheet sheetAt = workbook.getSheetAt(0);
                //4、循环读取表格数据
                for (Row row : sheetAt) {
                    //首行（即表头）不读取
                    if (row.getRowNum() == 0) {
                        if(row.getCell(0).getStringCellValue().equals("ID")&&
                           row.getCell(1).getStringCellValue().equals("图片")&&
                           row.getCell(2).getStringCellValue().equals("商品名")&&
                           row.getCell(3).getStringCellValue().equals("单价")&&
                           row.getCell(4).getStringCellValue().equals("库存")&&
                           row.getCell(5).getStringCellValue().equals("缺货")&&
                           row.getCell(6).getStringCellValue().equals("供货商")&&
                           row.getCell(7).getStringCellValue().equals("备注")&&
                           row.getCell(8).getStringCellValue().equals("状态"))
                            continue;
                        else{
                            result.put("msg", "Excel文件格式不正确");
                            result.put("msgState", "error");
                            checkRequest.responseJson(response, result);
                            return;
                        }
                    }

                    //读取当前行中单元格数据，索引从0开始
                    String image = row.getCell(1).getStringCellValue();
                    String name = row.getCell(2).getStringCellValue();
                    Double price = row.getCell(3).getNumericCellValue();
                    int inventory = (int) row.getCell(4).getNumericCellValue();
                    int lack = (int) row.getCell(5).getNumericCellValue();
                    String supplier = row.getCell(6).getStringCellValue();
                    String details = row.getCell(7).getStringCellValue();
                    String state = row.getCell(8).getStringCellValue();

                    Goods goods = new Goods();
                    goods.setImage(image);
                    goods.setName(name);
                    goods.setPrice(price);
                    goods.setInventory(inventory);
                    goods.setLack(lack);
                    goods.setSupplier(supplier);
                    goods.setDetails(details);
                    goods.setState(state);

                    list.add(goods);
                }
                //关闭流
                workbook.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            //开始恢复商品
            if(list.size()>0){
                for(int i = 0;i<list.size();i++){
                    Goods goods1 = goodsDao.selectByNameAndSupplier(list.get(i).getName(),list.get(i).getSupplier());
                    if(goods1==null){
                        //恢复图片文件，如果图片文件还存在的话（删除超过30天会被删掉）
                        String imageUrl = list.get(i).getImage();
                        String imageName = imageUrl.substring(imageUrl.lastIndexOf("/")+1,imageUrl.lastIndexOf("."));
                        String imageSuffix = imageUrl.substring(imageUrl.lastIndexOf("."));
                        File newFile = new File(imageFolderPath,imageName+imageSuffix);
                        File dir = new File(imageFolderPath);
                        String[] children = dir.list();
                        for(String nameString:children){
                            if(nameString.indexOf(imageName)!=-1){
                                //找到文件了，开始恢复（重命名）
                                File oldFile = new File(imageFolderPath,nameString);
                                if(oldFile.renameTo(newFile)){
                                    System.out.println("图片"+imageName+imageSuffix+"恢复成功");
                                }
                                break;
                            }
                        }
//                        System.out.println("没有商品"+list.get(i).getName());
                        list.get(i).setId(goodsDao.getNewId());
//                        System.out.println(list.get(i).toString());
                        int ii = goodsDao.insertGoods(list.get(i));
                        if(ii>0)
                            number++;
                    }
                    else{
                        nameList.add(list.get(i).getName());
                    }
                }
            }

            //整理恢复结果
            if(nameList.size()>0){
                names = "商品";
                for(int i = 0;i<nameList.size();i++){
                    if(i!=nameList.size()-1){
                        names += nameList.get(i)+"、";
                    }
                    else{
                        names += nameList.get(i)+"已存在,不进行恢复";
                    }
                }
            }

            if(number>0){
                result.put("msg", "恢复成功,"+names);
                result.put("msgState", "success");
            }
            else if(list.size()==0){
                result.put("msg", "恢复失败,请检查Excel表格内容");
                result.put("msgState", "error");
            }
            else{
                result.put("msg", "商品表中已存在Excel中的所有商品,无需恢复");
                result.put("msgState", "error");
            }
            targetFile.delete();
        }
        else{
            result.put("msg", "获取Excel文件失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    public void recoverGoodsImageName(String[] Names){

    }

    @RequestMapping("/checkUserState")
    public Boolean checkUserState(HttpServletRequest request,HttpServletResponse response,@RequestParam String checkType){//由于前端数据易被非法修改，所以采用后端验证
        JSONObject result = new JSONObject();
        Boolean isLogin,isManager;
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(user != null){
            isLogin = true;
            if(user.getUsertype().equals("管理员")){
                isManager = true;
            }
            else{
                isManager = false;
            }
        }
        else{
            isLogin = false;
            isManager = false;
        }
        if(checkType.equals("isLogin")){
            if(isLogin == false){
                result.put("msg", "请先登录");
                result.put("msgState", "error");
                checkRequest.responseJson(response, result);
            }
            return isLogin;
        }
        else{
            if(isManager == false){
                result.put("msg", "非法操作");
                result.put("msgState", "error");
                checkRequest.responseJson(response, result);
            }
            return isManager;
        }
    }

    @RequestMapping("/changeUserOfSelect")
    public void changeUserOfSelect(HttpServletRequest request,HttpServletResponse response,
                                   @RequestParam int userid,@RequestParam(required = false) String gender,
    @RequestParam(required = false) String type,@RequestParam(required = false) String state){
        System.out.println("changeUserOfSelect...");
        JSONObject result = new JSONObject();

        User user = userDao.selectByPrimaryKey(userid);
        if(user!=null){
            if(gender != null){
                user.setGender(gender);
            }
            else if(type != null){
                user.setUsertype(type);
            }
            else{
                user.setUserstate(state);
            }
        }

        if(userDao.updateByPrimaryKeySelective(user)>0){
            result.put("msg", "修改成功");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "修改失败");
            result.put("msgState", "error");
        }

        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getNotice")
    public void getNotice(HttpServletRequest request,HttpServletResponse response){
        System.out.println("getNotice...");
        JSONObject result = new JSONObject();
        result.put("noticeList", getJsonString("notice"));
        checkRequest.responseJson(response, result);
    }
    @RequestMapping("/addNotice")
    public void addNotice(HttpServletRequest request,HttpServletResponse response,@RequestBody Notice notice){
        System.out.println("addNotice...");
        JSONObject result = new JSONObject();
        User user = getSessionUser(request);
        notice.setNoticeid(noticeDao.getNewId());
        notice.setNoticetime(new Date());
        notice.setUserid(user.getId());
        notice.setUsername(user.getName());
        if(noticeDao.insertNotice(notice)>0){
            result.put("msg", "添加成功");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "添加失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }
    @RequestMapping("/updateNotice")
    public void updateNotice(HttpServletRequest request,HttpServletResponse response,@RequestBody Notice notice){
        System.out.println("updateNotice...");
        JSONObject result = new JSONObject();
        if(noticeDao.updateNotice(notice)>0){
            result.put("msg", "修改成功");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "修改失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }
    @RequestMapping("/deleteNotice")
    public void deleteNotice(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteNotice...");
        JSONObject result = new JSONObject();
        int i = noticeDao.deleteNotice(ids);
        if(i>0){
            result.put("msg", "删除了"+i+"条公告");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "删除失败");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/getOfflineMessage")
    public void getOfflineMessage(HttpServletRequest request,HttpServletResponse response,@RequestParam String acceptUserId){
        System.out.println("getOfflineMessage...");
        JSONObject result = new JSONObject();
        List<ChatCache> list = chatCacheDao.selectByAcceptUserId(acceptUserId);
        for(ChatCache o : list)
            o.setChatCacheList(null);
        if(list.size()>0)
            chatCacheDao.deleteByAcceptUserId(acceptUserId);
        result.put("messageList",JSONArray.toJSONString(list));
        checkRequest.responseJson(response, result);
    }

}
