package neu.lab.conflict.soot;

import neu.lab.conflict.graph.IGraph;
import soot.SceneTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class CallGraphTF extends SceneTransformer {
    protected String entryClass;
    protected Set<String> riskMethods;
    protected IGraph graph;

    public CallGraphTF(String entryClass, Set<String> riskMethods) {
        super();
        this.entryClass = entryClass;
        this.riskMethods = riskMethods;
    }

    protected  void internalTransform(String arg0, Map<String, String> arg1) {
        Map<String, String> cgMap = new HashMap<>();
        cgMap.put("enabled", "true");
        cgMap.put("apponly", "true");
        cgMap.put("all-reachable", "true");

        initMthd2branch();
        CHATransformer.v().transform("wjtp", cgMap);
        formGraph();
    }

    protected abstract void initMthd2branch();

    protected abstract void formGraph();

    protected boolean isHostClass(String clsName) {
        return entryClass.equals(clsName);
    }

    public IGraph getGraph() {
        return graph;
    }
}
