package org.pierce;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;
import org.pierce.imp.HttpDownloader;

public class HttpDownloaderTest {
    public static void main(String[] args) throws InterruptedException {
        Downloader downloader = new HttpDownloader();
        Promise<Channel> promise = downloader.download("http://repo.or.cz/gfwlist.git/blob_plain/HEAD:/gfwlist.txt", "/tmp/gfwlist.txt-6", false);
        promise.await().sync();
        System.out.println("xx");
        /*promise.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("下载成功1");
                return;
            }
            System.out.println("下载失败1");
            future.cause().printStackTrace();
        });*/
    }
}
