package cn.edu.buaa.onlinejudge.mapper;

import cn.edu.buaa.onlinejudge.model.Problem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemMapper {
    /**
     * 根据题目ID获取题目对象
     * @param problemId - 题目ID
     * @return Problem对象
     */
    Problem getProblemById(long problemId);

    /**
     * 获取多个竞赛下的所有题目
     * @param contestIdlist - 竞赛ID列表
     * @return Problem列表
     */
    List<Problem> getVisibleProblemsByContestIdList(List<Integer> contestIdlist);

    /**
     * 获取指定竞赛下的所有可见题目
     * @param contestId - 竞赛ID
     * @return Problem对象列表
     */
    List<Problem> getVisibleProblemsOfContest(int contestId);

    /**
     * 获取指定竞赛下的所有题目
     * @param contestId - 竞赛ID
     * @return - Problem对象列表
     */
    List<Problem> getAllProblemsOfContest(int contestId);

    /**
     * 获取指定竞赛下的所有题目的ID
     * @param contestId - 竞赛ID
     * @return - 题目ID列表
     */
    List<Long> getAllProblemIdOfContest(int contestId);

    /**
     * 插入题目
     * @param problem - 题目对象
     */
    void insertProblem(Problem problem);

    /**
     * 修改题目信息
     * @param problem - 题目对象
     */
    void updateProblem(Problem problem);

    /**
     * 删除题目
     * @param problemId - 题目ID
     */
    void deleteProblem(long problemId);

    /**
     * 设置题目的可见性
     * @param problemId - 题目ID
     * @param visibility - 可见性值（可见1，不可见0）
     */
    void setProblemVisibility(@Param("problemId") long problemId, @Param("visibility") int visibility);
}
