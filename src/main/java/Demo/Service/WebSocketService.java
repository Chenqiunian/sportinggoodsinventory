package Demo.Service;

import Demo.Entity.ChatCache;
import Demo.Entity.SocketMsg;
import Demo.Entity.User;
import Demo.Mapper.ChatCacheDao;
import Demo.Mapper.UserDao;
import Demo.Tool.GetTime;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.awt.print.PrinterAbortException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Author Chenqiunian
 * Date 2022-04-16 20:46
 * Description
 */
@Component
@ServerEndpoint(value = "/websocket/{userid}/{username}")
public class WebSocketService {
    @Autowired
    private ChatCacheDao chatCacheDao;
    @Autowired
    private GetTime getTime;
    private static WebSocketService webSocketService;
    private String nickname;
    private Session session;
    //用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketService> webSocketSet = new CopyOnWriteArraySet<WebSocketService>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    //用来记录sessionId和该session进行绑定
    private static Map<String, Session> map = new HashMap<String, Session>();
    //用来记录用户的姓名
    private static Map<String,String> nameMap = new HashMap<String,String>();
    //发送给前端的数据
    Map<String,Object> m=new HashMap<String, Object>();
    @PostConstruct
    public void init(){
        webSocketService = this;
        webSocketService.getTime = this.getTime;
        webSocketService.chatCacheDao = this.chatCacheDao;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userid") String userid,@PathParam("username") String username) {
        Map<String,Object> message=new HashMap<String, Object>();
        this.session = session;
        this.nickname = username;
        if(!map.containsKey(userid)){
            map.put(userid, session);
            webSocketSet.add(this);//加入set中
            nameMap.put(userid,this.nickname);
            noticeOtherUsers(userid,username,1);
        }
        //this.session.getAsyncRemote().sendText("恭喜" + nickname + "成功连接上WebSocket(其频道号：" + session.getId() + ")-->当前在线人数为：" + webSocketSet.size());
        message.put("type",0); //消息类型，0-连接成功，1-用户消息
        message.put("people",webSocketSet.size()); //在线人数
        message.put("name",this.nickname); //昵称
        message.put("aisle",userid); //频道号
        List<User> userList = getNameMap(userid);
        String userListString = JSONArray.toJSONString(userList);
        message.put("userList", userListString);
        System.out.println("有新连接加入:" + nickname + ",当前在线人数为" + userList.size()+" "+userListString);
        System.out.println(map.toString());
        System.out.println(nameMap.toString());
        //获取离线消息
        List<ChatCache> list = webSocketService.chatCacheDao.selectByAcceptUserId(userid);
        System.out.println(list.toString());
        this.session.getAsyncRemote().sendText(new Gson().toJson(message));
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userid") String userid,@PathParam("username") String username) {
        webSocketSet.remove(this); //从set中删除
        map.remove(userid);
        nameMap.remove(userid);
        System.out.println(username+"下线了,当前在线人数"+map.size());
        System.out.println(map.toString());
        System.out.println(nameMap.toString());
        //通知其他用户刷新数据
        noticeOtherUsers(userid,username,0);
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userid") String userid,@PathParam("username") String username) {

        System.out.println("来自客户端的消息-->" + username + ": " + message);

        //从客户端传过来的数据是json数据，所以这里使用jackson进行转换为SocketMsg对象，
        // 然后通过socketMsg的type进行判断是单聊还是群聊，进行相应的处理:
        ObjectMapper objectMapper = new ObjectMapper();
        SocketMsg socketMsg;

        try {
            socketMsg = objectMapper.readValue(message, SocketMsg.class);
            if (socketMsg.getType() == 1) {
                //单聊.需要找到发送者和接受者.
                Session fromSession = map.get(socketMsg.getFromUser());
                Session toSession = map.get(socketMsg.getToUser());
                //发送给接受者.
                if (toSession != null) {
                    //发送给发送者.
                    m = getMessage(1, socketMsg);
                    System.out.println(map.toString());
                    System.out.println(nameMap.toString());
                    fromSession.getAsyncRemote().sendText(new Gson().toJson(m));
                    toSession.getAsyncRemote().sendText(new Gson().toJson(m));
                } else {
                    System.out.println("用户"+socketMsg.getToUser()+"不在线");
                    //提示发送者对方不在线
                    fromSession.getAsyncRemote().sendText("对方不在线");
                    //将离线消息保存到数据库
                    ChatCache chatCache = new ChatCache();
                    chatCache.setMessage(socketMsg.getMsg());
                    chatCache.setId(webSocketService.chatCacheDao.getNewId());
                    chatCache.setSendtime(new Date());
                    chatCache.setSenduserid(Integer.parseInt(socketMsg.getFromUser()));
                    chatCache.setAcceptuserid(Integer.parseInt(socketMsg.getToUser()));
                    chatCache.setSendusername(username);
                    webSocketService.chatCacheDao.insertSelective(chatCache);
                }
            } else {
                //群发消息
                m = getMessage(1, socketMsg);
                System.out.println(m.toString());
                broadcast(new Gson().toJson(m));
            }

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 群发自定义消息
     */
    public void broadcast(String message) {
        for (WebSocketService item : webSocketSet) {
            item.session.getAsyncRemote().sendText(message);//异步发送消息.
        }
    }

    //生成返回前端的消息
    public Map<String,Object> getMessage(int type,SocketMsg socketMsg){
        Map<String,Object> map = new HashMap<>();
        map.put("type",type);
        map.put("name",socketMsg.getFromUserName());
        map.put("msg",socketMsg.getMsg());
        map.put("date", webSocketService.getTime.gettime());
        map.put("id", socketMsg.getToUser());
        map.put("id2", socketMsg.getFromUser());
        return map;
    }

    //获取在线用户的姓名
    public List<User> getNameMap(String myId){
        //记录所有在线的用户信息
        List<User> userList = new ArrayList<>();
        //获取nameMap里所有在线的用户名字
        Iterator iterator = nameMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            User user = new User();
            user.setId(Integer.parseInt((String) entry.getKey()));
            user.setName((String) entry.getValue());
            userList.add(user);
        }
        return userList;
    }

    //通知其他用户刷新数据
    public void noticeOtherUsers(String myId,String myName,int type){
        Iterator iterator = map.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            if (!entry.getKey().equals(myId)) {
                if(type == 1){//1为上线通知。0为下线通知
                    System.out.println("用户"+myId+"上线，通知用户："+entry.getKey());
                    map.get((String)entry.getKey()).getAsyncRemote().sendText("{\"msg\":\"refresh\",\"name\":\"" + myName + "\",\"id\":\"" + myId + "\"}");
                }
                else{
                    System.out.println("用户"+myId+"下线，通知用户："+entry.getKey());
                    map.get((String)entry.getKey()).getAsyncRemote().sendText("{\"msg\":\"refresh2\",\"name\":\"" + myName + "\",\"id\":\"" + myId + "\"}");
                }

            }
        }
    }

}
