package cn.edu.buaa.onlinejudge.controller;

import cn.edu.buaa.onlinejudge.model.*;
import cn.edu.buaa.onlinejudge.service.JMSProducerService;
import cn.edu.buaa.onlinejudge.service.LanguageService;
import cn.edu.buaa.onlinejudge.service.ProblemService;
import cn.edu.buaa.onlinejudge.service.SubmissionService;
import cn.edu.buaa.onlinejudge.utils.DateUtil;
import cn.edu.buaa.onlinejudge.utils.HttpResponseWrapperUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "提交相关接口")
@RestController
@RequestMapping(value = "BUAAOJ/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    private final ProblemService problemService;

    private final LanguageService languageService;

    private final JMSProducerService jmsProducerService;

    public SubmissionController(SubmissionService submissionService, ProblemService problemService,
                                LanguageService languageService, JMSProducerService jmsProducerService) {
        this.submissionService = submissionService;
        this.problemService = problemService;
        this.languageService = languageService;
        this.jmsProducerService = jmsProducerService;
    }

    @ApiOperation("学生查看对指定题目的提交记录接口")
    @GetMapping(value = "/getProblemSubmissionsOfStudent/{studentId}/{problemId}")
    public HttpResponseWrapperUtil getProblemSubmissionsOfStudent(@PathVariable("studentId") long studentId,
                                                                  @PathVariable("problemId") long problemId) {
        List<Submission> submissionList = submissionService.getSubmissionsByStudentIdAndProblemId(studentId,problemId);
        Map<String,Object> data = new HashMap<>();
        data.put("acceptStudents", submissionService.getProblemAcceptStudents(problemId));
        data.put("submitStudents", submissionService.getProblemSubmitStudents(problemId));
        List<Object> submitRecords = new ArrayList<>();
        if( submissionList != null ) {
            for (Submission submission : submissionList) {
                Map<String,Object> metadata = wrapSubmission2Json(submission);
                submitRecords.add(metadata);
            }
        }
        data.put("submitRecords", submitRecords);
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生查看对指定竞赛的提交记录接口")
    @GetMapping(value = "/getContestSubmissionsOfStudent/{studentId}/{contestId}")
    public HttpResponseWrapperUtil getContestSubmissionsOfStudent(@PathVariable("studentId") long studentId,
                                                                  @PathVariable("contestId") int contestId) {
        List<Submission> submissionList = submissionService.getSubmissionsOfContest(studentId, contestId);
        List<Object> data = new ArrayList<>();
        if( submissionList != null ) {
            for (Submission submission : submissionList) {
                Map<String,Object> metadata = wrapSubmission2Json(submission);
                Problem problem = problemService.getProblemById(submission.getProblemId());
                metadata.put("problemName",problem.getProblemName());
                metadata.put("problemNumber",problem.getProblemNumber());
                Language language = languageService.getLanguageById(submission.getLanguageId());
                metadata.put("language",language.getLanguageName());
                //代码长度，以K为单位，保留到小数点后一位
                Float codeLength = 0.0f;
                try {
                    codeLength = submission.getSubmitCode().getBytes("utf-8").length / 1024.0f;
                } catch (UnsupportedEncodingException e) {
                    codeLength = null;
                    e.printStackTrace();
                } finally {
                    metadata.put("codeLength",String.format("%.1f",codeLength));
                }
                metadata.put("submitTime",DateUtil.formatTimestamp(submission.getSubmitTime()));
                data.add(metadata);
            }
        }
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生提交代码接口")
    @PostMapping(value = "/submitCode")
    public HttpResponseWrapperUtil submitCode(@RequestBody Submission submission) {
        submission.setSubmitTime(DateUtil.getCurrentTimestamp());
        Problem problem = problemService.getBasicProblemById(submission.getProblemId());
        if( problem == null ){
            return new HttpResponseWrapperUtil(null, -1, "题目不存在");
        }
        submission.setContestId(problem.getContestId());
        submissionService.insertSubmission(submission);
        if( submission.getSubmissionId() > 0 ){
            //提交ID进入ActiveMQ队列
            jmsProducerService.sendMessage(submission.getSubmissionId());
            Map<String,Object> data = wrapSubmission2Json(submission);
            return new HttpResponseWrapperUtil(data);
        } else{
            return new HttpResponseWrapperUtil(null, -1, "提交代码失败");
        }
    }

    /**
     * 将基本的提交记录信息封装成JSON数据格式
     * @param submission - 提交记录对象
     * @return JSON数据
     */
    public Map<String,Object> wrapSubmission2Json(Submission submission) {
        Map<String,Object> data = new HashMap<>();
        data.put("submissionId",submission.getSubmissionId());
        data.put("judgeResult",submission.getJudgeResult());
        data.put("judgeScore",submission.getJudgeScore());
        data.put("usedTime",submission.getUsedTime());
        data.put("usedMemory",submission.getUsedMemory());
        return data;
    }
}
