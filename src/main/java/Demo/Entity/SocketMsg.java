package Demo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author Chenqiunian
 * Date 2022-04-16 20:40
 * Description 消息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocketMsg {
    private int type; //聊天类型0：群聊，1：单聊.
    private String fromUserName;//发送者姓名
    private String fromUser;//发送者id.
    private String toUser;//接受者id.
    private String msg;//消息
}
