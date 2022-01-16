package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * 登录处理逻辑
 *
 * @author lsz on 2022/1/13
 */
@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    /**
     * 定位注册页
     * @return
     */
    @GetMapping(value = "/register")
    public String getRegisterPage() {
        return "site/register";
    }

    /**
     * 注册功能
     * @param model
     * @param user
     * @return
     */
    @PostMapping(value = "/register")
    public String register(Model model, User user) {
        // 根据返回值map做出响应
        Map<String, Object> map = userService.register(user);
        // map是存放错误信息的；若为空，则注册成功！
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            // 三种可能会存在的错误信息，无则为空
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }

    }

    /**
     * 定位登入页
     * @return
     */
    @GetMapping(value = "/login")
    public String loginPage(){
        return "site/login";
    }

    /**
     * 激活账号
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @GetMapping(value = "/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable(value = "userId") int userId, @PathVariable(value = "code") String code){
        int result = userService.activation(userId, code);
        switch (result) {
            case ACTIVATION_SUCCESS: //激活成功
            {
                model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
                model.addAttribute("target", "/login");
                break;
            }
            case ACTIVATION_REPEAT: // 重复激活
            {
                model.addAttribute("msg", "无效操作，该账号已经激活过了！");
                model.addAttribute("target", "/index");
                break;
            }
            case ACTIVATION_FAILURE: // 激活失败
            {
                model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
                model.addAttribute("target", "/index");
                break;
            }
            default:
        }
        return "site/operate-result";
    }

    /**
     * 生成验证码
     * @param response
     * @param session
     */
    @GetMapping(value = "/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        // 生成验证码
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        // 将验证码存入session
        session.setAttribute("kaptcha",text);
        // 输出图片到浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            LOGGER.error("响应验证码失败：" + e.getMessage());
        }
    }

    /**
     * 登入
     * @param username
     * @param password
     * @param code
     * @param rememberme
     * @param model
     * @param session
     * @param response
     * @return
     */
    @PostMapping(value = "/login")
    public String login(String username,String password,String code,boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response){

        // 1 检查验证码
        String kaptcha = (String) session.getAttribute("kaptcha"); // 取出验证码
        if (StringUtils.isBlank(kaptcha)
                || StringUtils.isBlank(code)
                || !kaptcha.equalsIgnoreCase(code)) { // 验证码不区分大小写!
            model.addAttribute("codeMsg", "验证码不正确！"); // 验证码提示
            return "site/login";
        }
        // 2 检查账号，密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS; // 设置超时时间
        // 登录, 凭证录入数据库中, 返回 <"ticket", ticket凭证字符串对象>
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        String key = "ticket";
        // 若map里包含ticket，则登录成功，跳转到首页页面
        if (map.containsKey(key)) {
            Cookie cookie = new Cookie(key, map.get(key).toString());
            cookie.setPath(contextPath); // 设置有限范围: 整个项目cookie都有效
            cookie.setMaxAge(expiredSeconds); // 设置有效时间
            response.addCookie(cookie); // 提交cookie给服务器
            return "redirect:/index";
        } else {
            // 登录失败: 可能会出错且包含的信息，重新登录！
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    /**
     * 忘记密码
     * @return
     */
    @GetMapping("/forget")
    public String getForgetPage() {
        return "/site/forget";
    }

    /**
     * 获取验证码
     * @param email
     * @param session
     * @return
     */
    @GetMapping("/forget/code")
    @ResponseBody
    public String getForgetCode(String email, HttpSession session) {
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("verifyCode", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);

        // 保存验证码
        session.setAttribute("verifyCode", code);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 重置密码
     * @param email
     * @param verifyCode
     * @param password
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {
            return "redirect:/login";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }

    // 退出
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket); // 登录凭证设置为失效状态：1
        return "redirect:/login"; // 跳转到登录页面
    }
}