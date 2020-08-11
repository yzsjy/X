package neu.lab.conflict;

import neu.lab.conflict.writer.PrintCallGraphWriter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "printCallGraph", defaultPhase = LifecyclePhase.VALIDATE)
public class PrintCallGraphMojo extends ConflictMojo {
    @Override
    public void run() {
//        new PrintCallGraphWriter().testClassPath();
        new PrintCallGraphWriter().printRiskAPI();
    }
}
