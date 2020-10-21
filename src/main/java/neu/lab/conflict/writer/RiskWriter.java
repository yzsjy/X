package neu.lab.conflict.writer;

import neu.lab.conflict.container.ArtifactNodes;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.container.NewNodeAdapters;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.PomOperation;
import neu.lab.conflict.vo.DepJar;
import neu.lab.conflict.vo.DependencyInfo;
import neu.lab.conflict.vo.ExcelDataVO;
import org.apache.poi.ss.usermodel.Workbook;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RiskWriter {

    private static String groupId;
    private static String artifactId;
    private static String originalVersion;
    private static String changeVersion;
    private Set<String> dependencyInPom;
    private static String conflictInfo = null;
    private static String conflictVersions = null;

    public RiskWriter() {
        groupId = Conf.testGroupId;
        artifactId = Conf.testArtifactId;
        changeVersion = Conf.changeVersion;
        originalVersion = getUsedVersion(groupId, artifactId);
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

    public void printRiskLevel() {
        PomOperation.i().mvnPackage();
        DependencyInfo dependencyInfo = new DependencyInfo(groupId, artifactId, changeVersion);
        changeDepVersion(dependencyInfo);
        String resultPath = Conf.outDir + "RiskLevel/";
        String riskPath = Conf.outDir + "Risk/";
        PomOperation.i().mvnRiskLevel(resultPath);
        String fileName = MavenUtil.i().getProjectGroupId() + ":" + MavenUtil.i().getProjectArtifactId() + ":" + MavenUtil.i().getProjectVersion();
        File srcFile = new File(resultPath + fileName.replace('.', '_').replace(':', '_') + ".xml");
        File tgtFile = new File(riskPath + dependencyInfo.getLogFileName() + fileName.replace('.', '_').replace(':', '_') + ".xml");
        if (check(srcFile)) {
            PomOperation.i().backupFile(srcFile, tgtFile);
            writeToExcelFile(tgtFile);
        } else {
            PomOperation.i().deleteFile(srcFile);
        }

    }

    public void writeToExcelFile(File tgtFile) {
        String projectName = MavenUtil.i().getName();
        int star = 0;
        int depNum = NewNodeAdapters.i().getContainer().size();
        int avgNum = getAvgNum();
        ExcelDataVO data = new ExcelDataVO(projectName, star, depNum, avgNum, groupId, artifactId, changeVersion, originalVersion, conflictInfo, conflictVersions, tgtFile.getAbsolutePath());
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

    public boolean check(File file) {
        boolean isTrue = false;
        if (!file.exists() || file.length() == 0) {
            isTrue = false;
        } else {
            try {
                SAXReader saxReader = new SAXReader();
                saxReader.setEncoding("GB2312");
                Document document = saxReader.read(file);

                Element rootElement = document.getRootElement();
                if (rootElement != null) {
                    List<Element> conflicts = rootElement.elements("conflicts");
                    for (Element conflict : conflicts) {
                        Element conflictJar = conflict.element("conflictJar");

                        List<Attribute> listAttr = conflictJar.attributes();
                        String conflictDep = null;
                        String conflictVersion = null;
                        for (Attribute attribute : listAttr) {
                            if (attribute.getName().equals("groupId-artifactId")) {
                                conflictDep = attribute.getValue();
                            }
                            if (attribute.getName().equals("versions")) {
                                conflictVersion = attribute.getValue();
                            }
                            if (attribute.getName().equals("riskLevel")) {
                                if (attribute.getValue().equals("3") || attribute.getValue().equals("4")) {
                                    isTrue = true;
                                    conflictInfo = conflictDep;
                                    conflictVersions = conflictVersion;
                                }
                            }
                        }
                    }
                }
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        return isTrue;
    }

    public void changeDepVersion(DependencyInfo dependencyInfo) {
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

    private int getAvgNum() {
        int size = NewNodeAdapters.i().getContainer().size();
        int count = 0;
        for (ArtifactNodes artifactNodes : NewNodeAdapters.i().getContainer()) {
            count += artifactNodes.getArtifactVersions().size();
        }
        return count/size;
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
