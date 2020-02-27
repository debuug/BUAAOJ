package cn.edu.buaa.onlinejudge.controller;

import cn.edu.buaa.onlinejudge.model.Course;
import cn.edu.buaa.onlinejudge.model.Student;
import cn.edu.buaa.onlinejudge.service.*;
import cn.edu.buaa.onlinejudge.utils.HttpResponseWrapperUtil;
import cn.edu.buaa.onlinejudge.utils.MD5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "课程相关接口")
@RestController
@RequestMapping(value = "BUAAOJ/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @ApiOperation("学生查看所有课程接口")
    @GetMapping(value = "/getAllCourses")
    public HttpResponseWrapperUtil getAllCourses() {
        List<Course> allCourses = courseService.getAllCourses();
        return wrapCourseList2JSON(allCourses);
    }

    @ApiOperation("学生查看课程主页接口")
    @GetMapping(value = "/getCourseHomepage/{studentId}/{courseId}")
    public HttpResponseWrapperUtil getCourseHomepage(@PathVariable("studentId") long studentId,
                                                     @PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ) {
            return new HttpResponseWrapperUtil(null, -1, "failure");
        }
        Map<String,Object> data = new HashMap<>();
        data.put("introduction", course.getIntroduction());
        data.put("downloadable", courseService.isCoursewareExists(courseId) ? 1 : 0);
        int status = courseService.isStudentJoinCourse(studentId,courseId);
        data.put("isStudentAccept", status);
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生申请加入课程接口")
    @GetMapping(value = "/joinCourse/{studentId}/{courseId}")
    public HttpResponseWrapperUtil joinCourse(@PathVariable("studentId") long studentId,
                                                     @PathVariable("courseId") int courseId) {
        Map<String,Object> map = wrapCourseStudentRelationship2Json(studentId,courseId);
        HttpResponseWrapperUtil httpResponse = (HttpResponseWrapperUtil)map.get("httpResponse");
        if( httpResponse != null ){
            return httpResponse;
        }
        if( (int)map.get("status") >= 0 ){
            return new HttpResponseWrapperUtil(null, -1, "请勿重复提交申请");
        }
        courseService.joinCourse(studentId,courseId);
        return new HttpResponseWrapperUtil(null);
    }

    @ApiOperation("学生查看课程成员接口")
    @GetMapping(value = "/getCourseMembers/{studentId}/{courseId}")
    public HttpResponseWrapperUtil getCourseMembers(@PathVariable("studentId") long studentId,
                                              @PathVariable("courseId") int courseId) {
        Map<String,Object> map = wrapCourseStudentRelationship2Json(studentId,courseId);
        HttpResponseWrapperUtil httpResponse = (HttpResponseWrapperUtil)map.get("httpResponse");
        if( httpResponse != null ){
            return httpResponse;
        }
        if( (int)map.get("status") != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "请先加入课程");
        }
        List<Map<String,Object>> data = courseService.getCourseMembers(courseId,1);
        //学生查看课程成员不需要返回学生ID
        for (Map<String, Object> metadata : data) {
            metadata.remove("studentId");
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师查看课程列表接口")
    @GetMapping(value = "/getCourseList/{teacherId}")
    public HttpResponseWrapperUtil getCourseList(@PathVariable("teacherId") long teacherId) {
        List<Course> courses = courseService.getCoursesOfTeacher(teacherId);
        return wrapCourseList2JSON(courses);
    }

    @ApiOperation("教师查看课程信息接口")
    @GetMapping(value = "/getCourseById/{teacherId}/{courseId}")
    public HttpResponseWrapperUtil getCourseById(@PathVariable("teacherId") long teacherId,
                                                 @PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        if( teacherId != course.getTeacherId() ){
            return new HttpResponseWrapperUtil(null, -1, "教师权限不足");
        }
        Map<String,Object> data = new HashMap<>();
        data.put("courseName",course.getCourseName());
        data.put("introduction",course.getIntroduction());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师新建课程接口")
    @PostMapping(value = "/createCourse")
    public HttpResponseWrapperUtil createCourse(@RequestBody Course course) {
        courseService.insertCourse(course);
        Map<String,Object> data = new HashMap<>();
        data.put("courseId", course.getCourseId());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师删除课程接口")
    @GetMapping(value = "/deleteCourse/{teacherId}/{courseId}")
    public HttpResponseWrapperUtil deleteCourse(@PathVariable("teacherId") int teacherId,
                                                @PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        if( course.getTeacherId() != teacherId ){
            return new HttpResponseWrapperUtil(null, -1, "权限不足");
        }
        courseService.deleteCourse(courseId);
        return new HttpResponseWrapperUtil(null);
    }

    @ApiOperation("教师修改课程信息")
    @PostMapping(value = "/updateCourse")
    public HttpResponseWrapperUtil updateCourseIntro(@RequestBody Course course) {
        Course realCourse = courseService.getCourseById(course.getCourseId());
        if( realCourse == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        courseService.updateCourse(course);
        return new HttpResponseWrapperUtil(null);
    }

    @ApiOperation("教师审核课程成员接口")
    @GetMapping(value = "/auditCourseMembers/{teacherId}/{courseId}/{tag}")
    public HttpResponseWrapperUtil auditCourseMembers(@PathVariable("teacherId") long teacherId,
                                                      @PathVariable("courseId") int courseId,
                                                      @PathVariable("tag") int tag) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        if( teacherId != course.getTeacherId() ){
            return new HttpResponseWrapperUtil(null, -1, "教师权限不足");
        }
        if( tag != 0 && tag != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "tag值错误");
        }
        List<Map<String,Object>> data = courseService.getCourseMembers(courseId,tag);
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师修改课程成员角色接口")
    @GetMapping(value = "/changeCourseMemberRole/{teacherId}/{courseId}/{studentId}/{role}")
    public HttpResponseWrapperUtil changeCourseMemberRole(@PathVariable("teacherId") long teacherId,
                                                          @PathVariable("courseId") int courseId,
                                                          @PathVariable("studentId") long studentId,
                                                          @PathVariable("role") int role) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        if( teacherId != course.getTeacherId() ){
            return new HttpResponseWrapperUtil(null, -1, "教师权限不足");
        }
        if( courseService.isStudentJoinCourse(studentId,courseId) != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "学生尚未加入课程");
        }
        if( role != 0 && role != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "role值错误");
        }
        courseService.updateCourseMemberRole(courseId, studentId, role);
        return new HttpResponseWrapperUtil(null);
    }

    @ApiOperation("教师加入或移除学生接口")
    @GetMapping(value = "/auditStudent/{teacherId}/{courseId}/{studentId}/{status}")
    public HttpResponseWrapperUtil auditStudent(@PathVariable("teacherId") long teacherId,
                                                @PathVariable("courseId") int courseId,
                                                @PathVariable("studentId") long studentId,
                                                @PathVariable("status") int status) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        if( teacherId != course.getTeacherId() ){
            return new HttpResponseWrapperUtil(null, -1, "教师权限不足");
        }
        if( status != 0 && status != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "status值错误");
        }
        courseService.auditStudent(courseId, studentId, status);
        return new HttpResponseWrapperUtil(null);
    }

    @ApiOperation("教师重置其课程中学生密码接口")
    @GetMapping(value = "/resetStudentPasswordByTeacher/{courseId}/{studentId}")
    public HttpResponseWrapperUtil resetStudentPasswordByTeacher(@PathVariable(value = "courseId") int courseId,
                                                                 @PathVariable(value = "studentId") long studentId){
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        if( courseService.isStudentJoinCourse(studentId, courseId) != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "权限不足");
        }
        Student student = studentService.getStudentById(studentId);
        student.setPassword(MD5Util.encryptedByMD5(student.getEmail()));
        studentService.resetPassword(student);
        return new HttpResponseWrapperUtil(null);
    }

    /**
     * 将学生与课程的关系封装为Map
     * 为了避免代码冗余
     * @param studentId - 学生ID
     * @param courseId - 课程ID
     * @return
     */
    public Map<String,Object> wrapCourseStudentRelationship2Json(long studentId, int courseId) {
        Map<String,Object> result = new HashMap<>();
        Course course = courseService.getCourseById(courseId);
        Student student = studentService.getStudentById(studentId);
        if( course == null ){
            result.put("httpResponse",new HttpResponseWrapperUtil(null, -1, "课程不存在"));
        } else if( student == null ){
            result.put("httpResponse",new HttpResponseWrapperUtil(null, -1, "学生不存在"));
        } else{
            result.put("httpResponse",null);
        }
        result.put("status",courseService.isStudentJoinCourse(studentId,courseId));
        return result;
    }

    /**
     * 将课程对象列表基本信息封装成JSON数据
     * @param courseList - 课程对象列表
     * @return JSON数据
     */
    public HttpResponseWrapperUtil wrapCourseList2JSON(List<Course> courseList){
        if( courseList == null ){
            return new HttpResponseWrapperUtil(null, -1, "failure");
        }
        List<Object> data = new ArrayList<>();
        for (Course course : courseList) {
            Map<String,Object> metadata = new HashMap<>();
            metadata.put("courseId", course.getCourseId());
            metadata.put("courseName", course.getCourseName());
            data.add(metadata);
        }
        return new HttpResponseWrapperUtil(data);
    }
}