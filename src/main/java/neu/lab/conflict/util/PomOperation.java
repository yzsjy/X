package neu.lab.conflict.util;

import neu.lab.conflict.vo.DependencyInfo;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PomOperation {
    //    public static String COPY_CONFLICT = "copyConflictDependency.xml";
//    public static String COPY_JUNIT = "copyJunit.xml";
//    public static String COYT_EVOSUITE = "copyEvosuiteRuntime.xml";
    private String POM_PATH = MavenUtil.i().getProjectPom();
    public String POM_PATH_COPY = MavenUtil.i().getBaseDir().getAbsolutePath() + "/pom-copy.xml";

    private static PomOperation instance;

    public static PomOperation i() {
        if (instance == null) {
            instance = new PomOperation();
        }
        return instance;
    }

    private PomOperation() {
    }

    /**
     * add dependency to new empty
     *
     * @param dependencyInfo
     */
    public void addDependency(DependencyInfo dependencyInfo) {
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(POM_PATH);
            Element rootElement = document.getRootElement();
            Element dependencies = rootElement.element("dependencies");
            if (dependencies == null) {
                dependencies = rootElement.addElement("dependencies");
            }
            Element dependency = dependencies.addElement("dependency");
            dependencyInfo.addDependencyElement(dependency);
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            outputFormat.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(new FileWriter(POM_PATH_COPY), outputFormat);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            MavenUtil.i().getLog().error(e.getMessage());
        }
    }

    public void updateDependencyVersion(DependencyInfo dependencyInfo) {
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(POM_PATH);
            Element rootElement = document.getRootElement();
            Element dependencies = rootElement.element("dependencies");
            Iterator dependencyIterator = dependencies.elementIterator("dependency");
            while (dependencyIterator.hasNext()) {
                Element dependency = (Element) dependencyIterator.next();
                if (dependency.element("groupId").getText().equals(dependencyInfo.getGroupId())
                        && dependency.element("artifactId").getText().equals(dependencyInfo.getArtifactId())) {
                    Element version = dependency.element("version");
                    if (version == null) {
                        version = dependency.addElement("version");
                    }
                    version.setText(dependencyInfo.getVersion());
                    break;
                }
            }
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            outputFormat.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(new FileWriter(POM_PATH_COPY), outputFormat);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            MavenUtil.i().getLog().error(e.getMessage());
        }
    }

    public List<Element> readPomDependencies() {
        SAXReader reader = new SAXReader();
        List<Element> dependencyList = new ArrayList<>();

        try {
            Document document = reader.read(POM_PATH);
            Element rootElement = document.getRootElement();
            Element dependencies = rootElement.element("dependencies");
            if (dependencies != null) {
                dependencyList = dependencies.elements("dependency");
//                while (elementIterator.hasNext()) {
//                    dependencyList.add((Element) elementIterator.next());
//                }
            }
//            Element dependency = dependencies.addElement("dependency");
//            dependencyInfo.addDependencyElement(dependency);
//            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
//            outputFormat.setEncoding("UTF-8");
//            XMLWriter writer = new XMLWriter(new FileWriter(xmlFilePath), outputFormat);
//            writer.write(document);
//            writer.close();
        } catch (Exception e) {
            MavenUtil.i().getLog().error(e.getMessage());
        }
        return dependencyList;
    }

    public boolean backupPom() {
        if (new File(POM_PATH_COPY).exists()) {
            new File(POM_PATH_COPY).delete();
        }
        MavenUtil.i().getLog().info("backup pom.xml to pom-copy.xml");
        try {
            Files.copy(new File(POM_PATH).toPath(), new File(POM_PATH_COPY).toPath());
        } catch (IOException e) {
            MavenUtil.i().getLog().error("backup pom.xml error");
            MavenUtil.i().getLog().error(e.getMessage());
            return false;
        }
        MavenUtil.i().getLog().info("success backup");
        return true;
    }

    public boolean backupFile(File srcFile, File tgtFile) {
        if (tgtFile.exists()) {
            tgtFile.delete();
        }
        MavenUtil.i().getLog().info("Backup the file which has risk jar");
        try {
            Files.copy(srcFile.toPath(), tgtFile.toPath());
        } catch (IOException e) {
            MavenUtil.i().getLog().error("Backup failed");
            MavenUtil.i().getLog().error(e.getMessage());
            return false;
        }
        MavenUtil.i().getLog().info("Backup success");
        return true;
    }

    public void backupPomCopy() {
        if (new File(POM_PATH).exists()) {
            new File(POM_PATH).delete();
        }
        MavenUtil.i().getLog().info("backup pom-copy.xml to pom.xml");
        try {
            Files.copy(new File(POM_PATH_COPY).toPath(), new File(POM_PATH).toPath());
        } catch (IOException e) {
            MavenUtil.i().getLog().error("backup pom-copy.xml error");
            MavenUtil.i().getLog().error(e.getMessage());
        }
        MavenUtil.i().getLog().info("success backup");
    }

    public void restorePom() {
        if (new File(POM_PATH).exists()) {
            new File(POM_PATH).delete();
        }
        try {
            Files.copy(new File(POM_PATH_COPY).toPath(), new File(POM_PATH).toPath());
        } catch (IOException e) {
            MavenUtil.i().getLog().error("restore pom.xml error");
            MavenUtil.i().getLog().error(e.getMessage());
        }
        MavenUtil.i().getLog().info("success restore pom.xml");
    }

    public void deletePomCopy() {
        if (new File(POM_PATH_COPY).exists()) {
            new File(POM_PATH_COPY).delete();
        }
        MavenUtil.i().getLog().info("success delete pom-copy.xml");
    }

    public void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
        MavenUtil.i().getLog().info("success delete xml file");
    }

    public boolean mvnTest() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        CommandLine  cmdLine = CommandLine.parse("mvn test -f " + POM_PATH_COPY);
        DefaultExecutor executor = new DefaultExecutor();
        int exitCode = -1;
        try {
            executor.setStreamHandler(streamHandler);
            exitCode = executor.execute(cmdLine);
        } catch (IOException e) {
            MavenUtil.i().getLog().error("This project execute error : " + e.getMessage());
        }
        return exitCode == 0;
    }

    public boolean mvnPackage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        CommandLine  cmdLine = CommandLine.parse("mvn package -Dmaven.test.skip=true");
        DefaultExecutor executor = new DefaultExecutor();
        int exitCode = -1;
        try {
            executor.setStreamHandler(streamHandler);
            exitCode = executor.execute(cmdLine);
        } catch (IOException e) {
            MavenUtil.i().getLog().error("This project execute error : " + e.getMessage());
        }
        return exitCode == 0;
    }

    public boolean mvnRiskLevel(String resultPath) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        CommandLine cmdLine = CommandLine.parse("mvn -f=" + POM_PATH_COPY + " -Dmaven.test.skip=true -DuseAllJar=false -DfilterSuper=true neu.lab:riddle:1.0:printRiskLevel -DresultPath=" + resultPath + " -e");
        DefaultExecutor executor = new DefaultExecutor();
        int exitCode = -1;
        try {
            executor.setStreamHandler(streamHandler);
            exitCode = executor.execute(cmdLine);
        } catch (IOException e) {
            MavenUtil.i().getLog().error("Print Risk Level failed : " + e.getMessage());
        }
        return exitCode == 0;
    }
//    /**
//     * copy empty dependency xml to target path
//     *
//     * @return XMLName.xml path
//     */
//    public static String copyPom(String XMLName) {
//        InputStream fileInputStream = ReadXML.class.getResourceAsStream("/" + XMLName);
//        String xmlFileName = System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.SENSOR_DIR + Config.FILE_SEPARATOR + XMLName;
//        byte[] buffer;
//        try {
//            buffer = new byte[fileInputStream.available()];
//            fileInputStream.read(buffer);
//            Files.write(buffer, new File(xmlFileName));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return xmlFileName;
//    }

    /**
     * execute Maven dependency:copy-dependencies
     *
     * @param xmlFileName
     * @param dir         for dependency jar
     */
//    public static void executeMavenCopy(String xmlFileName, String dir) {
////        String mvnCmd = Config.getMaven() + Command.MVN_POM + xmlFileName + Command.MVN_COPY + dir;
//        try {
//            ExecuteCommand.exeCmd(mvnCmd);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
