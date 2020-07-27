package neu.lab.conflict;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "printCallGraph", defaultPhase = LifecyclePhase.VALIDATE)
public class PrintCallGraphMojo extends ConflictMojo {
    @Override
    public void run() {

    }
}
