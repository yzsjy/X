package neu.lab.conflict.risk.jar;

import gumtree.spoon.diff.operations.Operation;
import neu.lab.conflict.CountProjectMojo;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.container.NodeAdapters;
import neu.lab.conflict.graph.*;
import neu.lab.conflict.soot.JarAna;
import neu.lab.conflict.soot.MethodPathCGTF;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.MySortedMap;
import neu.lab.conflict.vo.DepJar;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;

import java.io.*;
import java.util.*;

public class PrintCallGraph {

    private static int[][] compValue;
    private String groupId;
    private String artifactId;
    private String originalVersion;
    private String changeVersion;
    private static Map<String, Integer> riskMethods;
    private static Map<String, Integer> pathMethods;

    public PrintCallGraph() {
        getDepInfo();
        originalVersion = getUsedVersion(groupId, artifactId);
        riskMethods = new HashMap<String, Integer>();
        pathMethods = new HashMap<String, Integer>();
    }

    public String getUsedVersion(String groupId, String artifactId) {
        String version = null;
        for (DepJar depJar : DepJars.i().getUsedDepJars()) {
            if (depJar.getGroupId().equals(groupId) && depJar.getArtifactId().equals(artifactId)) {
                version = depJar.getVersion();
            }
        }
        return version;
    }

    public void getDepInfo() {
        try {
            FileInputStream inputStream = new FileInputStream(Conf.textPath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            int i = 1;
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                if (i == 4) {
                    groupId = str;
                }
                if (i == 5) {
                    artifactId = str;
                }
                if (i == 6) {
                    changeVersion = str;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void findSemiMethods(String groupId, String artifactId, String originalVer, String changeVer) throws IOException {
        long startTime = System.currentTimeMillis();
        MavenUtil.i().setMojo(new CountProjectMojo());
        List<String> JarFilePath = new ArrayList<>();
        JarFilePath.add(getRepositoryJarPath(groupId, artifactId, originalVer));
        DepJar usedDepJar = new DepJar(groupId, artifactId, originalVer, "", JarFilePath);
        List<String> conflictJarFilePath = new ArrayList<>();
        conflictJarFilePath.add(getRepositoryJarPath(groupId, artifactId, changeVer));
        DepJar conflictDepJar = new DepJar(groupId, artifactId, changeVer, "", conflictJarFilePath);
        DepJarJRisk depJarJRisk = new DepJarJRisk(conflictDepJar, usedDepJar);
        Map<String, List<Operation>> riskMethodDiffsMap = depJarJRisk.getAllSemantemeMethodForDifferences();
        MavenUtil.i().getLog().info("Risk method size : " + riskMethodDiffsMap.keySet().size());
        for (String method : riskMethodDiffsMap.keySet()) {
            riskMethods.put(method, riskMethodDiffsMap.get(method).size());
        }
        long endTime = System.currentTimeMillis();
        MavenUtil.i().getLog().info("Run Time : " + (endTime - startTime) + "ms");
    }

    public void printRiskCallGraph(String riskMethod, String entryClass) throws IOException {
        findSemiMethods(groupId, artifactId, originalVersion, changeVersion);
        String classPaths = NodeAdapters.i().getNodeClassPath(groupId, artifactId, changeVersion);
        String[] classPath = classPaths.split(":");
        MethodPathCGTF methodPathCGTF = new MethodPathCGTF(entryClass, riskMethods.keySet());
        Graph4path graph4path = (Graph4path) JarAna.i().getGraph(classPath, methodPathCGTF);
        Set<String> hostNds = graph4path.getHostNodes();
        Map<String, IBook> books = new Dog(graph4path).findRlt(hostNds, 100, Dog.Strategy.NOT_RESET_BOOK);
        MySortedMap<Integer, Record4path> dis2records = new MySortedMap<Integer, Record4path>();

        for (String topMthd : books.keySet()) {
            if (hostNds.contains(topMthd)) {
                Book4path book = (Book4path) (books.get(topMthd));
                for (IRecord iRecord : book.getRecords()) {
                    Record4path record = (Record4path) iRecord;
                    dis2records.add(record.getPathlen(), record);
                }
            }
        }
        if (dis2records.size() > 0) {
            for (Record4path record : dis2records.flat()) {
                if (record.getPathStr().split("\\n")[0].contains(riskMethod)) {
                    String endMethod = getEndMethod(record.getPathStr());
                    if (riskMethods.containsKey(endMethod)) {
                        if (pathMethods.containsKey(endMethod)) {
                            if (record.getPathlen() < pathMethods.get(endMethod)) {
                                pathMethods.put(endMethod, record.getPathlen());
                            }
                        } else {
                            pathMethods.put(endMethod, record.getPathlen());
                        }
                    }
                }
            }
        }
    }

    public void printPath(MySortedMap<Integer, Record4path> dis2records, String riskMethod) {
        try {

            if (dis2records.size() > 0) {
                String fileName = MavenUtil.i().getProjectGroupId() + ":" + MavenUtil.i().getProjectArtifactId() + ":" + MavenUtil.i().getProjectVersion();
                PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(Conf.outDir + fileName.replace('.', '_').replace(':', '_') + "_test.txt")));
                for (Record4path record : dis2records.flat()) {
                    if (record.getPathStr().split("\\n")[0].contains(riskMethod)) {
                        printer.println("pathLen:" + record.getPathlen() + "\n" + addJarPath(record.getPathStr()));
                    }
                }
                printer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String addJarPath(String mthdCallPath) {
        StringBuilder sb = new StringBuilder();
        String[] mthds = mthdCallPath.split("\\n");
        for (int i = 0; i < mthds.length - 1; i++) {
            // last method is risk method,don't need calculate.
            String mthd = mthds[i];
            sb.append(mthd + "\n");
        }
        sb.append(mthds[mthds.length - 1]);
        return sb.toString();
    }

    public String getEndMethod(String mthdCallPath) {
        String[] mthds = mthdCallPath.split("\\n");
        return mthds[mthds.length - 1];
    }

    public static String getRepositoryJarPath(String groupId, String artifactId, String version) {
        return "/Users/yzsjy/Maven/Respository/" +
                groupId.replace(".", "/") + File.separator +
                artifactId.replace(".", "/") + File.separator +
                version + File.separator +
                artifactId + "-" + version + ".jar";
    }

    public void deleteCommonMethods() {
        File file = new File(Conf.outDir + "commonMethods.txt");
        if (file.exists()) {
            file.delete();
        }
    }

//    public static String getRepositoryJarPath(String groupId, String artifactId, String version) {
//        return MavenUtil.i().getMvnRep() +
//                groupId.replace(".", "/") + File.separator +
//                artifactId.replace(".", "/") + File.separator +
//                version + File.separator +
//                artifactId + "-" + version + ".jar";
//    }

}
