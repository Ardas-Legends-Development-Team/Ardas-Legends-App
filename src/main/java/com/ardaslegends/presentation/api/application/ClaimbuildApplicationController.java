package com.ardaslegends.presentation.api.application;

import com.ardaslegends.presentation.AbstractRestController;
import com.ardaslegends.presentation.api.response.applications.ClaimbuildApplicationResponse;
import com.ardaslegends.service.applications.ClaimbuildApplicationService;
import com.ardaslegends.service.dto.applications.ApplicationVoteDto;
import com.ardaslegends.service.dto.applications.CreateClaimbuildApplicationDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ClaimbuildApplicationController.BASE_URL)
public class ClaimbuildApplicationController extends AbstractRestController {
    public static final String BASE_URL = "/api/applications/claimbuild";

    private static final String FIND_ALL = "/all";
    private static final String FIND_ACTIVE = "/active";
    private static final String ADD_VOTE = "/vote/accept";
    private static final String REMOVE_VOTE = "/vote/remove";
    private static final String ADD_DECLINE_VOTE = "/vote/decline";

    private final ClaimbuildApplicationService cbbAppService;

    @Operation(summary = "Creates a Claimbuild Application")
    @PostMapping
    public HttpEntity<ClaimbuildApplicationResponse> createClaimbuildApplication(@RequestBody CreateClaimbuildApplicationDto applicationDto) {
        log.debug("Incoming createClaimbuildApplication Request: Data [{}]", applicationDto);

        val application = cbbAppService.createClaimbuildApplication(applicationDto);

        return ResponseEntity.ok(new ClaimbuildApplicationResponse(application));
    }

    @Operation(summary = "Returns a slice of all Claimbuild Applications")
    @GetMapping(FIND_ALL)
    public HttpEntity<Slice<ClaimbuildApplicationResponse>> findAllSliced(Pageable pageable) {
        log.debug("Incoming findAllCbApplications Request: Data [{}]", pageable.toString());

        val originalSlice = cbbAppService.findAll(pageable);
        val appsResponse = originalSlice.map(ClaimbuildApplicationResponse::new);

        return ResponseEntity.ok(appsResponse);
    }
    @Operation(summary = "Returns a slice of only active Claimbuild Applications")
    @GetMapping(FIND_ACTIVE)
    public HttpEntity<Slice<ClaimbuildApplicationResponse>> findAllActiveAppsSliced(Pageable pageable) {
        log.debug("Incoming findAllActiveApplications Request: Data [{}]", pageable.toString());

        val roleplayAppSlice = cbbAppService.findAllActive(pageable);
        val roleplayAppResponseSlice = roleplayAppSlice.map(ClaimbuildApplicationResponse::new);

        return ResponseEntity.ok(roleplayAppResponseSlice);
    }

    @Operation(summary = "Adds a upvote to an application, if the same staff member has downvoted this application then that downvote will be removed")
    @PatchMapping(ADD_VOTE)
    public HttpEntity<ClaimbuildApplicationResponse> addAcceptVote(ApplicationVoteDto voteDto) {
        log.debug("Incoming add-vote to claimbuild application Request: Data [{}]", voteDto);

        val application = cbbAppService.addAcceptVote(voteDto);

        return ResponseEntity.ok(new ClaimbuildApplicationResponse(application));
    }


    @Operation(summary = "Adds a downvote to an application, if the same staff member has upvoted this application then that upvoted will be removed")
    @PatchMapping(ADD_DECLINE_VOTE)
    public HttpEntity<ClaimbuildApplicationResponse> addDeclineVote(ApplicationVoteDto voteDto) {
        log.debug("Incoming add-vote to claimbuild application Request: Data [{}]", voteDto);

        val application = cbbAppService.addDeclineVote(voteDto);

        return ResponseEntity.ok(new ClaimbuildApplicationResponse(application));
    }

    @Operation(summary = "Removes a vote from a claimbuild application")
    @PatchMapping(REMOVE_VOTE)
    public HttpEntity<ClaimbuildApplicationResponse> removeVote(ApplicationVoteDto voteDto) {
        log.debug("Incoming remove-vote from claimbuild application Request: Data [{}]", voteDto);

        val application = cbbAppService.removeVote(voteDto);

        return ResponseEntity.ok(new ClaimbuildApplicationResponse(application));
    }

}
