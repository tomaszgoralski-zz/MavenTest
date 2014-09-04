import javax.servlet.*;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

@WebListener()
public class DynamicAddListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext sc = event.getServletContext();
        final ServletRegistration.Dynamic dn =
                sc.addServlet("Jersey", com.sun.jersey.spi.container.servlet.ServletContainer.class);
        dn.setLoadOnStartup(1);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}