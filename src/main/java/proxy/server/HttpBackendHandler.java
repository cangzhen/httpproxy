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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.util.HttpUitls;

public class HttpBackendHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LoggerFactory.getLogger(HttpBackendMgr.class);

	final  FullHttpRequest request;
	final ChannelHandlerContext frontCtx;
    private final StringBuilder buf = new StringBuilder();
	public HttpBackendHandler(ChannelHandlerContext frontCtx, FullHttpRequest request) {
		this.request = request;
		this.frontCtx = frontCtx;
	}
	
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
        	//TODO 
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            buf.append(httpContent.content().toString(CharsetUtil.UTF_8));

            if (msg instanceof LastHttpContent) {
            	//TODO 处理buf
            	String content = buf.toString();
                FullHttpResponse response = HttpUitls.createHttpResponse(HttpResponseStatus.OK,content); 

            	//把应答写会到前端
            	frontCtx.writeAndFlush(response).addListener(new ChannelFutureListener() {

                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                        	String error = "write reponse to frontend failed."+future.cause().getMessage();
                        	logger.warn(error);
                        }
                        frontCtx.close();//前端关闭
                    }
                });
            	ctx.close();//后端关闭
            }
         }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
