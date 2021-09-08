package com.glocash.log;

public class DefaultLog implements Log {

    private static final boolean IS_DEBUG = true ;

    @Override
    public void  showLog(String title,String data) {
        if(IS_DEBUG) {
            System.out.println(title+":<"+data+">");
        }
    }
}
