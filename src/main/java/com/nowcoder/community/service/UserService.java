package com.nowcoder.community.service;

import com.nowcoder.community.CommunityApplication;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     * 注册功能实现
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 验证处理: 是否可以查到用户, 是, 则代表已存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // -------------注册用户开始-----------
        // 注册用户：将信息存入数据库
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5)); // 设置salt为随机生成的字符串
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt())); // 对密码进行加salt再md5加密
        user.setType(0); // 普通用户
        user.setStatus(0); // 没有激活
        user.setActivationCode(CommunityUtil.generateUUID()); // 生成随机激活码
        user.setHeaderUrl(
                String.format(
                        "http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000))); // 设置牛客网随机头像
        user.setCreateTime(new Date());
        userMapper.insertUser(user); // 写入数据库

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8083/community/activation/101/code //自定义激活url
        String url = domain
                        + contextPath
                        + "/activation/"
                        + user.getId()
                        + "/"
                        + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        // -------------注册用户结束-----------

        return map;
    }

    /**
     * 激活功能实现
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            // 1 代表已经激活
            return ACTIVATION_REPEAT; // 重复激活
        } else if (user.getActivationCode().equals(code)) {
            // 相等代表激活码相同即激活成功
            userMapper.updateStatus(userId, 1); // 更改激活状态：0->1
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 登入功能实现
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    @PostMapping(value = "/login")
    public Map<String ,Object> login(String username,String password,int expiredSeconds){
        Map<String, Object> map = new HashMap<>(16);

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！"); // `status` int(11) DEFAULT NULL COMMENT '0-未激活; 1-已激活;',
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 若上面的验证都通过, 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID()); // 生成随机字符串
        loginTicket.setStatus(0); // 有效状态
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000)); // 现在的时间加上过期时间（毫秒）
        // 存入登录凭证到数据库
        loginTicketMapper.insertLoginTicket(loginTicket);
        // 浏览器只需要存 ticket, 通过它可以查询到登录凭证即可
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登入
     * @param ticket
     */
    public void logout(String ticket) {
       loginTicketMapper.updateStatus(ticket,1);
    }

    /**
     * 更新用户头像
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    /**
     * 更新用户密码
     * @param userId 用户 id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @return
     */
    public Map<String,Object> updatePassword(int userId,String oldPassword,String newPassword,String confirmPassword){
        Map<String,Object> map = new HashMap<>();

        // 判空处理
        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原始密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(confirmPassword)){
            map.put("confirmPasswordMsg","确认密码不能为空！");
            return map;
        }
        if(!confirmPassword.equals(newPassword)){
            map.put("confirmPasswordMsg","两次输入的新密码不相同！");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原始密码输入有误!");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);

        return map;
    }

    /**
     * 重置密码
     * @param email
     * @param password
     * @return
     */
    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }

        // 重置密码
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);

        map.put("user", user);
        return map;
    }
}
