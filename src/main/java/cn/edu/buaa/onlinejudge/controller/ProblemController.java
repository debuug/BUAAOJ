package cn.edu.buaa.onlinejudge.controller;

import cn.edu.buaa.onlinejudge.model.*;
import cn.edu.buaa.onlinejudge.service.ContestService;
import cn.edu.buaa.onlinejudge.service.CourseService;
import cn.edu.buaa.onlinejudge.service.ProblemService;
import cn.edu.buaa.onlinejudge.service.SubmissionService;
import cn.edu.buaa.onlinejudge.utils.HttpResponseWrapperUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "题目相关接口")
@RestController
@RequestMapping(value = "BUAAOJ/problems")
public class ProblemController {

    private final ProblemService problemService;

    private final ContestService contestService;

    private final SubmissionService submissionService;

    private final CourseService courseService;

    public ProblemController(ProblemService problemService, ContestService contestService, SubmissionService submissionService, CourseService courseService) {
        this.problemService = problemService;
        this.contestService = contestService;
        this.submissionService = submissionService;
        this.courseService = courseService;
    }

    @ApiOperation("学生根据题目ID查看题目接口")
    @GetMapping(value = "/getProblemById/{studentId}/{problemId}")
    public HttpResponseWrapperUtil getProblemById(@PathVariable("studentId") long studentId,
                                                  @PathVariable("problemId") long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        Contest contest = contestService.getContestById(problem.getContestId());
        if( problem == null || contest == null ){
            return new HttpResponseWrapperUtil(null, -1, "查询失败");
        }
        Submission submission = submissionService.getStudentLatestSubmissionOfProblem(studentId, problemId);
        Map<String,Object> data = wrapProblem2Json(problem);
        //竞赛在当前进行状态是否可答题
        boolean isAnswerable = contestService.isContestAnswerable(contest);
        data.put("isAnswerable", isAnswerable ? 1 : 0);
        String submitCode = (submission == null) ? null : submission.getSubmitCode();
        data.put("submitCode",submitCode);
        return new HttpResponseWrapperUtil(data);
    }

    /**
     * 不同学生查看到的题库不一定相同
     * 题库仅展示该学生已加入课程中的所有题目
     * @param pageSize - 页面大小
     * @param pageIndex - 页面索引
     * @param studentId - 学生ID
     * @return
     */
    @ApiOperation("学生查看题库接口")
    @GetMapping(value = "/getPageProblems/{pageSize}/{pageIndex}/{studentId}")
    public HttpResponseWrapperUtil getPageProblems(@PathVariable("pageSize") int pageSize,
                                                   @PathVariable("pageIndex") int pageIndex,
                                                   @PathVariable("studentId") long studentId) {
        if( pageSize < 0 || pageIndex < 0 ) {
            return new HttpResponseWrapperUtil(null, -1, "页面参数错误");
        }
        Map<String,Object> problemMap = problemService.getPageProblemsFromPersonalLibrary(studentId,pageSize,pageIndex);
        List<Problem> problemList = (List<Problem>)problemMap.get("problemList");
        Map<String,Object> data = new HashMap<>();
        data.put("totalProblemNum",problemMap.get("totalProblemNum"));
        if( problemList != null ){
            List<Object> problems = new ArrayList<>();
            for (Problem problem : problemList) {
                Map<String,Object> metadata = wrapProblemSubmitInfo2Json(problem);
                metadata.put("author",problem.getAuthor());
                Contest contest = contestService.getContestById(problem.getContestId());
                metadata.put("srcContest",contest.getContestName());
                metadata.put("srcContestId",contest.getContestId());
                problems.add(metadata);
            }
            data.put("problems",problems);
        } else {
            data.put("problems",null);
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生在题库中根据题目ID搜索题目接口")
    @GetMapping(value = "/searchProblemById/{studentId}/{problemId}")
    public HttpResponseWrapperUtil searchProblemById(@PathVariable("studentId") long studentId,
                                                     @PathVariable("problemId") long problemId) {
        Problem problem = problemService.getBasicProblemById(problemId);
        if( problem == null || !problem.isVisible() ){
            return new HttpResponseWrapperUtil(null, -1, "题目不存在");
        }
        Contest contest = contestService.getContestById(problem.getContestId());
        Course course = courseService.getCourseById(contest.getCourseId());
        //题库中不会展示学生尚未加入课程中的题目
        if( courseService.studentJoinCourseStatus(studentId, course.getCourseId()) != 1 ){
            return new HttpResponseWrapperUtil(null, -1, "题目不存在");
        }
        Map<String,Object> data = wrapProblemSubmitInfo2Json(problem);
        data.put("author", problem.getAuthor());
        data.put("srcContest", contest.getContestName());
        data.put("srcContestId", contest.getContestId());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生在题库中根据题目名称模糊搜索题目接口")
    @PostMapping(value = "/fuzzySearchProblemsByName")
    public HttpResponseWrapperUtil fuzzySearchProblemsByName(@RequestParam("studentId") long studentId,
                                                             @RequestParam("problemName") String problemName) {
        List<Problem> problemList = problemService.fuzzySearchProblemsByNameFromPersonalLibrary(studentId, problemName);
        if( problemList == null || problemList.size() == 0 ){
            return new HttpResponseWrapperUtil(null, -1, "无相关查询数据");
        }
        List<Object> data = new ArrayList<>();
        for (Problem problem : problemList) {
            Map<String,Object> metadata = wrapProblemSubmitInfo2Json(problem);
            metadata.put("author",problem.getAuthor());
            Contest contest = contestService.getContestById(problem.getContestId());
            metadata.put("srcContest",contest.getContestName());
            metadata.put("srcContestId",contest.getContestId());
            data.add(metadata);
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生在题库中根据题目作者模糊搜索题目接口")
    @PostMapping(value = "/fuzzySearchProblemsByAuthor")
    public HttpResponseWrapperUtil fuzzySearchProblemsByAuthor(@RequestParam("studentId") long studentId,
                                                               @RequestParam("author") String author) {
        List<Problem> problemList = problemService.fuzzySearchProblemsByAuthorFromPersonalLibrary(studentId, author);
        if( problemList == null || problemList.size() == 0 ){
            return new HttpResponseWrapperUtil(null, -1, "无相关查询数据");
        }
        List<Object> data = new ArrayList<>();
        for (Problem problem : problemList) {
            Map<String,Object> metadata = wrapProblemSubmitInfo2Json(problem);
            metadata.put("author",problem.getAuthor());
            Contest contest = contestService.getContestById(problem.getContestId());
            metadata.put("srcContest",contest.getContestName());
            metadata.put("srcContestId",contest.getContestId());
            data.add(metadata);
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生查看课程题库接口")
    @GetMapping(value = "/getProblemsOfCourse/{courseId}")
    public HttpResponseWrapperUtil getProblemsOfCourse(@PathVariable("courseId") int courseId) {
        Course course = courseService.getCourseById(courseId);
        if( course == null ){
            return new HttpResponseWrapperUtil(null, -1, "课程不存在");
        }
        List<Contest> contestList = contestService.getVisibleContestsOfCourse(courseId);
        if( contestList == null || contestList.size() == 0 ){
            return new HttpResponseWrapperUtil(null);
        }
        List<Integer> contestIdList = new ArrayList<>();
        for (Contest contest : contestList) {
            contestIdList.add(contest.getContestId());
        }
        List<Problem> problemList = problemService.getVisibleProblemsByContestIdList(contestIdList);
        List<Object> data = new ArrayList<>();
        for (Problem problem : problemList) {
            Map<String,Object> metadata = wrapProblemSubmitInfo2Json(problem);
            metadata.put("srcContestId",problem.getContestId());
            data.add(metadata);
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师获取题目可编辑信息")
    @GetMapping(value = "/getProblemEditInfo/{contestId}/{problemId}")
    public HttpResponseWrapperUtil getProblemEditInfo(@PathVariable("contestId") int contestId,
                                                      @PathVariable("problemId") long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        if( problem == null ){
                return new HttpResponseWrapperUtil(null, -1, "题目不存在");
        }
        if( problem.getContestId() != contestId ){
            return new HttpResponseWrapperUtil(null, -1, "权限不足");
        }
        Map<String,Object> data = wrapProblem2Json(problem);
        data.put("problemNumber",problem.getProblemNumber());
        data.put("judgeMechanism", problem.getJudgeMechanism());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师查看竞赛题目接口")
    @GetMapping(value = "/getProblemsOfContest/{contestId}")
    public HttpResponseWrapperUtil getProblemsOfContest(@PathVariable("contestId") int contestId) {
        Contest contest = contestService.getContestById(contestId);
        if( contest == null ){
            return new HttpResponseWrapperUtil(null, -1, "访问对象不存在");
        }
        List<Problem> problemList = problemService.getAllProblemsOfContest(contestId);
        List<Object> data = new ArrayList<>();
        if( problemList != null ){
            for (Problem problem : problemList) {
                Map<String,Object> metadata = new HashMap<>();
                metadata.put("problemId", problem.getProblemId());
                metadata.put("isVisible", problem.isVisible() ? 1 : 0);
                metadata.put("problemNumber", problem.getProblemNumber());
                metadata.put("problemName", problem.getProblemName());
                metadata.put("author", problem.getAuthor());
                data.add(metadata);
            }
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师新建题目接口")
    @PostMapping(value = "/createProblem")
    public HttpResponseWrapperUtil createProblem(@RequestBody Problem problem) {
        Contest contest = contestService.getContestById(problem.getContestId());
        if( contest == null ){
            return new HttpResponseWrapperUtil(null, -1, "操作失败");
        }
        problemService.insertProblem(problem);
        Map<String,Object> data = new HashMap<>();
        data.put("problemId", problem.getProblemId());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("教师修改题目信息接口")
    @PostMapping(value = "/updateProblem")
    public HttpResponseWrapperUtil updateProblem(@RequestBody Problem problem) {
        problemService.updateProblem(problem);
        Problem newProblem = problemService.getProblemById(problem.getProblemId());
        Map<String,Object> data = wrapProblem2Json(newProblem);
        data.put("problemNumber",newProblem.getProblemNumber());
        data.put("judgeMechanism", newProblem.getJudgeMechanism());
        return new HttpResponseWrapperUtil(data);
    }

    /**
     * 教师删除题目，同时会删除题目测试点文件
     * @param contestId - 竞赛ID
     * @param problemId - 题目ID
     * @return
     */
    @ApiOperation("教师删除题目接口")
    @GetMapping(value = "/deleteProblem/{contestId}/{problemId}")
    public HttpResponseWrapperUtil deleteProblem(@PathVariable("contestId") int contestId,
                                                 @PathVariable("problemId") long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        if( problem == null ){
            return new HttpResponseWrapperUtil(null, -1, "该题目不存在");
        }
        if( problem.getContestId() != contestId ){
            return new HttpResponseWrapperUtil(null, -1, "权限不足");
        }
        problemService.deleteProblem(problemId);
        return new HttpResponseWrapperUtil(null);
    }

    @ApiOperation("教师反转题目可见性接口")
    @GetMapping(value = "/reverseProblemVisibility/{contestId}/{problemId}")
    public HttpResponseWrapperUtil reverseContestVisibility(@PathVariable("contestId") int contestId,
                                                            @PathVariable("problemId") long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        if( problem == null ){
            return new HttpResponseWrapperUtil(null, -1, "题目不存在");
        }
        if( problem.getContestId() != contestId ){
            return new HttpResponseWrapperUtil(null, -1, "权限不足");
        }
        problemService.reverseProblemVisibility(problemId);
        return new HttpResponseWrapperUtil(null);
    }

    /**
     * 将题目基本信息封装为JSON数据格式
     * @param problem - 题目对象
     * @return JSON数据
     */
    public Map<String,Object> wrapProblem2Json(Problem problem){
        Map<String,Object> data = new HashMap<>();
        data.put("problemName",problem.getProblemName());
        data.put("timeLimit",problem.getTimeLimit());
        data.put("memoryLimit",problem.getMemoryLimit());
        data.put("description",problem.getDescription());
        data.put("inputFormat",problem.getInputFormat());
        data.put("outputFormat",problem.getOutputFormat());
        data.put("inputOutputSamples", problem.getInputOutputSamples());
        data.put("sampleExplanation",problem.getSampleExplanation());
        data.put("hint",problem.getHint());
        data.put("code",problem.getCode());
        return data;
    }

    /**
     * 将题目的提交信息封装为JSON数据格式
     * @param problem - Problem对象
     * @return - Map对象
     */
    public Map<String,Object> wrapProblemSubmitInfo2Json(Problem problem) {
        Map<String,Object> metadata = new HashMap<>();
        metadata.put("problemId",problem.getProblemId());
        metadata.put("problemName",problem.getProblemName());
        metadata.put("acceptStudents",
                submissionService.getProblemAcceptStudents(problem.getProblemId()));
        metadata.put("submitStudents",
                submissionService.getProblemSubmitStudents(problem.getProblemId()));
        metadata.put("submitTimes",
                submissionService.getProblemSubmitTimes(problem.getProblemId()));
        return metadata;
    }
}