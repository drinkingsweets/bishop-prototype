package org.example.bishopprototype;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.bishopprototype.controller.BishopController;
import org.example.synthetichumancorestarter.command.CommandQueueManager;
import org.example.synthetichumancorestarter.command.model.Command;
import org.example.synthetichumancorestarter.command.model.PriorityType;
import org.example.synthetichumancorestarter.exception.ValidationException;
import org.example.synthetichumancorestarter.monitoring.TaskMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BishopPrototypeApplicationTests {

    @Autowired
    private ObjectMapper jacksonMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommandQueueManager commandQueueManager;


    @Test
    void createCommonCommand() throws Exception {
        Command command = new Command("тестовая команда 1", PriorityType.COMMON, "test",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        String commandJson = jacksonMapper.writeValueAsString(command);
        String expected = "Команда принята";

        mockMvc.perform(post("/command/create")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(commandJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expected))
                .andDo(print());
    }

    @Test
    void createCriticalCommand() throws Exception {
        Command command = new Command("тестовая команда 1", PriorityType.CRITICAL, "test",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        String commandJson = jacksonMapper.writeValueAsString(command);
        String expected = "Команда принята";

        mockMvc.perform(post("/command/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(commandJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expected))
                .andDo(print());
    }

    @Test
    void createThreeCommandsAndCheckMetrics() throws Exception {
        Command command1 = new Command("тестовая команда 1", PriorityType.COMMON, "test1",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        Command command2 = new Command("тестовая команда 2", PriorityType.CRITICAL, "test1",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        Command command3 = new Command("тестовая команда 3", PriorityType.COMMON, "igor",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));


        String commandJson1 = jacksonMapper.writeValueAsString(command1);
        String commandJson2 = jacksonMapper.writeValueAsString(command2);
        String commandJson3 = jacksonMapper.writeValueAsString(command3);

        String expected = "Команда принята";

        mockMvc.perform(post("/command/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(commandJson1))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));

        mockMvc.perform(post("/command/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(commandJson2))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));

        mockMvc.perform(post("/command/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(commandJson3))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));

        String jsonFinal = jacksonMapper.writeValueAsString(Map.of(
                "queueSize", 0,
                "completedByAuthor", Map.of(
                        "test1", 2,
                        "igor", 1
                )
        ));

        Thread.sleep(2000);

        mockMvc.perform(MockMvcRequestBuilders.get("/command/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonFinal));
    }

}
