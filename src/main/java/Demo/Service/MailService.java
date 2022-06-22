package Demo.Service;

import Demo.Entity.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.Date;

@Service
public class MailService {
    /**
     * 后加的防止题目过长并且进行全局定义
     */
    static {
        System.setProperty("mail.mime.splitlongparameters", "false");
        System.setProperty("mail.mime.charset", "UTF-8");
    }

    @Autowired
    private JavaMailSenderImpl mailSender;//注入邮件工具类

    public Mail sendMail(Mail mail) {
        try {
            checkMail(mail); //1.检测邮件

            //return saveMail(mail); //3.保存邮件
            return sendMimeMail(mail); //2.发送邮件;
        } catch (Exception e) {
            mail.setStatus("fail");
            mail.setError(e.getMessage());
            return mail;
        }
    }

    //检测邮件信息类
    private void checkMail(Mail mail) {
        if (StringUtils.isEmpty(mail.getTo())) {
            throw new RuntimeException("邮件收信人不能为空");
        }
        if (StringUtils.isEmpty(mail.getSubject())) {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(mail.getText())) {
            throw new RuntimeException("邮件内容不能为空");
        }
    }

    //构建复杂邮件信息类
    private Mail sendMimeMail(Mail mail) {
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);//true表示支持复杂类型

            //Mail.setFrom(getMailSendFrom());//邮件发信人从配置项读取
            messageHelper.setFrom(mail.getFrom());//邮件发信人
            messageHelper.setTo(mail.getTo().split(","));//邮件收信人
            messageHelper.setSubject(mail.getSubject());//邮件主题
            messageHelper.setText(mail.getText(),true);//邮件内容
            if (!StringUtils.isEmpty(mail.getCc())) {//抄送
                messageHelper.setCc(mail.getCc().split(","));
            }
            if (!StringUtils.isEmpty(mail.getBcc())) {//密送
                messageHelper.setCc(mail.getBcc().split(","));
            }
            if (mail.getMultipartFiles() != null) {//添加邮件附件
                for (MultipartFile multipartFile : mail.getMultipartFiles()) {
                    messageHelper.addAttachment(multipartFile.getOriginalFilename(), multipartFile);
                }
            }
            if (mail.getSentDate()==null) {//发送时间
                mail.setSentDate(new Date());
                messageHelper.setSentDate(mail.getSentDate());
            }
            //邮件中显示图片
//            ClassPathResource resource = new ClassPathResource("static/images/rmb.jpg");
//            messageHelper.addInline("rmb",resource);

            mailSender.send(messageHelper.getMimeMessage());//正式发送邮件
            mail.setStatus("ok");
            return mail;
        } catch (Exception e) {
            throw new RuntimeException(e);//发送失败
        }
    }

    //保存邮件
    /*
    private Mail saveMail(Mail Mail) {

        //将邮件保存到数据库..

        return Mail;
    }

    //获取邮件发信人

    public String getMailSendFrom() {
        return mailSender.getJavaMailProperties().getProperty("from");
    }*/


}
