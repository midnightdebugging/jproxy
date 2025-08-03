package org.pierce.socks;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.*;
import org.pierce.impl.JproxyDomainQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class JproxyDomainQueryTest {

    private static final Logger log = LoggerFactory.getLogger(JproxyDomainQueryTest.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException, URISyntaxException {
        EventLoopGroup eventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                log.info("eventLoopGroup.shutdownGracefully();");
                eventLoopGroup.shutdownGracefully();
            }
        });

        String[] hostNameArr = new String[]{
                "creative.mnaspm.com", "creative.myavlive.com", "creative.niscprts.com", "creative.xlivrdr.com", "creative.zwhitelabel.com", "creativecdn.com", "csi.gstatic.com", "css.yhdmtu.xyz", "cstat.cdn-apple.com", "cstm.baidu.com", "csycdn.flv.wxqcloud.qq.com", "cube.meituan.com", "cube.weixinbridge.com", "currency.prebid.org", "d.myani.org", "d.qchannel03.cn", "d1--cn-gotcha204-1.bilivideo.com", "d1--cn-gotcha204-3.bilivideo.com", "d1--cn-gotcha204-4.bilivideo.com", "danmu.yhdmjx.com", "das.svc.litv.tv", "dash.cloudflare.com", "data-api.esign.cn", "data.ab.qq.com", "data.bilibili.com", "data.sec.miui.com", "dataflow.biliapi.com", "detectportal.firefox.com", "device-config.pcms.apple.com", "dfpgw.paas.cmbchina.com", "dfzximg02.dftoutiao.com", "diffusedpassionquaking.com", "dig.bdurl.net", "dis.criteo.com", "disp-qryapi.3g.qq.com", "dispatcher.is.autonavi.com"
        };
        EventExecutor executor = ImmediateEventExecutor.INSTANCE;
        for (String hostName : hostNameArr) {
            Promise<List<String>> promise0 = executor.newPromise();
            promise0.addListener(new GenericFutureListener<Future<? super List<String>>>() {
                @Override
                public void operationComplete(Future<? super List<String>> future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("{} == > {}", hostName, future.getNow());
                        return;
                    }
                    log.info("{} fail", hostName);
                }
            });
            JproxyDomainQuery jproxyDomainQuery = new JproxyDomainQuery(eventLoopGroup);
            jproxyDomainQuery.query(hostName, promise0);
        }


/*        try {
            Thread.sleep(1000 * 30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }
}
