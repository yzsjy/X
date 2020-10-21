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
    private String conflictInfo;
    private String conflictVersions;
    private String filePath;

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

    public ExcelDataVO(String projectName, int stars, int depNum, int avgNum, String groupId, String artifactId, String changeVersion, String originalVersion, String conflictInfo, String conflictVersions, String filePath) {
        this.projectName = projectName;
        this.stars = stars;
        this.depNum = depNum;
        this.avgNum = avgNum;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.changeVersion = changeVersion;
        this.originalVersion = originalVersion;
        this.conflictInfo = conflictInfo;
        this.conflictVersions = conflictVersions;
        this.filePath = filePath;
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

    public String getFilePath() {
        return filePath;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getConflictInfo() {
        return conflictInfo;
    }

    public String getConflictVersions() {
        return conflictVersions;
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
