package neu.lab.conflict;

import neu.lab.conflict.writer.RiskWriter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "riskLevel", defaultPhase = LifecyclePhase.VALIDATE)
public class RiskLevelMojo extends ConflictMojo {

    @Override
    public void run() {
        new RiskWriter().printRiskLevel();
    }
}
