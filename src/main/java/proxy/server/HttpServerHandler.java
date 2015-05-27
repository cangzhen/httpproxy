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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.domain.TaskRequest;
import proxy.util.HttpUitls;

import com.google.gson.Gson;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

	final HttpBackendMgr httpBackendMgr;
    private final StringBuilder buf = new StringBuilder();
    public HttpServerHandler(HttpBackendMgr httpBackendMgr) {
    	this.httpBackendMgr = httpBackendMgr;
    }

	/** Buffer that stores the response content */

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    	String error =null;
    	TaskRequest taskRequest  = null;
    	if(buf.length()==0)
    	{
    		error = "receive an http request but without content,directly response!";
        	logger.warn(error);
    	}else
    	{
        	taskRequest = new Gson().fromJson(buf.toString(), TaskRequest.class);
        	if(taskRequest == null)
        	{
        		error = "receive an http request but not a TaskRequest !"+buf.toString();
            	logger.warn(error);
        	}
    	}
    	
    	if(taskRequest == null)
    	{
            FullHttpResponse response = HttpUitls.createHttpResponse(HttpResponseStatus.BAD_REQUEST,error); 
            ctx.writeAndFlush(response);
    	}
    	else
    	{
    		logger.info("forward requect to workstation,id "+taskRequest.getId());
        	try {
    			httpBackendMgr.forwardRequect(ctx,taskRequest);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
        	//TODO  解析Request，根据请求服务分类处理
        }
		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;
			ByteBuf content = httpContent.content();
			if (content.isReadable()) {
				buf.append(content.toString(CharsetUtil.UTF_8));
			}
		}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.warn("HttpServerHandler  exceptionCaught ",cause);
        ctx.close();
    }
    
    
}
