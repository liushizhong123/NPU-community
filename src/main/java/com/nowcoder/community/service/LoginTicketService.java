package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.entity.LoginTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录凭证
 *
 * @author lsz on 2022/1/14
 */
@Service
public class LoginTicketService {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    /**
     * 通过ticket查询LoginTicket对象
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }
}