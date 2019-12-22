package com.xiangqian.ts.conf;

import lombok.Data;

/**
 * @author xiangqian
 * @date 15:10 2019/12/22
 */
@ConfValue("tomcat")
@Data
public class TomcatConf implements Conf {

    @ConfValue("host.name")
    private String hostName;

    @ConfValue("server.port")
    private int serverPort;

    @ConfValue("connector.port")
    private int connectorPort;

    @ConfValue("context.path")
    private String contextPath;

    @ConfValue("base.dir")
    private String baseDir;

    @ConfValue("doc.dir")
    private String docDir;

    public TomcatConf() {
        this.hostName = "localhost";
        this.serverPort = 8005;
        this.connectorPort = 8080;
        this.contextPath = "";
        this.baseDir = "F:\\test\\tomcat\\base-dir";;
        this.docDir = "F:\\test\\tomcat\\doc-dir";
    }

}
