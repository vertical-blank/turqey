import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

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

        WebAppContext context = new WebAppContext();

        ProtectionDomain domain = JettyLauncher.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        Configuration[] configurations = {
            new AnnotationConfiguration(),
            new WebInfConfiguration(),
            new WebXmlConfiguration(),
            new MetaInfConfiguration(),
            new FragmentConfiguration(),
            new EnvConfiguration(),
            new PlusConfiguration(),
            new JettyWebXmlConfiguration()
        };
        context.setConfigurations(configurations);

        File tmpDir = new File(new File(System.getProperty("user.home"), ".turqey"), "tmp");
        if(tmpDir.exists()){
          for(File file: tmpDir.listFiles()){
              if(file.isFile()){
                  file.delete();
              } else if(file.isDirectory()){
                  deleteDirectory(file);
              }
          }
          tmpDir.delete();
        }
        tmpDir.mkdirs();
        context.setTempDirectory(tmpDir);
        context.setContextPath(contextPath);
        context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        context.setWar(location.toExternalForm());

        System.out.println(location.toExternalForm());

        if (forceHttps) {
            context.setInitParameter("org.scalatra.ForceHttps", "true");
        }
        // context.setResourceBase("src/main/webapp");

        Server server = new Server(port);
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
