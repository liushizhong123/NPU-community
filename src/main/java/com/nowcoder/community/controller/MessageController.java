package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 私信逻辑实现
 *
 * @author lsz on 2022/1/21
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 获取用户回话列表，每次会话只显示最新的消息
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page){
        // 获取当前用户
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        // 存储返回模板数据
        List<Map<String,Object>> conversations = new ArrayList<>();
        // 遍历会话列表
        if(conversationList != null){
            for (Message message: conversationList){
                Map<String,Object> map = new HashMap<>();
                // 最新的一条私信
                map.put("conversation",message);
                // 私信数量
                map.put("letterCount",messageService.findLetterCount(
                        message.getConversationId()));
                // 私信未读数量
                map.put("unreadCount",messageService.findLetterUnreadCount(
                        user.getId(),message.getConversationId()));
                // 私信用户
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }

    /**
     * 获取私信详情
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" +conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(
                conversationId,page.getOffset(),page.getLimit());
        // 传给模板的变量存储集合
        List<Map<String,Object>> letters = new ArrayList<>();
        // 遍历私信列表
        if(letterList != null){
            for(Message message : letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        // 设置私信目标(fromUser)
        model.addAttribute("target", getLetterTarget(conversationId));

        // 如果当前用户是接收者就设置私信已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    /**
     * 得到未读私信列表id集合
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            for(Message message : letterList){
                // 当前用户是信息的接收者，且当前消息是未读状态
                if(hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 得到私信目标用户
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int from = Integer.parseInt(ids[0]);
        int to = Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId() == from){
            return userService.findUserById(to);
        }else {
            return userService.findUserById(from);
        }
    }

    /**
     * 发送消息
     * @param toName 目标用户
     * @param content 消息内容
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        if(target.equals(hostHolder.getUser())){
            return CommunityUtil.getJSONString(1,"不能给自己发私信！");
        }
        // 构造消息
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId()); // 设置当前用户
        message.setToId(target.getId());
        // 会话 id 格式为 小的_大的
        message.setConversationId(
                message.getFromId() < message.getToId()
                        ? message.getFromId() + "_" + message.getToId()
                        : message.getToId() + "_" + message.getFromId());
        message.setContent(content);
        message.setCreateTime(new Date());
        // 执行添加消息业务逻辑
        messageService.addMessage(message);

        // 成功返回状态0
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除消息
     * @param id
     * @return
     */
    @PostMapping("letter/delete")
    @ResponseBody
    public String deleteLetter(int id){
        messageService.deleteMessage(id);
        return CommunityUtil.getJSONString(0);
    }
}