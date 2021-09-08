package com.glocash.result;

public class ResultData {

    private static final int SUCCESS_CODE = 200 ;
    private static final int ERROR_CODE   = -1;


    private int code;

    private String data;

    private Exception exception;

    private String msgInfo;

    public int getCode() {
        return code;
    }


    public String getData() {
        return data;
    }


    public Exception getException() {
        return exception;
    }


    public String getMsgInfo() {
        return msgInfo;
    }


    @Override
    public String toString() {
        return "ResultData{" +
                "code=" + code +
                ", data='" + data + '\'' +
                ", exception=" + exception +
                ", msgInfo='" + msgInfo + '\'' +
                '}';
    }

    public static ResultData createSuccessResult(String data) {
        ResultData resultData = new ResultData();
        resultData.code = SUCCESS_CODE;
        resultData.data = data;
        resultData.msgInfo = "handle success";

        return resultData;
    }


    public static ResultData createErrorResult(Exception exception, String errorMsg) {
        ResultData resultData = new ResultData();
        resultData.code = ERROR_CODE;
        resultData.exception = exception;
        resultData.msgInfo = errorMsg;

        return resultData;
    }


}
