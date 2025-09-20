package com.example.ooxx.Retrofit;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.example.ooxx.model.BaseResponse;
import com.example.ooxx.model.GameTopic;
import com.example.ooxx.model.RankItem;

public interface GameApiService {
    // 获取游戏题目（分页）
    @GET("api/game/getTopic")
    Call<BaseResponse<List<GameTopic>>> getGameTopic(
            @Query("page") int page,
            @Query("size") int size
    );

    // 提交对战结果到服务器
    @GET("api/game/submitResult")
    Call<BaseResponse<Boolean>> submitResult(
            @Query("userName") String userName,
            @Query("gameId") int gameId,
            @Query("result") String result,
            @Query("score") int score
    );

    // 获取服务器排行榜
    @GET("api/game/getRank")
    Call<BaseResponse<List<RankItem>>> getServerRank();

    @GET("api/game/submitMove")
    Call<BaseResponse<Boolean>> submitMove(
            @Query("userName") String userName,
            @Query("gameId") int gameId,
            @Query("row") int row,
            @Query("col") int col,
            @Query("player") int player
    );
}
