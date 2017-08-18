package com.gs.dao.impl;

import com.gs.bean.User;
import com.gs.common.bean.Pager;
import com.gs.common.util.DateUtil;
import com.gs.common.util.PhoneUtil;
import com.gs.dao.AbstractBaseDAO;
import com.gs.dao.UserDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wang Genshen on 2017-07-04.
 */
public class UserDAOImpl extends AbstractBaseDAO implements UserDAO {
    @Override
    public void add(User user) {
        getConnection();
        String sql = "insert into t_user(access_token, openid, wechat_nickname, gender) values(?, ?, ?, ?)";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getAccessToken());
            preparedStatement.setString(2, user.getOpenId());
            preparedStatement.setString(3, user.getWechatNickname());
            preparedStatement.setString(4, user.getGender());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();
    }

    @Override
    public void update(User user) {

    }

    @Override
    public void valid(Integer integer, String status) {

    }

    @Override
    public User queryById(Integer integer) {
        return null;
    }

    @Override
    public User queryByPhone(String phone) {
        return null;
    }

    @Override
    public User queryByOpenId(String openId) {
        getConnection();
        String sql = "select * from t_user where openid = ?";
        User user = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, openId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setAccessToken(resultSet.getString("access_token"));
                user.setAccessToken1(resultSet.getString("access_token1"));
                user.setOpenId(openId);
                user.setWechatNickname(resultSet.getString("wechat_nickname"));
                user.setGender(resultSet.getString("gender"));
                user.setPhone(resultSet.getString("phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();
        return user;
    }

    @Override
    public List<User> queryAll() {
        return null;
    }

    @Override
    public List<User> queryByPager(Pager<User> pager) {
        return null;
    }

    @Override
    public List<User> queryAllPrized() {
        getConnection();
        String sql = "select * from t_user where prized = 1";
        List<User> users = new ArrayList<User>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setAccessToken(resultSet.getString("access_token"));
                user.setAccessToken1(resultSet.getString("access_token1"));
                user.setOpenId(resultSet.getString("openid"));
                user.setWechatNickname(resultSet.getString("wechat_nickname"));
                user.setGender(resultSet.getString("gender"));
                user.setPhone(resultSet.getString("phone"));
                user.setHidePhone(PhoneUtil.hidePhone(user.getPhone()));
                user.setPayedFee(resultSet.getInt("payed_fee"));
                user.setPayedTime(resultSet.getDate("payed_time"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();
        return users;
    }

    public void updatePhone(String openid, String phone) {
        getConnection();
        String sql = "update t_user set phone = ? where openid = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, phone);
            preparedStatement.setString(2, openid);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();
    }

    public void batchUpdate(List<User> users) {
        getConnection();
        String sql = "update t_user set payed_fee = ?, payed_time = ?, trade_no = ?, tran_id = ?, prized = ? where openid = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int i = 0, size = users.size(); i < size; i++) {
                User user = users.get(i);
                ps.setDouble(1, user.getPayedFee());
                ps.setDate(2, DateUtil.convert(user.getPayedTime()));
                ps.setString(3, user.getTradeNo());
                ps.setString(4, user.getTranId());
                ps.setInt(5, user.getPrized());
                ps.setString(6, user.getOpenId());
                ps.addBatch();
            }
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();
    }
}
