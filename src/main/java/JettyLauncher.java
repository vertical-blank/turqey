import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.scalatra.servlet.ScalatraListener;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;

public class JettyLauncher {
    public static void main(String[] args) throws Exception {
        String host = null;
        int port = 8080;
        String contextPath = "/";
        boolean forceHttps = false;

        /*
        for(String arg: args) {
            if(arg.startsWith("--") && arg.contains("=")) {
                String[] dim = arg.split("=");
                if(dim.length >= 2) {
                    if(dim[0].equals("--host")) {
                        host = dim[1];
                    } else if(dim[0].equals("--port")) {
                        port = Integer.parseInt(dim[1]);
                    } else if(dim[0].equals("--prefix")) {
                        contextPath = dim[1];
                    } else if(dim[0].equals("--gitbucket.home")){
                        System.setProperty("gitbucket.home", dim[1]);
                    }
                }
            }
        }
        */

        Server server = new Server(port);

        WebAppContext context = new WebAppContext();

        ProtectionDomain domain = JettyLauncher.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        context.setContextPath(contextPath);
        context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        context.setServer(server);
        context.setWar(location.toExternalForm());
        if (forceHttps) {
            context.setInitParameter("org.scalatra.ForceHttps", "true");
        }
        context.setResourceBase("src/main/webapp");

        server.setHandler(context);

        server.start();
        server.join();
    }

    private static void deleteDirectory(File dir){
        for(File file: dir.listFiles()){
            if(file.isFile()){
                file.delete();
            } else if(file.isDirectory()){
                deleteDirectory(file);
            }
        }
        dir.delete();
    }
}
