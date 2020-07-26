package neu.lab.conflict.soot;

import neu.lab.conflict.graph.Graph4path;
import neu.lab.conflict.graph.Node4path;
import neu.lab.conflict.vo.MethodCall;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;

public class MethodPathCGTF extends CallGraphTF {
    public MethodPathCGTF(String entryClass, Set<String> riskMethods) {
        super(entryClass, riskMethods);
    }

    @Override
    protected void formGraph() {
        if (graph == null) {
            System.out.println("start to generate call graph");
            Map<String, Node4path> name2node = new HashMap<>();
            List<MethodCall> mthdRlts = new ArrayList<>();

            CallGraph cg = Scene.v().getCallGraph();
            Iterator<Edge> ite = cg.iterator();
            while (ite.hasNext()) {
                Edge edge = ite.next();
                String srcMthdName = edge.src().getSignature();
                String tgtMthdName = edge.tgt().getSignature();

                String srcClsName = edge.src().getDeclaringClass().getName();
                String tgtClsName = edge.tgt().getDeclaringClass().getName();
                if (edge.src().isJavaLibraryMethod() || edge.tgt().isJavaLibraryMethod()) {

                } else {
                    if (!name2node.containsKey(srcMthdName)) {
                        name2node.put(srcMthdName, new Node4path(srcMthdName, isHostClass(srcClsName), riskMethods.contains(srcMthdName)));
                    }
                    if (!name2node.containsKey(tgtMthdName)) {
                        name2node.put(tgtMthdName, new Node4path(tgtMthdName, isHostClass(tgtClsName), riskMethods.contains(tgtMthdName)));
                    }
                    mthdRlts.add(new MethodCall(srcMthdName, tgtMthdName));
                }
            }
            graph = new Graph4path(name2node, mthdRlts);
            System.out.println("end graph");
        }
    }

    @Override
    protected void initMthd2branch() {

    }
}
