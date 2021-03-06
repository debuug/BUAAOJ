package cn.edu.buaa.onlinejudge.model;

public class Course {
    /**
     * 课程ID
     */
    private int courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程的开课教师ID
     */
    private int teacherId;

    /**
     * 课程简介
     */
    private String introduction;

    /**
     * 课程的无参构造方法
     */
    public Course() { }

    public Course(int courseId, String courseName, int teacherId, String introduction) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.introduction = introduction;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", teacherId=" + teacherId +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
