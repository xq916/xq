package com.example.puzzle_server.controller;

import com.example.puzzleserver.common.BaseResponse;
import com.example.puzzleserver.entity.GameTopic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/game")
public class GameController {
    
    // 模拟数据库中的谜题数据
    private List<GameTopic> puzzleList = Arrays.asList(
        new GameTopic(1, "211221121221212112212121121212022112", "完成棋盘匹配", "easy"),
        new GameTopic(2, "012012012012012012012012012012012012", "完成棋盘匹配", "medium"),
        new GameTopic(3, "201201201201201201201201201201201201", "完成棋盘匹配", "hard"),
        new GameTopic(4, "120120120120120120120120120120120120", "完成棋盘匹配", "easy"),
        new GameTopic(5, "021021021021021021021021021021021021", "完成棋盘匹配", "medium")
    );
    
    @GetMapping("/getTopic")
    public BaseResponse<List<GameTopic>> getGameTopic(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 随机返回一个谜题
        Random random = new Random();
        int index = random.nextInt(puzzleList.size());
        List<GameTopic> result = Collections.singletonList(puzzleList.get(index));
        
        return new BaseResponse<>(200, "success", result);
    }
}
