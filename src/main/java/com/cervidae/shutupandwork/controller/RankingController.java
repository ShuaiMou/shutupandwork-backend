package com.cervidae.shutupandwork.controller;

import com.cervidae.shutupandwork.pojo.Ranking;
import com.cervidae.shutupandwork.service.RankingService;
import com.cervidae.shutupandwork.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rankings")
public class RankingController {

    final RankingService rankingService;

    @Autowired
    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * Get rankings of users, return cached if not expired
     * @param top length of the rankings
     * @return ranking
     */
    @GetMapping(params = {"top"})
    public Response<Ranking> get(int top) {
        return Response.success(rankingService.getRankings(top));
    }

    /**
     * Forced to pull latest rankings from database, ignore caches
     * Use parameter: 'forced'
     * @param top length of the rankings
     * @return ranking
     */
    @GetMapping(params = {"top", "forced"})
    public Ranking getForced(int top) {
        return rankingService.getLatestRankings(top);
    }

}
