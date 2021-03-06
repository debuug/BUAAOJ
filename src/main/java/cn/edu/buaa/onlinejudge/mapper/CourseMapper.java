package cn.edu.buaa.onlinejudge.mapper;

import cn.edu.buaa.onlinejudge.model.Course;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMapper {
    /**
     * 获取所有的课程
     * @return Course对象列表
     */
    List<Course> getAllCourses();

    /**
     * 根据课程名称模糊查询课程，前缀精确匹配模式
     * @param courseName - 课程名称
     * @return Course对象列表
     */
    List<Course> fuzzyQueryCoursesByCourseName(String courseName);

    /**
     * 获取教师的所有开课课程
     * @param teacherId - 教师ID
     * @return Course对象列表
     */
    List<Course> getAllCoursesByTeacherId(long teacherId);

    /**
     * 根据课程名称模糊查询教师的所有课程
     * @param teacherId - 教师ID
     * @param courseName - 课程名称
     * @return - Course对象列表
     */
    List<Course> fuzzyQueryCoursesOfTeacherByCourseName(@Param("teacherId") long teacherId,
                                                        @Param("courseName") String courseName);

    /**
     * 根据课程ID获取课程对象
     * @param courseId - 课程ID
     * @return Course对象
     */
    Course getCourseById(int courseId);

    /**
     * 插入课程对象
     * @param course - 课程对象
     */
    void insertCourse(Course course);

    /**
     * 更新课程信息
     * @param course - 课程对象
     */
    void updateCourse(Course course);

    /**
     * 删除课程
     * 由于课程数据表、竞赛数据表、题目数据表、题目输入输出样例数据表之间的外键约束关系设置了`ON DELETE CASCADE ON UPDATE CASCADE`，
     * 因此删除课程会自动删除课程中所有的竞赛，以及竞赛中所有的题目和题目的所有输入输出样例
     * @param courseId
     */
    void deleteCourse(int courseId);
}
