package neu.lab.conflict.risk.jar;

import gumtree.spoon.diff.operations.Operation;
import neu.lab.conflict.CountProjectMojo;
import neu.lab.conflict.container.ArtifactNodes;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.container.NewNodeAdapters;
import neu.lab.conflict.container.NodeAdapters;
import neu.lab.conflict.graph.*;
import neu.lab.conflict.soot.JarAna;
import neu.lab.conflict.soot.MethodPathCGTF;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.MySortedMap;
import neu.lab.conflict.util.PomOperation;
import neu.lab.conflict.vo.DepJar;
import neu.lab.conflict.vo.DependencyInfo;
import neu.lab.conflict.vo.ExcelDataVO;
import neu.lab.conflict.vo.NodeAdapter;
import neu.lab.conflict.writer.ExcelWriter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.poi.ss.usermodel.Workbook;
import org.dom4j.Element;

import java.io.*;
import java.util.*;

public class PrintCallGraph {
    private static String groupId;
    private static String artifactId;
    private static String originalVersion;
    private static String changeVersion;
    private static String riskMethod;
    private static String entryClass;
    private static Map<String, Integer> riskMethods;
    private static Map<String, Integer> pathMethods;
    private Set<String> dependencyInPom;
    private String riskAPI;
    private int diffNum = 0;
    private int pathlen = 100;

    public PrintCallGraph() {
//        getDepInfo();
        groupId = Conf.testGroupId;
        artifactId = Conf.testArtifactId;
        changeVersion = Conf.changeVersion;
        riskMethod = Conf.testMethod;
        entryClass = Conf.testClass;
        originalVersion = getUsedVersion(groupId, artifactId);
        riskMethods = new HashMap<String, Integer>();
        pathMethods = new HashMap<String, Integer>();
        dependencyInPom = new HashSet<String>();
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

    public static void findSemiMethods() {
        long startTime = System.currentTimeMillis();
//        MavenUtil.i().setMojo(new CountProjectMojo());
        List<String> JarFilePath = new ArrayList<>();
        JarFilePath.add(getDepJarPath(groupId, artifactId, originalVersion));
//        JarFilePath.add(getRepositoryJarPath(groupId, artifactId, originalVersion));
        DepJar usedDepJar = new DepJar(groupId, artifactId, originalVersion, "", JarFilePath);
        List<String> conflictJarFilePath = new ArrayList<>();
        conflictJarFilePath.add(getDepJarPath(groupId, artifactId, changeVersion));
//        conflictJarFilePath.add(getRepositoryJarPath(groupId, artifactId, changeVersion));
        DepJar conflictDepJar = new DepJar(groupId, artifactId, changeVersion, "", conflictJarFilePath);
        DepJarJRisk depJarJRisk = new DepJarJRisk(conflictDepJar, usedDepJar);
        Map<String, List<Operation>> riskMethodDiffsMap = depJarJRisk.getAllSemantemeMethodForDifferences();
        MavenUtil.i().getLog().info("Risk method size : " + riskMethodDiffsMap.keySet().size());
        for (String method : riskMethodDiffsMap.keySet()) {
            riskMethods.put(method, riskMethodDiffsMap.get(method).size());
        }
        MavenUtil.i().getLog().info("Map riskMethods size : " + riskMethods.keySet().size());
        long endTime = System.currentTimeMillis();
        MavenUtil.i().getLog().info("Run Time : " + (endTime - startTime) + "ms");
    }

    public void printRiskCallGraph() {
        findSemiMethods();
//        PomOperation.i().mvnTest();
        DependencyInfo dependencyInfo = new DependencyInfo(groupId, artifactId, changeVersion);
        generateTestClassPath(dependencyInfo);
        String classPaths = NodeAdapters.i().getNodeClassPath(groupId, artifactId, changeVersion);
        MavenUtil.i().getLog().info("ClassPath : " + classPaths);
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
        printPath(dis2records, riskMethod);
        if (dis2records.size() > 0) {
            for (Record4path record : dis2records.flat()) {
                if (record.getPathStr().split("\\n")[0].contains(riskMethod)) {
                    String endMethod = getEndMethod(record.getPathStr());
                    if (riskMethods.containsKey(endMethod)) {
                        Integer pathlen = pathMethods.get(endMethod);
                        if (pathlen != null) {
                            if (record.getPathlen() < pathlen) {
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

    public boolean findRiskAPI() {
        printRiskCallGraph();
        for (String pathMethod : pathMethods.keySet()) {
            int tempNum = riskMethods.get(pathMethod);
            int tempLen = pathMethods.get(pathMethod);
            if (tempNum >= diffNum) {
                if (tempLen < pathlen) {
                    diffNum = tempNum;
                    pathlen = tempLen;
                    riskAPI = pathMethod;
                }
                if ((tempLen - pathlen) < (tempNum - diffNum)) {
                    diffNum = tempNum;
                    pathlen = tempLen;
                    riskAPI = pathMethod;
                }
            }
        }
        if (riskAPI == null) {
            return false;
        } else {
            return true;
        }
    }

    public static String getDepJarPath(String groupId, String artifactId, String version) {
        NodeAdapter node = NodeAdapters.i().getNode(groupId, artifactId);
        Artifact artifact = MavenUtil.i().getArtifact(groupId, artifactId, version, node.getType(), node.getClassifier(), node.getScope());
        try {
            MavenUtil.i().resolve(artifact);
        } catch (ArtifactNotFoundException e) {
            e.printStackTrace();
        } catch (ArtifactResolutionException e) {
            e.printStackTrace();
        }
        return artifact.getFile().getAbsolutePath();
    }

    public void writeToTextFile() {
        try {
            PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(new File(Conf.outDir + "Result.txt"), true)));
            printer.println("Project Path : " + MavenUtil.i().getBaseDir().getAbsolutePath());
            printer.println("Project Info : " + MavenUtil.i().getProjectInfo());
            printer.println("Change artifact" + groupId + ":" + artifactId);
            printer.println("Original version : " + originalVersion);
            printer.println("Change version : " + changeVersion);
            printer.println("Risk Method : <" + entryClass + "." + riskMethod + ">");
            printer.println("Risk API : " + riskAPI);
            printer.println("Difference Number : " + riskMethods.get(riskAPI));
            printer.println("Path Length : " + pathMethods.get(riskAPI));
            printer.println("==============================================================================");
            printer.println();
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToExcelFile() {
        String projectName = MavenUtil.i().getName();
        int star = 0;
        int depNum = NewNodeAdapters.i().getContainer().size();
        int avgNum = getAvgNum();
        int testCase = 0;
        String testAPI = entryClass + "." + riskMethod;
        String riskMethod = riskAPI.replace("<", "").replace(">", "");
        ExcelDataVO data = new ExcelDataVO(projectName, star, depNum, avgNum, testCase, groupId, artifactId, changeVersion, originalVersion, testAPI, riskMethod);
        String filePath = Conf.outDir + "GrandTruth.xlsx";
        File file = new File(filePath);
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                Workbook workbook = ExcelWriter.getWorkBook(inputStream);
                FileOutputStream outputStream = new FileOutputStream(file);
                ExcelWriter.insertData(data, workbook);
                workbook.write(outputStream);
                outputStream.flush();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
                Workbook workbook = ExcelWriter.exportData(data);
                FileOutputStream fileOut = new FileOutputStream((filePath));
                workbook.write(fileOut);
                fileOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getAvgNum() {
        int size = NewNodeAdapters.i().getContainer().size();
        int count = 0;
        for (ArtifactNodes artifactNodes : NewNodeAdapters.i().getContainer()) {
            count += artifactNodes.getArtifactVersions().size();
        }
        return count/size;
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

//    public static String getRepositoryJarPath(String groupId, String artifactId, String version) {
//        return MavenUtil.i().getMvnRep() +
//                groupId.replace(".", "/") + File.separator +
//                artifactId.replace(".", "/") + File.separator +
//                version + File.separator +
//                artifactId + "-" + version + ".jar";
//    }

    public void deleteCommonMethods() {
        File file = new File(Conf.outDir + "commonMethods.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public void generateTestClassPath(DependencyInfo dependencyInfo) {
        readPom();
        boolean backupPom = PomOperation.i().backupPom();
        if (backupPom) {
            if (hasInCurrentPom(dependencyInfo)) {
                PomOperation.i().updateDependencyVersion(dependencyInfo);
                MavenUtil.i().getLog().info("success update dependency version for " + dependencyInfo.getName());
            } else {
                PomOperation.i().addDependency(dependencyInfo);
                MavenUtil.i().getLog().info("success add dependency for " + dependencyInfo.getName());
            }
//            PomOperation.i().mvnPackage();
//            PomOperation.i().mvnTest();
        }
    }

    public void readPom() {
        List<Element> dependencyList = PomOperation.i().readPomDependencies();
        for (Element element : dependencyList) {
//            DependencyInfo dependencyInfo = new DependencyInfo(element.element("groupId").getText(), element.element("artifactId").getText());
            dependencyInPom.add(element.element("groupId").getText() + ":" + element.element("artifactId").getText());
//            dependencyInfoList.add(dependencyInfo);
        }
    }

    private boolean hasInCurrentPom(DependencyInfo dependencyInfo) {
        return dependencyInPom.contains(dependencyInfo.getGroupId() + ":" + dependencyInfo.getArtifactId());
    }
}
