package cn.edu.buaa.onlinejudge;

import cn.edu.buaa.onlinejudge.service.ProblemService;
import cn.edu.buaa.onlinejudge.utils.FileUtil;
import cn.edu.buaa.onlinejudge.utils.ZipUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTest extends OnlinejudgeApplicationTests {
    @Autowired
    private ProblemService problemService;

    @Test
    public void testFile() throws IOException {
//        String filePath = "D:/resources/1/";
//        File zipFile = new File(filePath + "courseware.zip");
//        zipFile.delete();
//        String tmpPath = FileUtil.getTmpDirPath();
//        File tmpFile = new File(tmpPath + "courseware.zip");
//        if( !tmpFile.getParentFile().exists() ){
//            tmpFile.getParentFile().mkdirs();
//        }
//        FileOutputStream fos = new FileOutputStream(tmpFile);
//        ZipUtil.toZip(filePath,fos,true);
//        FileUtil.removeFile(tmpPath + "courseware.zip",filePath + "courseware.zip");
    }

    @Test
    public void testDeleteCheckpointFile(){
        problemService.deleteProblemCheckpointFile(1);
    }

}
