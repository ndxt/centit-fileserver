package com.centit.framework.dubbo.config;

import com.caucho.hessian.client.HessianProxyFactory;
import org.springframework.context.annotation.Configuration;

/**
 * 为解决Hessian Premature EOF 异常
 */
//Hessian的HessianFactory的isOverloadEnabled属性默认为false。这个参数如果为false，Hessian调用的时候获取接口仅根据方法名；
//反之，Hessian调用时决定调用哪个方法是通过方法名和参数类型一起决定。

@Configuration
public class FileServerHessianProxyFactoryBean extends HessianProxyFactory {
    @Override
    public boolean isOverloadEnabled() {
        return true;
    }

    @Override
    public void setChunkedPost(boolean isChunked) {
        super.setChunkedPost(false);
    }
}

