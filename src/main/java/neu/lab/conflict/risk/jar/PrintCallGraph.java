package neu.lab.conflict.risk.jar;

import neu.lab.conflict.graph.*;
import neu.lab.conflict.soot.JarAna;
import neu.lab.conflict.soot.MethodPathCGTF;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MySortedMap;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrintCallGraph {
    public void printRiskCallGraph(String riskMethod, String entryClass, String classPaths) throws IOException {
        String[] classPath = classPaths.split(":");
        Set<String> methods = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Conf.outDir + "commonMethods.txt"));
            String line = reader.readLine();
            while (line != null) {
                if (!line.equals("")) {
                    methods.add(line.split("@@")[0]);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MethodPathCGTF methodPathCGTF = new MethodPathCGTF(entryClass, methods);
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
            PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(Conf.outDir + "test.txt")));
            for (Record4path record : dis2records.flat()) {
                if (record.getPathStr().split("\\n")[0].contains(riskMethod)) {
                    printer.println("pathLen:" + record.getPathlen() + "\n" + addJarPath(record.getPathStr()));
                }
            }
            printer.close();
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


}
