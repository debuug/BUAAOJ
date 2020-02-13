package cn.edu.buaa.onlinejudge.controller;

import cn.edu.buaa.onlinejudge.model.Department;
import cn.edu.buaa.onlinejudge.model.Student;
import cn.edu.buaa.onlinejudge.service.DepartmentService;
import cn.edu.buaa.onlinejudge.service.StudentService;
import cn.edu.buaa.onlinejudge.utils.HttpResponseWrapperUtil;
import cn.edu.buaa.onlinejudge.utils.ImageVerifyCodeUtil;
import cn.edu.buaa.onlinejudge.utils.MD5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "用户相关接口")
@RestController
@RequestMapping(value = "BUAAOJ/accounts")
public class AccountsController {
    @Autowired
    private StudentService studentService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "生成图片验证码接口")
    @RequestMapping(value = "/getImgVerifyCode", method = RequestMethod.GET)
    public void getImgVerifyCode(HttpSession session, HttpServletResponse response) {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        //生成随机字串
        String verifyCode = ImageVerifyCodeUtil.generateVerifyCode(4);
        session.setAttribute("verifyCode", verifyCode);
        //生成验证码图片
        int width = 100, height = 30;
        try {
            ImageVerifyCodeUtil.outputImage(width, height, response.getOutputStream(), verifyCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "学生登录接口")
    @RequestMapping(value = "/studentLogin", method = RequestMethod.POST)
    public HttpResponseWrapperUtil studentLogin(@RequestBody Student stu, HttpServletRequest request) {
        String codeInSession = (String) request.getSession().getAttribute("verifyCode");
        if( !checkVerifyCode(codeInSession, stu.getVerifyCode()) ) {
            return new HttpResponseWrapperUtil(null, -1, "验证码错误");
        }
        Student realStu = studentService.getStudentByEmail(stu.getEmail());
        if( realStu == null ){
            return new HttpResponseWrapperUtil(null, -1, "账号不存在");
        }
        if( !MD5Util.verifyMD5(stu.getPassword(), realStu.getPassword()) ){
            return new HttpResponseWrapperUtil(null, -1, "账号或密码错误");
        }
        Map<String,Object> data = new HashMap<>();
        data.put("studentId",realStu.getStudentId());
        data.put("studentName",realStu.getStudentName());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生注册接口")
    @RequestMapping(value = "/studentRegister", method = RequestMethod.POST)
    public HttpResponseWrapperUtil studentRegister(@RequestBody Student stu, HttpServletRequest request){
        Student realStu = studentService.getStudentByEmail(stu.getEmail());
        if( realStu != null ){
            return new HttpResponseWrapperUtil(null, -1, "该邮箱已注册");
        }
        String codeInSession = (String) request.getSession().getAttribute("verifyCode");
        if( !checkVerifyCode(codeInSession, stu.getVerifyCode()) ) {
            return new HttpResponseWrapperUtil(null, -1, "验证码错误");
        }
        stu.setPassword(MD5Util.encryptedByMD5(stu.getPassword()));
        studentService.insertStudent(stu);
        Map<String,Object> data = new HashMap<>();
        data.put("studentId",stu.getStudentId());
        data.put("studentName",stu.getStudentName());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("查看学生信息接口")
    @RequestMapping(value = "/getStudentInfo/{studentId}", method = RequestMethod.GET)
    public HttpResponseWrapperUtil getStudentInfo(@PathVariable("studentId") long studentId){
        Student stu = studentService.getStudentById(studentId);
        if( stu == null ) {
            return new HttpResponseWrapperUtil(null, -1, "failure");
        }
        Map<String,Object> data = new HashMap<>();
        data.put("email",stu.getEmail());
        data.put("studentName",stu.getStudentName());
        data.put("studentNumber",stu.getStudentNumber());
        data.put("departmentId",stu.getDepartmentId());
        data.put("introduction",stu.getIntroduction());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生修改个人信息接口")
    @RequestMapping(value = "/updateStudentInfo", method = RequestMethod.POST)
    public HttpResponseWrapperUtil updateStudentInfo(@RequestBody Student stu){
        studentService.updateStudent(stu);
        Student newStu = studentService.getStudentById(stu.getStudentId());
        if( newStu == null ) {
            return new HttpResponseWrapperUtil(null, -1, "该用户不存在");
        }
        Map<String,Object> data = new HashMap<>();
        data.put("studentId",newStu.getStudentId());
        data.put("studentName",newStu.getStudentName());
        data.put("studentNumber",newStu.getStudentNumber());
        data.put("departmentId",newStu.getDepartmentId());
        data.put("introduction",newStu.getIntroduction());
        return new HttpResponseWrapperUtil(data);
    }

    @ApiOperation("学生修改密码接口")
    @RequestMapping(value = "/resetStudentPassword", method = RequestMethod.POST)
    public HttpResponseWrapperUtil resetStudentPassword(@RequestParam(value = "studentId") long studentId,
                                                         @RequestParam(value = "oldPassword") String oldPassword,
                                                         @RequestParam(value = "newPassword") String newPassword){
        Student stu = studentService.getStudentById(studentId);
        if( stu == null ){
            return new HttpResponseWrapperUtil(null, -1, "该用户不存在");
        }
        if( !MD5Util.verifyMD5(oldPassword, stu.getPassword()) ){
            return new HttpResponseWrapperUtil(null, -1, "旧密码输入错误");
        }
        if( oldPassword.equals(newPassword) ){
            return new HttpResponseWrapperUtil(null, -1, "新密码与旧密码不能相同");
        }
        stu.setPassword(MD5Util.encryptedByMD5(newPassword));
        studentService.resetPassword(stu);
        return new HttpResponseWrapperUtil(null);
    }

    /**
     * 判断验证码是否正确
     * @param codeInSession - Session中的验证码
     * @param verfiyCode - 用户输入的验证码
     * @return - 验证码是否正确的布尔值
     */
    public boolean checkVerifyCode(String codeInSession, String verfiyCode) {
        if(StringUtils.isEmpty(codeInSession) ||
                !codeInSession.equals(verfiyCode.toUpperCase())) {
            return false;
        }
        return true;
    }
}
