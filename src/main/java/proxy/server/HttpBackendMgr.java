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
package proxy.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.domain.TaskRequest;
import proxy.util.HttpUitls;

import com.google.gson.Gson;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 * {@link System#out} to test {@link HttpSnoopServer}.
 */
public final class HttpBackendMgr {
    private static final Logger logger = LoggerFactory.getLogger(HttpBackendMgr.class);


    final String host;
    final int port ;
    final String path;
    
    EventLoopGroup group = new NioEventLoopGroup(4);
    
    public HttpBackendMgr() 
    {
         host = "127.0.0.1";
         port = 8081;
         path = "/test";
    }
   
    public void forwardRequect(final ChannelHandlerContext frontCtx, TaskRequest request2) throws InterruptedException
    {
    	//创建发送对象
        // Prepare the HTTP request.
    	String content = new Gson().toJson(request2);
        final FullHttpRequest request = HttpUitls.createHttpRequest(path,content);
        // Make the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(group)
         .channel(NioSocketChannel.class)
         .handler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) throws Exception {
            	 ChannelPipeline p = ch.pipeline();
                 p.addLast(new HttpClientCodec());
                 p.addLast(new HttpContentDecompressor());
                 p.addLast(new HttpBackendHandler(frontCtx,request));
             }
         });
        
        b.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                	future.channel().close();
                	
                	String error = "connect backend failed "+future.cause().getMessage();
                	logger.warn(error);
                    FullHttpResponse response = HttpUitls.createHttpResponse(HttpResponseStatus.NOT_FOUND,error); 
                    frontCtx.writeAndFlush(response);
                }
                else
                {
                	future.channel().writeAndFlush(request);
                }
            }
        });
 
    }



    
    
}
