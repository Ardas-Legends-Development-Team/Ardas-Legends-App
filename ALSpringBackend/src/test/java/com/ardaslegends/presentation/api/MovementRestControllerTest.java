package com.ardaslegends.presentation.api;

import com.ardaslegends.data.domain.Movement;
import com.ardaslegends.data.presentation.api.MovementRestController;
import com.ardaslegends.data.service.MovementService;
import com.ardaslegends.data.service.dto.player.rpchar.MoveRpCharDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class MovementRestControllerTest {
    MockMvc mockMvc;

    MovementService mockMovementService;

    MovementRestController movementRestController;

    @BeforeEach
    void setup() {
        mockMovementService = mock(MovementService.class);
        movementRestController = new MovementRestController(mockMovementService);
        mockMvc = MockMvcBuilders.standaloneSetup(movementRestController).build();
    }

    @Test
    void ensureMoveRoleplayCharacterWorksCorrectly() throws Exception {
        log.debug("Testing if moveRoleplayCharacter");

        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("RandoId","12.S");

        log.trace("Initialize return movement");
        Movement movement = Movement.builder().build();

        log.trace("Initializing mock methods");
        when(mockMovementService.moveRoleplayCharacter(dto)).thenReturn(movement);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

        String requestJson = ow.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("http://localhost:8080/api/movement/move-char")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}
