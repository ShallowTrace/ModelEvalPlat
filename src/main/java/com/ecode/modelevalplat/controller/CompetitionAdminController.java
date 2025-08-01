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
//import com.example.demo.entity.Competition;
import com.ecode.modelevalplat.service.CompetitionRegistrationService;
import com.ecode.modelevalplat.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CompetitionAdminController {

    private final CompetitionService competitionService;
    private final CompetitionRegistrationService competitionRegistrationService;
//     GET http://127.0.0.1:8002/api/competitions
    //    curl -X GET "http://127.0.0.1:8002/api/competitions"
    @GetMapping("/api/competitions")
    @ResponseBody
    public List<CompetitionDO> selectAllCompetition(){
        return competitionService.selectAllCompetition();
    }

//     POST http://127.0.0.1:8002/api/competitions
//        curl -X POST "http://127.0.0.1:8002/api/competitions" -H "Content-Type: application/json" -d '{
//                "name": "编程大赛",
//                "description": "年度编程挑战赛",
//                "start_time": "2025-08-01T09:00:00",
//                "end_time": "2025-08-01T18:00:00",
//                "path": "/data/competition1",
//                "is_active": true,
//                "participant_count": 1,
//                "daily_submission_limit": 6,
//                "created_at": "2025-08-01T09:00:00"
//    }'
    @PostMapping("/api/competitions")
    @ResponseBody
    public int publishCompetition(@RequestBody CompetitionDO competition){
        return competitionService.publishCompetition(competition);
    }

//     DELETE http://127.0.0.1:8082/api/competitions/{competitionId}
//        curl -X DELETE "http://127.0.0.1:8002/api/competitions/2" \
//                -H "Content-Type: application/json"
    @DeleteMapping("/api/competitions/{competitionId}")
    @ResponseBody
    public int deleteCompetition(@PathVariable Long competitionId){
        return competitionService.deleteCompetition(competitionId);
    }

    // PUT http://127.0.0.1:8082/api/competitions/{competitionId}/content
//     curl -X PUT "http://127.0.0.1:8002/api/competitions/1/content" -H "Content-Type: application/json" -d "重庆大学"
    @PutMapping("/api/competitions/{competitionId}/content")
    @ResponseBody
    public int updateCompetitionDescription(@PathVariable Long competitionId,  @RequestBody String content){
        return competitionService.updateCompetitionDescription(competitionId,content);
    }

//     PUT http://127.0.0.1:8082/api/competitions/{competitionId}/startTime
//     curl -X PUT "http://127.0.0.1:8002/api/competitions/1/startTime" -H "Content-Type: application/json" -d '"2023-10-01T15:30:09"'
    @PutMapping("/api/competitions/{competitionId}/startTime")
    @ResponseBody
    public int updateCompetitionStartTime(@PathVariable Long competitionId,  @RequestBody LocalDateTime startTime){
        return competitionService.updateCompetitionStartTime(competitionId,startTime);
    }

    // PUT http://127.0.0.1:8002/api/competitions/{competitionId}/endTime
//     curl -X PUT "http://127.0.0.1:8002/api/competitions/1/endTime" -H "Content-Type: application/json" -d '"2023-10-01T15:30:09"'
    @PutMapping("/api/competitions/{competitionId}/endTime")
    @ResponseBody
    public int updateCompetitionEndTime(@PathVariable Long competitionId,  @RequestBody LocalDateTime endTime){
        return competitionService.updateCompetitionEndTime(competitionId,endTime);
    }

    @PostMapping("/api/registrations/{userId}/{competitionId}")
    public int registerCompetition(@PathVariable Long userId, @PathVariable Long competitionId)
    {
        return competitionRegistrationService.registerCompetition(userId,competitionId);
    }


}