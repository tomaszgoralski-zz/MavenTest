import java.util.Set;
import java.util.HashSet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// The Application class's name should be unique if multiple
// Applications are to be deployed into the same servlet container.
@ApplicationPath("/resourcesA")
public class RestfulAdage extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(Adages.class);
        return set;
    }
}