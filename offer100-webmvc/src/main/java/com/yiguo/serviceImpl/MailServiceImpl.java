package com.yiguo.serviceImpl;

import com.yiguo.bean.User;
import com.yiguo.dao.BaseMapper;
import com.yiguo.dao.MailMapper;

import com.yiguo.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by will on 2017-06-22.
 */
@Service("Mail")
@Transactional
public class MailServiceImpl extends AbstractBaseServiceImpl<Integer, User> implements MailService {

	@Autowired
	MailMapper mailDao;

	@Override
	public BaseMapper<Integer, User> getDao() {
		// TODO Auto-generated method stub
		return mailDao;
	}

}
