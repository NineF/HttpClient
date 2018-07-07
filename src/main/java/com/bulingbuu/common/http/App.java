package com.bulingbuu.common.http;

import com.bulingbuu.common.http.connection.NettyHttpClient;
import com.bulingbuu.common.http.resp.APIConnectionException;
import com.bulingbuu.common.http.resp.APIRequestException;
import com.bulingbuu.common.http.resp.ResponseWrapper;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ClientConfig config=ClientConfig.getInstance();
        NettyHttpClient client=new NettyHttpClient(null,config);
        client.set_workerThreadNum(1);
        try {
            ResponseWrapper responseWrapper=client.sendGet("http://localhost:8081/");
            System.out.println(responseWrapper.responseCode);
            System.out.println(responseWrapper.responseContent);
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }
    }
}
