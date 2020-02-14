package cn.edu.buaa.onlinejudge.controller;

import cn.edu.buaa.onlinejudge.model.Contest;
import cn.edu.buaa.onlinejudge.model.Course;
import cn.edu.buaa.onlinejudge.model.Problem;
import cn.edu.buaa.onlinejudge.model.Student;
import cn.edu.buaa.onlinejudge.service.*;
import cn.edu.buaa.onlinejudge.utils.DateUtil;
import cn.edu.buaa.onlinejudge.utils.HttpResponseWrapperUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Api(tags = "课程相关接口")
@RestController
@RequestMapping(value = "BUAAOJ/courses")
public class CoursesController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ContestService contestService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private SubmissionService submissionService;

    @ApiOperation("查看所有课程接口")
    @RequestMapping(value = "/getAllCourses", method = RequestMethod.GET)
    public HttpResponseWrapperUtil getAllCourses() {
        List<Course> allCourses = courseService.getAllCourses();
        if( allCourses == null ){
            return new HttpResponseWrapperUtil(null, -1, "failure");
        }
        List<Object> data = new ArrayList<>();
        for (Course course : allCourses) {
            Map<String,Object> metadata = new HashMap<>();
            metadata.put("courseId", course.getCourseId());
            metadata.put("courseName", course.getCourseName());
            data.add(metadata);
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生查看课程主页接口")
    @RequestMapping(value = "/getCourseHomepage/{studentId}/{courseId}", method = RequestMethod.GET)
    public HttpResponseWrapperUtil getCourseHomepage(@PathVariable("studentId") long studentId,
                                                     @PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ) {
            return new HttpResponseWrapperUtil(null, -1, "failure");
        }
        Map<String,Object> data = new HashMap<>();
        data.put("introduction", course.getIntroduction());
        data.put("coursewareUrl", course.getCoursewareUrl());
        int status = courseService.isStudentJoinCourse(studentId,courseId);
        data.put("isStudentAccept", status);
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生申请加入课程接口")
    @RequestMapping(value = "/joinCourse/{studentId}/{courseId}", method = RequestMethod.GET)
    public HttpResponseWrapperUtil joinCourse(@PathVariable("studentId") long studentId,
                                                     @PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        Student student = studentService.getStudentById(studentId);
        if( student == null ){
            return new HttpResponseWrapperUtil(null, -1, "学生不存在");
        }
        int status = courseService.isStudentJoinCourse(studentId,courseId);
        if( status >= 0 ){
            return new HttpResponseWrapperUtil(null, -1, "请勿重复提交申请");
        }
        courseService.joinCourse(studentId,courseId);
        return new HttpResponseWrapperUtil(null);
    }

    @ApiOperation("学生查看课程成员接口")
    @RequestMapping(value = "/getCourseMembers/{studentId}/{courseId}", method = RequestMethod.GET)
    public HttpResponseWrapperUtil getCourseMembers(@PathVariable("studentId") long studentId,
                                              @PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        Student student = studentService.getStudentById(studentId);
        if( student == null ){
            return new HttpResponseWrapperUtil(null, -1, "学生不存在");
        }
        int status = courseService.isStudentJoinCourse(studentId,courseId);
        if( status != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "请先加入课程");
        }
        List<Object> data = courseService.getCourseMembers(courseId);
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生查看课程题库接口")
    @RequestMapping(value = "/getProblemsOfCourse/{courseId}", method = RequestMethod.GET)
    public HttpResponseWrapperUtil getProblemsOfCourse(@PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        List<Contest> contestList = contestService.getContestsOfCourse(courseId);
        if( contestList == null || contestList.size() == 0 ){
            return new HttpResponseWrapperUtil(null);
        }
        List<Integer> contestIdList = new ArrayList<>();
        for (Contest contest : contestList) {
            contestIdList.add(contest.getContestId());
        }
        List<Problem> problemList = problemService.getProblemsByContestIdList(contestIdList);
        List<Object> data = new ArrayList<>();
        for (Problem problem : problemList) {
            Map<String,Object> metadata = new HashMap<>();
            metadata.put("problemId",problem.getProblemId());
            metadata.put("problemName",problem.getProblemName());
            metadata.put("acceptStudents",
                    submissionService.getProblemAcceptStudents(problem.getProblemId()));
            metadata.put("submitStudents",
                    submissionService.getProblemSubmitStudents(problem.getProblemId()));
            metadata.put("submitTimes",
                    submissionService.getProblemSubmitTimes(problem.getProblemId()));
            data.add(metadata);
        }
        return new HttpResponseWrapperUtil(data);
    }
}