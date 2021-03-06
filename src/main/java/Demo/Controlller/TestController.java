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

        System.out.println(timeTool.gettime()+"  ip?????????"+ipUtil.getIpAddr(request)+"????????????"+deviceUtil.getdevice(device));

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

        //????????????????????????
        if((Boolean)map.get("bool")){
            CaptchaResponse<SliderCaptchaVO> res = application.generateSliderCaptcha(CaptchaImageType.WEBP);
            return res;
        }
        //??????????????????????????????????????????
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
        //????????????
        if((Boolean)map.get("bool")){
            Boolean bool = application.matching(id,sliderCaptchaTrack);
            if(bool==false){//???????????????????????????
                result.put("msg","false");
                LoginRecord loginRecord = (LoginRecord) map.get("loginRecord");
                if(loginRecord.getSliderfailtimes()+1>10){
                    loginRecord.setState("??????5??????");
                }
                loginRecord.setSliderfailtimes(loginRecord.getSliderfailtimes()+1);
                loginRecordDao.updateByPrimaryKeySelective(loginRecord);
            }
            else//??????????????????
                result.put("msg","true");
        }
        //??????????????????????????????
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
            //????????????
            Boolean isExist = userDao.checkExist(username);
            if(isExist){
                User user = userDao.userLogin(username,password);
                if(user==null){
                    result.put("msg", "????????????????????????");
                    result.put("msgState","error");
                    loginRecord.setPwfailtimes(loginRecord.getPwfailtimes()+1);
                    loginRecordDao.updateByPrimaryKeySelective(loginRecord);
                }
                else{
                    if(user.getUserstate().equals("??????")){
                        result.put("msg", "?????????????????????");
                        result.put("msgState","error");
                    }
                    else{
                        //???user??????JSON?????????
                        user.setUserList(null);
                        String userJSONString = com.alibaba.fastjson.JSONObject.toJSONString(user);
                        result.put("msg","????????????");
                        result.put("msgState","success");
                        result.put("user",userJSONString);
                        request.getSession().setAttribute("user",JSONObject.toJSONString(user));
                        System.out.println("sessionid:"+request.getSession().getId());
                        //????????????????????????
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
                result.put("msg","??????????????????");
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
        //????????????????????????????????????????????????????????????
        Map<String, Object> map;
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        String msg = "";
        String msgState = "";
        if(request.getSession().getAttribute("requestMap")!=null){
            map = (HashMap) request.getSession().getAttribute("requestMap");
            //??????????????????????????????????????????????????????
            long pastTime = timeTool.pasttime((Date)map.get("lastTime"));
            //??????state???enabled??????disabled
            if(pastTime >= 600){//???????????????????????????10??????
                map = getNewRequestMap("enabled");
            }
            else{//???????????????????????????10??????
                if(pastTime<60){//???????????????????????????1??????
                    msg = "??????????????????"+(60-pastTime)+"???????????????";
                    msgState = "error";
                    map.put("state", "disabled");
                }
                else{//????????????????????????1??????
                    if((int) map.get("times") < 5){//10????????????????????????5???
                        map.put("times", (int) map.get("times") + 1);
                        map.put("lastTime", new Date());
                        map.put("state", "enabled");
                    }
                    else{//10?????????????????????5???
                        long waitingTime = 600 - pastTime;
                        msg = "??????????????????"+waitingTime/60+"???"+waitingTime%60+"???????????????";
                        msgState = "error";
                        map.put("state", "disabled");
                    }
                }
            }
        }
        else
            map = getNewRequestMap("enabled");

        //???????????????enabled
        if(map.get("state").equals("enabled")){
            //???????????????????????????????????????????????????????????????
            User user = userDao.selectByEmailAddress(emailAddress);
            if(user!=null){
                if(user.getUserstate().equals("??????")){
                    msg = "????????????????????????????????????";
                    msgState = "error";
                }
                else{//??????????????????
                    Boolean bool = sendMail(request,emailAddress, user, "????????????");
                    if (bool) {
                        msg = "????????????";
                        msgState = "success";
                    }
                    else{
                        msg = "????????????";
                        msgState = "error";
                    }
                }
            }
            else{
                msg = "??????????????????????????????";
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
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }

        JSONObject result = new JSONObject();
        if(request.getSession().getAttribute("user")!=null){
            //?????????????????????????????????
            Boolean bool = userDao.selectByEmail(emailAddress);
            if(bool == true){
                result.put("msg", "?????????????????????????????????");
                result.put("msgState", "error");
            }
            else{

                User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
                Boolean bool2 = sendMail(request,emailAddress,user,"????????????");
                if(bool2){
                    result.put("msg", "true");
                    result.put("msgState", "success");
                }
                else{
                    result.put("msg", "????????????????????????");
                    result.put("msgState", "error");
                }
            }
        }
        else{
            result.put("msg", "????????????????????????????????????");
            result.put("msgState", "error");
        }

        checkRequest.responseJson(response, result);
    }

    public Boolean sendMail(HttpServletRequest request,String emailAddress,User user,String subject){
        //??????6??????????????????
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
        //??????????????????<img src="cid:rmb"></img>
        mail.setText("<!DOCTYPE html><html lang=\"en\"><body><table><tr><td colspan=\"3\">?????????"+user.getUsername()+"????????????</td></tr><tr><td style=\"width:90px;\">???????????????</td><td style=\"color: #007AFF;width: 58px\">"+stringBuilder+"</td><td>??????????????????5??????</td></tr><tr><td height=\"30px\" colspan=\"3\"></td></tr><tr><td colspan=\"3\">?????????????????????????????????<br>??????????????????????????????</td></tr></table></body></html>");
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
            result.put("msg","????????????????????????????????????");
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
                result.put("msg","????????????????????????????????????");
                result.put("msgState","error");
            }
            else{
                result.put("msg","???????????????");
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
            result.put("msg", "???????????????");
            result.put("msgState", "error");
        }
        else{
            user.setEmail(emailAddress);
            int bool = userDao.updateByPrimaryKeySelective(user);
            if(bool>0){
                result.put("msg","??????????????????");
                result.put("msgState", "success");
            }
            else{
                result.put("msg", "??????????????????");
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
                result.put("msg","???????????????????????????????????????");
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
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }

        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(user != null) {
            //??????user???????????????
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
            result.put("msg", "????????????????????????????????????");
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
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        JSONObject result = new JSONObject();
        //??????????????????
        String fileName = imageFile.getOriginalFilename();
        //??????????????????
        String suffix = FilenameUtils.getExtension(fileName);
        //??????UUID?????????????????????
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
            //????????????????????????
            String imageUrl = user.getHeadimage();
            String oldImageFileName = imageUrl.replace("http://localhost:81/images/", "");
            try {
                //?????????????????????
                new File(imageFolderPath+oldImageFileName).delete();
                //???????????????????????????
                imageFile.transferTo(new File(imageFolderPath,fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
            user.setHeadimage("http://localhost:81/images/"+fileName);
            i = userDao.updateByPrimaryKeySelective(user);
        }
        if(i>0){
            result.put("msg", "????????????");
            result.put("msgState", "success");
            result.put("user",user);
            //??????session??????
            if(user == loginUser){//???????????????????????????????????????????????????????????????session?????????????????????session?????????
                request.getSession().setAttribute("user", user);
            }
        }
        else {
            result.put("msg", "????????????????????????");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/changeUser")
    public void changeUser(HttpServletRequest request,HttpServletResponse response,@RequestBody String data){
        System.out.println("changeUser...");
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        JSONObject result = new JSONObject();
        User loginUser = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(loginUser != null){
            User user = JSON.parseObject(data, User.class);
            int i = userDao.updateByPrimaryKeySelective(user);
            if(i>0){
                result.put("msg", "????????????");
                result.put("msgState","success");
                if(loginUser.getId() == user.getId())
                    request.getSession().setAttribute("user",user);
            }
            else{
                result.put("msg", "????????????");
                result.put("msgState","error");
            }
        }
        else{
            result.put("msg", "?????????????????????????????????");
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
        //????????????
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
            result.put("msg", "????????????");
            result.put("msgState","success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState","error");
        }
        checkRequest.responseJson(response, result);
    }

    public String uploadImage(MultipartFile imageFile,String oldImageFileUrl){
        //?????????????????????
        String fileName = imageFile.getOriginalFilename();
        //??????????????????
        String suffix = FilenameUtils.getExtension(fileName);
        //??????UUID?????????????????????
        fileName = UUID.randomUUID().toString().replaceAll("-","") + "." + suffix;
        try {
            //?????????????????????
            if(oldImageFileUrl != null){
                String oldImageFileName = oldImageFileUrl.replace("http://localhost:81/images/", "");
                new File(imageFolderPath+oldImageFileName).delete();
                fileName = oldImageFileName;
            }
            //???????????????????????????
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
        //??????????????????
        int i = userDao.updatePasswordById(ids,password);
        if(i>0){
            result.put("msg", "?????????"+i+"??????????????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState", "error");
        }

        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteUsers")
    @ResponseBody
    public void deleteUsers(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteUsers...");
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //??????????????????
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //????????????
        int i = userDao.deleteByPrimaryKey(ids);
        if(i>0){
            result.put("msg", "????????????"+i+"?????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState", "error");
        }

        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteOperation")
    @ResponseBody
    public void deleteOperation(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteOperation...");
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //??????????????????
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //??????????????????
        int i = operationDao.deleteOperationById(ids);
        if(i>0){
            result.put("msg", "????????????"+i+"?????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deletePortRecord")
    @ResponseBody
    public void deletePortRecord(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deletePortRecord...");
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //??????????????????
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //?????????????????????
        int i = portDao.deletePortRecord(ids);
        if(i>0){
            result.put("msg", "????????????"+i+"?????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deletePort")
    @ResponseBody
    public void deletePort(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deletePort...");
        JSONObject result = new JSONObject();
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //??????????????????
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        //???????????????????????????
        int i = portDao.deletePortRecord(ids);
        if(i>0){
            result.put("msg", "????????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteIp")
    @ResponseBody
    public void deleteIp(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteIp...");
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //??????????????????
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //??????ip??????
        int i = loginRecordDao.deleteIp(ids);
        if(i>0){
            result.put("msg", "????????????"+i+"???IP??????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    @PostMapping("/deleteGoods")
    @ResponseBody
    public void deleteGoods(HttpServletRequest request,HttpServletResponse response,@RequestParam int[] ids){
        System.out.println("deleteGoods...");
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //??????????????????
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        //????????????
        System.out.println(ids.length);
        int i = goodsDao.deleteGoods(ids);
        if(i>0){
            result.put("msg", "????????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
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
                details += user.getName()+"???";
            }

        }
        return details;
    }

    @PostMapping("/updateIpState")
    @ResponseBody
    public void updateIpState(HttpServletRequest request,HttpServletResponse response,@RequestParam String ip,@RequestParam String state){
        System.out.println("updateIpState...");
        JSONObject result = new JSONObject();
        if(state.equals("??????")){
            loginRecordDao.updateTimesByIp(ip);
        }
        if(state.equals("??????5??????")){
            loginRecordDao.updateDateByIp(ip);
        }
        if(loginRecordDao.updateStateByIp(state,ip)>0){
            result.put("msg", "????????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
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
        if(state.equals("??????")){
            operation = "??????";
        }
        else{
            operation = "??????";
        }
        for(int i = 0;i<ips.length;i++){
            if(state.equals("??????")){
                loginRecordDao.updateTimesByIp(ips[i]);
            }
            if(state.equals("??????5??????")){
                loginRecordDao.updateDateByIp(ips[i]);
            }
            if(loginRecordDao.updateStateByIp(state, ips[i])>0){
                ii++;
            }
        }
        if(ii>0){
            result.put("msg",operation+"???"+ii+"???IP");
            result.put("msgState", "success");
        }
        else{
            result.put("msg",operation+"??????");
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
        //??????
        String imagePath = "";
        //????????????????????????(????????????????????????????????????????????????)
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

        //???????????????????????????
        if(operation.equals("??????")){
            if(state.equals("??????")){
                //????????????????????????
                if(goods == null){
                    System.out.println("???????????????");
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
                        System.out.println("??????????????????");
                    }
                }
                else{//?????????????????????
                    //??????
                    if(goods.getLack()>0){
                        int lack = goods.getLack();
                        int newNumber = 0;
                        if(lack > number){//????????????????????????
                            goods.setLack(lack-number);
                        }
                        else{//????????????????????????
                            goods.setLack(0);
                            newNumber = number - lack;
                            //????????????
                            goods.setInventory(goods.getInventory()+newNumber);
                        }
                    }
                    else{
                        //???????????????????????????
                        goods.setInventory(goods.getInventory()+number);
                    }
                    goods.setImage(imagePath);//????????????
                    goodsDao.updateGoods(goods);
                }
            }

            //??????????????????
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
                msg = "????????????";
                op = "??????";
                if(state.equals("??????")){
                    msg = "?????????????????????";
                    op = "??????????????????";
                }
                result.put("msg",msg);
                result.put("msgState", "success");
                String operationDetails = "?????????:"+trader+",??????:"+unitPrice+"???,??????:"+totalPrice+"???";
                System.out.println(user.getId()+","+user.getName()+","+op+","+number+","+name+","+operationDetails);
                addOperation(user.getId(), user.getName(), op, number,name,operationDetails );
            }
            else{
                msg = "????????????";
                if(state.equals("??????")){
                    msg = "????????????????????????";
                }

                result.put("msg",msg);
                result.put("msgState", "error");
            }
        }
        else{//??????
            Goods goods2 = goodsDao.selectByPrimaryKey(Integer.parseInt(id));
            if(goods2!=null){
                if(state.equals("??????")) {
                    //????????????
                    goods2.setInventory(goods2.getInventory() - number);
                    goodsDao.updateGoods(goods2);
                }
                //??????????????????
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
                    msg = "????????????";
                    op = "??????";
                    if(state.equals("??????")){
                        msg = "?????????????????????";
                        op = "??????????????????";
                    }
                    result.put("msg",msg);
                    result.put("msgState", "success");
                    String operationDetails = "?????????:"+trader+",??????:"+unitPrice+"???,??????:"+totalPrice+"???";
                    System.out.println(user.getId()+","+user.getName()+","+op+","+number+","+name+","+operationDetails);
                    addOperation(user.getId(), user.getName(), op, number,name,operationDetails);
                }
                else{
                    msg = "????????????";
                    if(state.equals("??????")){
                        msg = "????????????????????????";
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
            //??????
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
                result.put("msg", "????????????");
                result.put("msgState", "success");
            }
            else{
                result.put("msg", "????????????");
                result.put("msgState", "error");
            }
        }
        else{
            result.put("msg", "??????????????????");
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
            if(state.equals("??????")){
                result.put("msg", "???????????????,????????????????????????");
            }
            else {
                result.put("msg", "???????????????");
            }
            result.put("msgState","success");
        }
        else{
            if(state.equals("??????")){
                result.put("msg", "????????????");
            }
            else {
                result.put("msg", "????????????");
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
        String portIds = portDao.selectByPrimaryKey(ids[0]).getOperation()+"??????ID:";
        for(int i = 0;i<ids.length;i++){
            port = portDao.selectByPrimaryKey(ids[i]);
            if(goodsIds!=null){
                goods = goodsDao.selectByPrimaryKey(goodsIds[i]);
            }
            if(operation.equals("??????")){
                if(goods == null){//???????????????????????????
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
                else{//????????????????????????
                    //??????????????????
                    if(goods.getLack()>port.getNumber()){
                        goods.setLack(goods.getLack()-port.getNumber());
                    }
                    else{
                        goods.setLack(0);
                    }
                    goods.setInventory(goods.getInventory() + port.getNumber());
                    j = goodsDao.updateGoods(goods);
                }
                //??????????????????
                port.setState("??????");
            }
            if(operation.equals("??????")){
                if(goods.getInventory()<port.getNumber()){//???????????????????????????
                    if(i!=ids.length-1) {
                        lackName+=port.getName()+",";
                    }
                    else{
                        lackName+=port.getName();
                    }
                }
                else{//????????????????????????
                    goods.setInventory(goods.getInventory()-port.getNumber());
                    j = goodsDao.updateGoods(goods);
                    if(j>0){
                        ii++;
                        //??????????????????
                        port.setState("??????");
                    }
                }

            }
            if(operation.equals("??????")||operation.equals("??????")){
                Boolean isSetInvalid = false;
                if(port.getState().indexOf("???")!=-1 && operation.equals("??????")){
                    isSetInvalid = true;
                }
                //??????????????????
                if(port.getState().indexOf("???")==-1 || isSetInvalid){//????????????????????????????????????????????????????????????
                    port.setState(operation);
                    if(i!=ids.length-1){
                        portIds += port.getId()+"???";
                    }
                    else{
                        portIds += port.getId();
                    }
                }

            }

            if(port.getState().indexOf("???")==-1)
                j = portDao.updatePort(port);
            if(j>0)
                kk++;
        }
        if(operation.equals("??????")||operation.equals("??????")){
            if(kk>0){
                result.put("msg","????????????");
                result.put("msgState", "success");
                addOperation(user.getId(),user.getName(),"????????????"+operation,kk,"??????", portIds);
            }
            else{
                result.put("msg","????????????");
                result.put("msgState", "error");
            }
        }
        else{
            if(!lackName.equals("")){
                lackName+="????????????,????????????";
                result.put("msg2",lackName);
                result.put("msgState2", "error");
            }
            if(kk>0){
                result.put("msg", operation+"??????");
                result.put("msgState", "success");
            }
            else{
                result.put("msg", operation+"??????");
                result.put("msgState", "error");
            }
        }
        checkRequest.responseJson(response, result);
    }

    @RequestMapping("/exportExcel")
    private void exportExcel(HttpServletRequest request,HttpServletResponse response,String beanName) throws IOException{

        System.out.println("exportExcel...,beanName="+beanName);
        //????????????????????????
//        if(!checkUserState(request, response, "isLogin")){
//            return;
//        }
//        //??????????????????
//        if(!checkUserState(request, response, "isManager")){
//            return;
//        }

        request.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.setContentType("application/OCTET-STREAM;charset=UTF-8");
        String filename = "";

        if(beanName.equals("goods")){
            String[] headers = {"ID","??????","?????????","??????","??????","??????","?????????","??????","??????"};  //??????????????????
            filename= new String(("?????????"+timeTool.Gettime()+".xls").getBytes(),"iso-8859-1");//????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            List<Goods> list = goodsDao.selectAll();
            //???goods???list????????????
            for(int i = 0;i<list.size();i++){
                list.get(i).setGoodsList(null);
            }
            try {
                ExportExcel<Goods> ex = new ExportExcel<Goods>();  //???????????????
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("?????????",headers, list, out);  //title???excel????????????????????????????????????Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "????????????!");
                // System.out.println("excel???????????????");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(beanName.equals("export")) {
            filename = new String(("?????????" + timeTool.Gettime() + ".xls").getBytes(), "iso-8859-1");//????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            String[] headers = {"ID","??????ID","??????","??????","?????????","??????","??????","??????","??????","?????????","??????","??????"};  //??????????????????
            List<Port> list = portDao.selectExport();
            //???goods???list????????????
            for(int i = 0;i<list.size();i++){
                list.get(i).setPortList(null);
            }
            try {
                ExportExcel<Port> ex = new ExportExcel<Port>();  //???????????????
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("?????????",headers, list, out);  //title???excel????????????????????????????????????Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "????????????!");
                // System.out.println("excel???????????????");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(beanName.equals("import")) {
            filename = new String(("?????????" + timeTool.Gettime() + ".xls").getBytes(), "iso-8859-1");//????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            String[] headers = {"ID","??????ID","??????","??????","?????????","??????","??????","??????","??????","?????????","??????","??????"};  //??????????????????
            List<Port> list = portDao.selectImport();
            //???goods???list????????????
            for(int i = 0;i<list.size();i++){
                list.get(i).setPortList(null);
            }
            try {
                ExportExcel<Port> ex = new ExportExcel<Port>();  //???????????????
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("?????????",headers, list, out);  //title???excel????????????????????????????????????Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "????????????!");
                // System.out.println("excel???????????????");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(beanName.equals("operation")){
            filename= new String(("?????????????????????"+timeTool.Gettime()+".xls").getBytes(),"iso-8859-1");//????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename="+filename );
            String[] headers = {"ID","??????","?????????ID","???????????????","??????","??????","????????????","??????"};  //??????????????????
            List<UserOperation> list = operationDao.selectAll();
            //???goods???list????????????
            for(int i = 0;i<list.size();i++){
                list.get(i).setOperationList(null);
            }
            try {
                ExportExcel<UserOperation> ex = new ExportExcel<UserOperation>();  //???????????????
                OutputStream  out = new BufferedOutputStream(response.getOutputStream());
                ex.exportExcel("???????????????",headers, list, out);  //title???excel????????????????????????????????????Sheet
                out.close();
                //JOptionPane.showMessageDialog(null, "????????????!");
                // System.out.println("excel???????????????");
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
        //????????????????????????
        if(!checkUserState(request, response, "isLogin")){
            return;
        }
        //??????????????????
        if(!checkUserState(request, response, "isManager")){
            return;
        }
        JSONObject result = new JSONObject();
        List<Goods> list = new ArrayList<>();//??????excel????????????
        int number = 0;//?????????????????????
        List<String> nameList = new ArrayList<>();//???????????????????????????
        String names = "";//???????????????????????????????????????
        if(!excelFile.isEmpty()){
            //???????????????????????????????????????????????????
            String path = projectBasePath+"\\src\\main\\resources\\static\\excelcache";

            //??????????????????????????????(????????????)
            String fileName = excelFile.getOriginalFilename();

            //?????????????????????
            String suffix = FilenameUtils.getExtension(fileName);

            File targetFile = new File(path,fileName);

            //????????????
            targetFile.mkdirs();

            try {
                //????????????????????????
                excelFile.transferTo(targetFile);

            }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                //1????????????????????????
                InputStream inputStream = new FileInputStream(targetFile);
                //2?????????Excel???????????????
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                //3?????????Excel???????????????
                HSSFSheet sheetAt = workbook.getSheetAt(0);
                //4???????????????????????????
                for (Row row : sheetAt) {
                    //??????????????????????????????
                    if (row.getRowNum() == 0) {
                        if(row.getCell(0).getStringCellValue().equals("ID")&&
                           row.getCell(1).getStringCellValue().equals("??????")&&
                           row.getCell(2).getStringCellValue().equals("?????????")&&
                           row.getCell(3).getStringCellValue().equals("??????")&&
                           row.getCell(4).getStringCellValue().equals("??????")&&
                           row.getCell(5).getStringCellValue().equals("??????")&&
                           row.getCell(6).getStringCellValue().equals("?????????")&&
                           row.getCell(7).getStringCellValue().equals("??????")&&
                           row.getCell(8).getStringCellValue().equals("??????"))
                            continue;
                        else{
                            result.put("msg", "Excel?????????????????????");
                            result.put("msgState", "error");
                            checkRequest.responseJson(response, result);
                            return;
                        }
                    }

                    //?????????????????????????????????????????????0??????
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
                //?????????
                workbook.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            //??????????????????
            if(list.size()>0){
                for(int i = 0;i<list.size();i++){
                    Goods goods1 = goodsDao.selectByNameAndSupplier(list.get(i).getName(),list.get(i).getSupplier());
                    if(goods1==null){
                        //?????????????????????????????????????????????????????????????????????30??????????????????
                        String imageUrl = list.get(i).getImage();
                        String imageName = imageUrl.substring(imageUrl.lastIndexOf("/")+1,imageUrl.lastIndexOf("."));
                        String imageSuffix = imageUrl.substring(imageUrl.lastIndexOf("."));
                        File newFile = new File(imageFolderPath,imageName+imageSuffix);
                        File dir = new File(imageFolderPath);
                        String[] children = dir.list();
                        for(String nameString:children){
                            if(nameString.indexOf(imageName)!=-1){
                                //?????????????????????????????????????????????
                                File oldFile = new File(imageFolderPath,nameString);
                                if(oldFile.renameTo(newFile)){
                                    System.out.println("??????"+imageName+imageSuffix+"????????????");
                                }
                                break;
                            }
                        }
//                        System.out.println("????????????"+list.get(i).getName());
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

            //??????????????????
            if(nameList.size()>0){
                names = "??????";
                for(int i = 0;i<nameList.size();i++){
                    if(i!=nameList.size()-1){
                        names += nameList.get(i)+"???";
                    }
                    else{
                        names += nameList.get(i)+"?????????,???????????????";
                    }
                }
            }

            if(number>0){
                result.put("msg", "????????????,"+names);
                result.put("msgState", "success");
            }
            else if(list.size()==0){
                result.put("msg", "????????????,?????????Excel????????????");
                result.put("msgState", "error");
            }
            else{
                result.put("msg", "?????????????????????Excel??????????????????,????????????");
                result.put("msgState", "error");
            }
            targetFile.delete();
        }
        else{
            result.put("msg", "??????Excel????????????");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }

    public void recoverGoodsImageName(String[] Names){

    }

    @RequestMapping("/checkUserState")
    public Boolean checkUserState(HttpServletRequest request,HttpServletResponse response,@RequestParam String checkType){//???????????????????????????????????????????????????????????????
        JSONObject result = new JSONObject();
        Boolean isLogin,isManager;
        User user = JSON.parseObject((String)request.getSession().getAttribute("user"),User.class);
        if(user != null){
            isLogin = true;
            if(user.getUsertype().equals("?????????")){
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
                result.put("msg", "????????????");
                result.put("msgState", "error");
                checkRequest.responseJson(response, result);
            }
            return isLogin;
        }
        else{
            if(isManager == false){
                result.put("msg", "????????????");
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
            result.put("msg", "????????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
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
            result.put("msg", "????????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
            result.put("msgState", "error");
        }
        checkRequest.responseJson(response, result);
    }
    @RequestMapping("/updateNotice")
    public void updateNotice(HttpServletRequest request,HttpServletResponse response,@RequestBody Notice notice){
        System.out.println("updateNotice...");
        JSONObject result = new JSONObject();
        if(noticeDao.updateNotice(notice)>0){
            result.put("msg", "????????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
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
            result.put("msg", "?????????"+i+"?????????");
            result.put("msgState", "success");
        }
        else{
            result.put("msg", "????????????");
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
