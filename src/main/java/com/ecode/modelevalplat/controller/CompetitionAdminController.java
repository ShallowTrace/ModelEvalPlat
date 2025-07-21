/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ecode.modelevalplat.controller;

import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.example.demo.entity.Competition;
import com.ecode.modelevalplat.service.CompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CompetitionAdminController {
    @Autowired
    CompetitionService competitionService;

    // GET http://127.0.0.1:8080/api/competitions
    @GetMapping("/api/competitions")
    @ResponseBody
    public List<CompetitionDO> selectAllCompetition(){
        return competitionService.selectAllCompetition();
    }

    // POST http://127.0.0.1:8080/api/competitions
    @PostMapping("/api/competitions")
    @ResponseBody
    public int publishCompetition(@RequestBody Competition competition){
        return competitionService.publishCompetition(competition);
    }

    // DELETE http://127.0.0.1:8080/api/competitions/{competitionId}
    @DeleteMapping("/api/competitions/{competitionId}")
    @ResponseBody
    public int deleteCompetition(@PathVariable Long competitionId){
        return competitionService.deleteCompetition(competitionId);
    }

    // PUT http://127.0.0.1:8080/api/competitions/{competitionId}/content
    @PutMapping("/api/competitions/{competitionId}/content")
    @ResponseBody
    public int updateCompetitionDescription(@PathVariable Long competitionId,  @RequestBody String content){
        return competitionService.updateCompetitionDescription(competitionId,content);
    }

    // PUT http://127.0.0.1:8080/api/competitions/{competitionId}/startTime
    @PutMapping("/api/competitions/{competitionId}/startTime")
    @ResponseBody
    public int updateCompetitionStartTime(@PathVariable Long competitionId,  @RequestBody LocalDateTime startTime){
        return competitionService.updateCompetitionStartTime(competitionId,startTime);
    }

    // PUT http://127.0.0.1:8080/api/competitions/{competitionId}/endTime
    @PutMapping("/api/competitions/{competitionId}/endTime")
    @ResponseBody
    public int updateCompetitionEndTime(@PathVariable Long competitionId,  @RequestBody LocalDateTime endTime){
        return competitionService.updateCompetitionEndTime(competitionId,endTime);
    }
}