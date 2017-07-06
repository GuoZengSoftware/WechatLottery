package com.gs.controller;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.gs.bean.User;
import com.gs.common.Constants;
import com.gs.common.WebUtil;
import com.gs.common.WechatAPI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Wang Genshen on 2017-07-05.
 */
@WebServlet(name = "PayServlet", urlPatterns = "/pay/*")
public class PayServlet extends HttpServlet {
    private static final long serialVersionUID = -7542686547304843900L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = WebUtil.getReqMethod(req);
        if (method.equals("pay")) {
            pay(req, resp);
        } else if (method.equals("result")) {
            result(req, resp);
        }
    }

    private void pay(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(Constants.LOGINED_USER);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(WechatAPI.ORDER_URL);
        httpPost.addHeader("Content-Type", "text/xml");
        try {
            Map<String, String> reqData = prepayData(user.getOpenId(), req.getRemoteAddr());
            String data = WXPayUtil.mapToXml(reqData);
            System.out.println("post data: \n" + data);

            if (data != null) {
                StringEntity stringEntity = new StringEntity(data, Constants.DEFAULT_ENCODING);
                httpPost.setEntity(stringEntity);
            }
            String result = httpclient.execute(httpPost, responseHandler);
            if (result != null) {
                result = new String(result.getBytes(Constants.ISO_ENCODING), Constants.DEFAULT_ENCODING);
            }
            System.out.println("result: \n" + result);
            Map<String, String> prepayData = WXPayUtil.xmlToMap(result); // 获取预支付结果
            Map<String, String> payData = payData(prepayData);
            Set<Map.Entry<String, String>> entrySet1 = payData.entrySet();
            Iterator<Map.Entry<String, String>> iterator1 = entrySet1.iterator();
            while (iterator1.hasNext()) {
                Map.Entry<String, String> entry = iterator1.next();
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            req.setAttribute("appId", WechatAPI.APP_ID);
            req.setAttribute("timeStamp", payData.get("timeStamp"));
            req.setAttribute("nonceStr", payData.get("nonceStr"));
            req.setAttribute("packages", payData.get("package"));
            req.setAttribute("paySign", payData.get("paySign"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        req.getRequestDispatcher("/pay.jsp").forward(req, resp); // 转发到页面，调用js支付
    }

    private ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

        @Override
        public String handleResponse(final HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }

    };

    private void result(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /**
         * <xml><appid><![CDATA[wxbb5d044b94663978]]></appid>
         <bank_type><![CDATA[CFT]]></bank_type>
         <cash_fee><![CDATA[1]]></cash_fee>
         <fee_type><![CDATA[CNY]]></fee_type>
         <is_subscribe><![CDATA[Y]]></is_subscribe>
         <mch_id><![CDATA[1483257182]]></mch_id>
         <nonce_str><![CDATA[77c2b9fdd98f4304abe5b467aefbcfd8]]></nonce_str>
         <openid><![CDATA[olEzovssKwMKtvyEe7rCAaqr90VM]]></openid>
         <out_trade_no><![CDATA[b6b2b4c8d2334f11b2cebd7a992d3f7c]]></out_trade_no>
         <result_code><![CDATA[SUCCESS]]></result_code>
         <return_code><![CDATA[SUCCESS]]></return_code>
         <sign><![CDATA[856EFA24722D8EC7DC69C31066C5F0A6]]></sign>
         <time_end><![CDATA[20170706150303]]></time_end>
         <total_fee>1</total_fee>
         <trade_type><![CDATA[JSAPI]]></trade_type>
         <transaction_id><![CDATA[4004602001201707069181296658]]></transaction_id>
         </xml>
         */
        System.out.println("***********************notify_url*************************");
        ServletInputStream in = req.getInputStream();
        byte[] bytes = new byte[1024];
        int total = 0;
        StringBuffer s = new StringBuffer();
        while ((total = in.read(bytes)) != -1) {
            s.append(new String(bytes, 0, total));
        }
        System.out.println(s);
        resp.setContentType("text/xml;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.write(WechatAPI.NOTIFY_RESULT);
    }

    private Map<String, String> prepayData(String openid, String ip) {
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", WechatAPI.APP_ID);
        reqData.put("mch_id", WechatAPI.MCH_ID);
        reqData.put("nonce_str", WXPayUtil.generateUUID());
        reqData.put("sign_type", WXPayConstants.MD5);
        reqData.put("openid", openid);
        reqData.put("body", "付款");
        reqData.put("out_trade_no", WXPayUtil.generateUUID());
        reqData.put("total_fee", "1");
        reqData.put("trade_type", WechatAPI.TRADE_JSAPI);
        reqData.put("spbill_create_ip", ip);
        reqData.put("notify_url", WechatAPI.NOTIFY_URL);
        try {
            reqData.put("sign", WXPayUtil.generateSignature(reqData, WechatAPI.API_KEY, WXPayConstants.SignType.MD5));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reqData;
    }

    private Map<String, String> payData(Map<String, String> prepayData) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("appId", WechatAPI.APP_ID);
        data.put("package", "prepay_id=" + prepayData.get("prepay_id"));
        data.put("timeStamp", WXPayUtil.getCurrentTimestamp() + "");
        data.put("nonceStr", WXPayUtil.generateUUID());
        data.put("signType", WXPayConstants.MD5);
        try {
            data.put("paySign", WXPayUtil.generateSignature(data, WechatAPI.API_KEY, WXPayConstants.SignType.MD5));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}