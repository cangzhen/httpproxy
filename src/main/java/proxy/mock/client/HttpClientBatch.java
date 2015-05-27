/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package proxy.mock.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.service.GenerateRespObject;
import proxy.util.HttpUitls;

import com.google.gson.Gson;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 * {@link System#out} to test {@link HttpSnoopServer}.
 */
public final class HttpClientBatch {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientBatch.class);

    static final String URL = System.getProperty("url", "http://127.0.0.1:8080/");
    final String host;
    final int port ;
    final URI uri;
    EventLoopGroup group = new NioEventLoopGroup(2);
    public HttpClientBatch() throws URISyntaxException
    {
         uri = new URI(URL);
         host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
         port = uri.getPort();
    }
    
    static  final int Max_Concurent_Request_Num =100;
    public static void main(String[] args) throws Exception {
    	long start = System.currentTimeMillis();
    	TokenMgr tokenMgr = TokenMgr.getInstance();
    	tokenMgr.setTokenNum(Max_Concurent_Request_Num);
    	HttpClientBatch batch  = new HttpClientBatch();
    	batch.start(100000);
		while(tokenMgr.getTokenNum() != Max_Concurent_Request_Num)
		{	
			Thread.sleep(20);
		}
		logger.info("time ms="+(System.currentTimeMillis()-start));
		
		batch.shutdown();
    }
    public void shutdown() {
    	group.shutdownGracefully();
	}
	public  void start(int sendTimes) throws InterruptedException
    {
    	for(int i=0;i<sendTimes;i++)
    	{
    		while(!TokenMgr.getInstance().getToken())
    		{	
    			Thread.sleep(10);
    		}
    		sendRequect();
    		if(logger.isDebugEnabled())
    		{
	    		logger.debug("send request "+i);
    		}
    	}
    }
	public  void start(final int sentTimes,int nThread) throws InterruptedException
    {
		
		Thread threads[] = new Thread[nThread];
		final AtomicInteger counter = new AtomicInteger(0);
		for(int i=0;i<threads.length;i++)
		{
			threads[i] = new Thread( new Runnable() {
				
				@Override
				public void run() {
					do
					{
						int seq = counter.getAndAdd(1);
						if(seq > sentTimes)
							break;
			    		while(!TokenMgr.getInstance().getToken())
			    		{	
			    			try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			    		}
			    		try {
							sendRequect();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			    		if(logger.isDebugEnabled())
			    		{
				    		logger.debug("send request "+seq);
			    		}
					}while(true);
				}
			});
			threads[i].start();

		}
		for(int i=0;i<threads.length;i++)
		{
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}	
    }
    
    public void sendRequect() throws InterruptedException
    {
    	//创建发送对象
        // Prepare the HTTP request.
        String msg = new Gson().toJson(GenerateRespObject.getRequest());
        final FullHttpRequest request = HttpUitls.createHttpRequest(uri.getRawPath(),msg);
       
        // Make the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(group)
         .channel(NioSocketChannel.class)
         .handler(new ChannelInitializer<SocketChannel>() {
        	    @Override
        	    public void initChannel(SocketChannel ch) {
        	        ChannelPipeline p = ch.pipeline();
        	        p.addLast(new HttpClientCodec());
        	        p.addLast(new HttpClientHandler(request));
        	    }
        	});
        b.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                	TokenMgr.getInstance().returnToken();
                	logger.warn(" connect failed "+future.cause().getMessage());
                	future.channel().close();
                }
                else
                {
                	future.channel().writeAndFlush(request);
                }
            }
        });
 
    }
}
