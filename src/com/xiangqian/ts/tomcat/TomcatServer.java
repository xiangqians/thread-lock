package com.xiangqian.ts.tomcat;

import com.xiangqian.ts.conf.ConfFactory;
import com.xiangqian.ts.conf.TomcatConf;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.Servlet;

/**
 * Tomcat Server
 *
 * @author xiangqian
 * @date 15:04 2019/12/22
 */
@Slf4j
public class TomcatServer {

    private Tomcat tomcat;
    private Context context;

    private TomcatConf tomcatConf;

    public TomcatServer() {
        tomcatConf = ConfFactory.get(TomcatConf.class);

        // Tomcat
        tomcat = new Tomcat();
        tomcat.setHostname(tomcatConf.getHostName());
        tomcat.getHost().setAutoDeploy(false);
        tomcat.setBaseDir(tomcatConf.getBaseDir());
        tomcat.setPort(tomcatConf.getServerPort());

        log.info("Tomcat工作目录: " + tomcat.getServer().getCatalinaBase().getAbsolutePath());

        // Connector
        Connector connector = new Connector();
        connector.setPort(tomcatConf.getConnectorPort());
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);

        // Context，创建应用上下文
        context = tomcat.addWebapp(tomcatConf.getContextPath(), tomcatConf.getDocDir());
        context.setParentClassLoader(this.getClass().getClassLoader());
        context.setUseRelativeRedirects(false);
    }

    /**
     * 注册Servlet
     *
     * @param servletName Servlet name
     * @param servlet     Servlet instance
     * @param pattern     映射路径
     */
    public void registry(String servletName, Servlet servlet, String pattern) {
        tomcat.addServlet(tomcatConf.getContextPath(), servletName, servlet);
        context.addServletMappingDecoded(pattern, servletName);
        log.info("registry servlet! http://localhost:" + tomcatConf.getConnectorPort() + tomcatConf.getContextPath() + pattern);
    }

    public void startup() throws Exception {
        tomcat.start();
        log.info("Tomcat服务器启动成功! http://localhost:" + tomcatConf.getConnectorPort() + tomcatConf.getContextPath());
        tomcat.getServer().await();
    }

}
