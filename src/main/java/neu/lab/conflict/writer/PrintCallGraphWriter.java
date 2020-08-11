package neu.lab.conflict.writer;

import neu.lab.conflict.container.NodeAdapters;
import neu.lab.conflict.risk.jar.PrintCallGraph;
import neu.lab.conflict.util.Conf;

import java.io.*;

public class PrintCallGraphWriter {
    public void printRiskAPI() {
        PrintCallGraph printCallGraph = new PrintCallGraph();
        boolean hasAPI = printCallGraph.findRiskAPI();
        if (hasAPI) {
            printCallGraph.writeToExcelFile();
        }
    }

    public void testClassPath() {
        try {
            PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(new File(Conf.outDir + "classpath.txt"), false)));
            printer.println(NodeAdapters.i().getNodeClassPath("com.google.inject", "guice", "2.0-no_aop"));
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
