package org.pierce;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;

public interface Downloader {

    Promise<Channel> download(String url, String savePath, boolean proxy);

}
