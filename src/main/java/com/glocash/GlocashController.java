package com.glocash;
import com.alibaba.fastjson.JSON;
import com.glocash.log.DefaultLog;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

//https://portal.glocashpayment.com/#/integration/secretkey 秘钥获取
//https://docs.glocash.com/#/ 接口文档

@RestController

@RequestMapping("/transaction")
public class GlocashController {
    private static final int TRANSACTION_CREATE = 1;
    private static final int TRANSACTION_QUERY = 2;
    private static final int TRANSACTION_NOTIFY = 3;
    private static final int TRANSACTION_REFUNDED = 4;
    @Value("${glocash.live.url}")
    private String gatewayDomain;

    @Value("${glocash.sandbox.url}")
    private String sandboxGatewayDomain = "https://sandbox.glocashpayment.com";

    @Value("${glocash.sandbox.key}")
    private  String sandbox_key;

    @Value("${glocash.live.key}")
    private  String live_key;

    @Value("${glocash.mchEmail}")
    private  String mchEmail;

    @Value("${glocash.liveMode}")
    private  Boolean LiveMode;

    @Value("${glocash.debug}")
    private Boolean debug ;//正式环境请关闭 避免大量日志造成服务器压力

    private  String transactionUrl    = "/gateway/payment/index"; //交易地址
    private  String queryUrl    = "/gateway/transaction/index"; //查询地址
    private  String refundUrl    = "/gateway/transaction/refund"; //退款地址

    @GetMapping("/create/{orderId}")
    public String  createPayment(@PathVariable("orderId") String orderNo) {
        long timeStampSec = System.currentTimeMillis()/1000;
        String timestamp = String.format("%010d", timeStampSec);
        String timeTemp = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date(timeStampSec * 1000));
        Random rand = new Random();
        orderNo = "TESTchenrj"+timeTemp+orderNo;
        Map<String,String> data = new HashMap<>();
        String sandbox = LiveMode? "0" : "1";
        data.put("REQ_SANDBOX", sandbox);  //TODO 是否开启测试模式 0 正式环境 1 测试环境
        data.put("REQ_EMAIL", mchEmail);    //TODO 需要换成自己的 商户邮箱 商户后台申请的邮箱
        data.put("REQ_TIMES", timestamp);    //请求时间
        data.put("REQ_INVOICE", orderNo);    //订单号
        data.put("REQ_MERCHANT", "Merchant Name"); //商户名
        data.put("REQ_APPID", "380"); //应用ID
        data.put("CUS_EMAIL", "rongjiang.chen@witsion.com");    //客户邮箱
        data.put("BIL_METHOD", "C01");    //请求方式
        data.put("BIL_GOODSNAME", "#gold#Runescape/OSRS Old School/ 10M Gold");  //TODO 商品名称必填 而且必须是正确的否则无法结算
        data.put("BIL_PRICE", "1.2");    //价格
        data.put("BIL_CURRENCY", "USD");    //币种
        data.put("BIL_CC3DS", "1");    //是否开启3ds 1 开启 0 不开启
        data.put("URL_SUCCESS", String.format("http://java.crjblog.cn/transaction/success/"+orderNo+"?id=test123&status=paid"));    //支付成功跳转页面
        data.put("URL_FAILED", "http://java.crjblog.cn/transaction/failed/"+orderNo+"?id=1&status=2");    //支付失败跳转页面
        data.put("URL_NOTIFY", String.format("http://java.crjblog.cn/transaction/notify?order_id=%s",orderNo));    //异步回调跳转页面
        String sign = this.sign(TRANSACTION_CREATE,data);
        data.put("REQ_SIGN", sign);    //异步回调跳转页面
        DefaultLog log = new DefaultLog();
        log.showLog("transaction create data",data.toString());
        String url = getUrl(TRANSACTION_CREATE);
        try{
            String result = HttpClient(url,data);
            log("transaction create return",result);
            return result;
        }catch (Exception e){
            log("transaction create error",e.getMessage());
        }
        return "";
    }



    @RequestMapping(value = "/success/{tns_id}",method = {RequestMethod.POST,RequestMethod.GET})
    public String success(@PathVariable("tns_id") String tns_id){
        //TODO https://portal.glocashpayment.com/#/integration/urlconfig 商户后台可以调整跳转的请求方式 即可控制为post还是get
        return tns_id+"success";
    }

    @RequestMapping(value = "/failed/{tns_id}",method = {RequestMethod.POST,RequestMethod.GET})
    public String failed(@PathVariable("tns_id") String tns_id){
        //TODO 自定义失败页面
        return tns_id+"failed";
    }


    @GetMapping("/query/{gcid}")
    public String queryTransaction(@PathVariable("gcid") String gcid){
        long timeStampSec = System.currentTimeMillis()/1000;
        String timestamp = String.format("%010d", timeStampSec);
        Map<String,String> data = new HashMap<>();
        data.put("REQ_TIMES", timestamp);
        data.put("REQ_EMAIL", mchEmail);
        data.put("TNS_GCID", gcid);    //订单号
        String sign = this.sign(TRANSACTION_QUERY, data);
        data.put("REQ_SIGN", sign);    //请求时间
        String url = getUrl(TRANSACTION_QUERY);
        try{
            String result = HttpClient(url,data);
            log("transaction query return",result);
            return result;
        }catch (Exception e){
            log("transaction query error",e.getMessage());
            return e.getMessage();
        }
    }

    public String getOrderStatus(String Invoice){
        //TODO 具体的状态需要去你们系统查询
        return "0";
    }

    @PostMapping("/notify")
    public String notify( @RequestParam Map<String, String> params){
        log("notify data",params.toString());
        String SignString = this.sign(TRANSACTION_NOTIFY,params);
        String RequestSign = params.get("REQ_SIGN");
        log("gc sign",RequestSign);
        log("make sign",SignString);
        if(SignString.equals(RequestSign) == false){
            return "sign error";
        }
        log("sign success","");
        String Invoice = params.get("REQ_INVOICE");
        String NotifyType = params.get("PGW_NOTIFYTYPE");
        String GcStatus = params.get("BIL_STATUS");

        //TODO 通过Invoice 去你们系统查询当前交易的状态
        //TODO 假如 1 未支付 2 已付款  3为退款中 4全部退款 具体看你们系统业务逻辑
        String status = this.getOrderStatus(Invoice);
        //通知交易情况
        if(NotifyType.isEmpty()){
            return "NotifyType is null";
        }
        if(NotifyType.equals("transaction")){
            if(status.equals("1")){
                //判断是否已经更新过状态
                return "status is update";
            }
            if(GcStatus.equals("paid")){
                //更新状态为paid
            }
        }else if(NotifyType.equals("refunded")){
            if(!status.equals("2") && !status.equals("3")){
                return "status is update";
            }
            if(GcStatus.equals("refunding")){
                //TODO 更新状态到你们系统为退款中
            } else if(GcStatus.equals("refunded")){
                //TODO 更新退款
            }

        }
        return "success";
    }

    @GetMapping("/refund/{gcid}/{amount}")
    public String refund(@PathVariable("gcid")  String gcid,@PathVariable("amount")  String amount){
        long timeStampSec = System.currentTimeMillis()/1000;
        String timestamp = String.format("%010d", timeStampSec);
        Map<String, String> data = new HashMap<>();
        data.put("REQ_TIMES", timestamp);
        data.put("REQ_EMAIL", mchEmail);
        data.put("TNS_GCID", gcid);    //订单号
        data.put("PGW_PRICE", amount);    //订单号
        String sign = this.sign(TRANSACTION_REFUNDED, data);
        data.put("REQ_SIGN", sign);    //请求时间
        String url = getUrl(TRANSACTION_REFUNDED);
        try{
            String result = HttpClient(url,data);
            Map<String, Object> resultMap = (Map)JSON.parse(result);
            log("refund return",result);
            String ResultCode = resultMap.get("REQ_CODE").toString();
            String REQ_ERROR = resultMap.get("REQ_ERROR").toString();
            if(ResultCode.equals("200")){
                return "refunding success";
            }else{
                return REQ_ERROR;
            }
        }catch (Exception e){
            log("refund return error",e.getMessage());
            return e.getMessage();
        }

    }

    /**
     * http client 使用前需要导入相关jar包
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    public String HttpClient( String url, Map<String,String> data)  throws Exception {
        CloseableHttpClient httpClient =HttpClients.createDefault();
        log("request url",url);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for( String key : data.keySet()){
            params.add(new BasicNameValuePair(key, String.valueOf(data.get(key))));
        }
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
        httpPost.setEntity(formEntity);
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpClient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                // 解析响应体
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                return content;
            }
        } finally {
            if (response != null) {
                response.close();
            }
            // 关闭浏览器
            httpClient.close();
        }
        return "";
    }

    /**
     * 签名
     * @param type
     * @param data
     * @return
     */
    public String sign(Integer type,Map data){
        String key = LiveMode?live_key:sandbox_key;
        String sign = "";
        switch (type){
            case TRANSACTION_CREATE:
                sign =  key+data.get("REQ_TIMES")+data.get("REQ_EMAIL")+data.get("REQ_INVOICE")+data.get("CUS_EMAIL")+data.get("BIL_METHOD")+data.get("BIL_PRICE")+data.get("BIL_CURRENCY");
                break;
            case TRANSACTION_QUERY:
                sign =  key+data.get("REQ_TIMES")+data.get("REQ_EMAIL")+data.get("TNS_GCID");
                break;
            case TRANSACTION_NOTIFY:
                sign =  key+data.get("REQ_TIMES")+data.get("REQ_EMAIL")+data.get("CUS_EMAIL")+data.get("TNS_GCID")+data.get("BIL_STATUS")+data.get("BIL_METHOD")+data.get("PGW_PRICE")+data.get("PGW_CURRENCY");
                break;
            case TRANSACTION_REFUNDED:
                sign =  key+data.get("REQ_TIMES")+data.get("REQ_EMAIL")+data.get("TNS_GCID")+data.get("PGW_PRICE");
                break;
        }
        log("sign string"+type, sign);
        return getSHA256StrJava(sign);
    }

    /**
     * 生成对应的链接
     * @param type
     * @return
     */
    public String getUrl(Integer type){
        String base_url = LiveMode?gatewayDomain:sandboxGatewayDomain;
        String url = "";
        switch (type){
            case TRANSACTION_CREATE:
                url = base_url+transactionUrl;break;
            case TRANSACTION_QUERY:
                url =  base_url+queryUrl;break;
            case TRANSACTION_REFUNDED:
                url =  base_url+refundUrl;break;
        }
        return url;
    }


    /**
     * hash sha256 加密
     * @param str
     * @return
     */
    private String getSHA256StrJava(String str){
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    private String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i=0;i<bytes.length;i++){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 打印日志
     * @param title
     * @param data
     */
    private void log(String title,String data){
        if(debug){
            System.out.println(title+":<"+data+">");
        }
    }

}