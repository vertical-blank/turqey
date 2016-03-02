import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import org.scalatra.servlet.ScalatraListener;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;

public class JettyLauncher {

    public static final String envKey = "TURQEY_HOME";
    
    public static void main(String[] args) throws Exception {
        String host = null;
        int port = 8080;
        String contextPath = "/";
        boolean forceHttps = false;

        WebAppContext context = new WebAppContext();

        ProtectionDomain domain = JettyLauncher.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();
        String externalLocation = location.toExternalForm();

        String envHome = System.getenv(envKey);
        File tmpDir = new File(new File(envHome != null ? envHome : (System.getProperty("user.home") + "/.turqey") ), "tmp");
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
        context.setDescriptor(externalLocation + "/WEB-INF/web.xml");
        System.out.println (externalLocation);
        if (externalLocation.endsWith(".war")){
          context.setWar(externalLocation);
        } else {
          context.setResourceBase("src/main/webapp");
        }

        if (forceHttps) {
            context.setInitParameter("org.scalatra.ForceHttps", "true");
        }

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
