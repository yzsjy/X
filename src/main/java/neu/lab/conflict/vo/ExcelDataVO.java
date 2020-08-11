package neu.lab.conflict.vo;

public class ExcelDataVO {

    private String projectName;
    private int stars;
    private int depNum;
    private int avgNum;
    private int testCase;
    private String groupId;
    private String artifactId;
    private String changeVersion;
    private String originalVersion;
    private String testAPI;
    private String riskAPI;

    public ExcelDataVO(String projectName, int stars, int depNum, int avgNum, int testCase, String groupId, String artifactId, String changeVersion, String originalVersion, String testAPI, String riskAPI) {
        this.projectName = projectName;
        this.stars = stars;
        this.depNum = depNum;
        this.avgNum = avgNum;
        this.testCase = testCase;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.changeVersion = changeVersion;
        this.originalVersion = originalVersion;
        this.testAPI = testAPI;
        this.riskAPI = riskAPI;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getStars() {
        return stars;
    }

    public int getDepNum() {
        return depNum;
    }

    public int getAvgNum() {
        return avgNum;
    }

    public int getTestCase() {
        return testCase;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getChangeVersion() {
        return changeVersion;
    }

    public String getOriginalVersion() {
        return originalVersion;
    }

    public String getTestAPI() {
        return testAPI;
    }

    public String getRiskAPI() {
        return riskAPI;
    }
}
