package neu.lab.conflict.soot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neu.lab.conflict.graph.IGraph;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.ClassVO;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.Transform;

import static soot.SootClass.HIERARCHY;

public class JarAna extends SootAna {
	public static long runtime = 0;
	private static JarAna instance = new JarAna();

	private JarAna() {

	}

	public IGraph getGraph(String[] jarFilePaths, CallGraphTF transformer) {
		IGraph graph = null;
		try {
			PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", transformer));
			Scene.v().addBasicClass("com.google.common.base.Supplier", HIERARCHY);
			soot.Main.main(getArgs(jarFilePaths).toArray(new String[0]));
			graph = transformer.getGraph();
		} catch (Exception e) {
			System.out.println("cg error : " + e);
		}
		soot.G.reset();
		return graph;
	}

	public static JarAna i() {
		if (instance == null) {
			instance = new JarAna();
		}
		return instance;
	}

	/**
	 * 解析jar包
	 * @param jarFilePath jar包文件的路径
	 * @return
	 */
	public Map<String, ClassVO> deconstruct(List<String> jarFilePath) {
//		MavenUtil.i().getLog().info("use soot to deconstruct " + jarFilePath);

		long startTime = System.currentTimeMillis();

		List<String> args = getArgs(jarFilePath.toArray(new String[0]));	//执行命令
		if (args.size() == 0) {
			return new HashMap<String, ClassVO>();
		} else {
			DsTransformer transformer = new DsTransformer(jarFilePath);
			PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", transformer));
//			SootUtil.modifyLogOut();
			soot.Main.main(args.toArray(new String[0]));
			Map<String, ClassVO> clses = transformer.getClsTb();
			soot.G.reset();

			runtime = runtime + (System.currentTimeMillis() - startTime) / 1000;
			return clses;
		}
	}

	protected void addCgArgs(List<String> argsList) {
		argsList.addAll(Arrays.asList(new String[] { "-p", "cg", "off", }));
	}

}

class DsTransformer extends SceneTransformer {
	private Map<String, ClassVO> clsTb;
	private List<String> jarPaths;

	public DsTransformer(List<String> jarPaths) {
		this.jarPaths = jarPaths;
	}

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		clsTb = SootUtil.getClassTb(this.jarPaths);
	}

	public Map<String, ClassVO> getClsTb() {
		return clsTb;
	}

}
