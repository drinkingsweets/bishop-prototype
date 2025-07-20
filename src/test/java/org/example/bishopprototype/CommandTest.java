package org.example.bishopprototype;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bishopprototype.controller.BishopController;
import org.example.synthetichumancorestarter.command.CommandQueueManager;
import org.example.synthetichumancorestarter.command.CriticalCommandProcessor;
import org.example.synthetichumancorestarter.command.model.Command;
import org.example.synthetichumancorestarter.command.model.PriorityType;
import org.example.synthetichumancorestarter.exception.GlobalExceptionHandler;
import org.example.synthetichumancorestarter.exception.ValidationException;
import org.example.synthetichumancorestarter.monitoring.TaskMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    private MockMvc mockMvc;

    private final ObjectMapper jacksonMapper = new ObjectMapper();

    @Mock
    private CommandQueueManager commandQueueManager;

    @Mock
    private CriticalCommandProcessor criticalCommandProcessor;

    @Mock
    private TaskMetricsService taskMetricsService;

    @InjectMocks
    private BishopController bishopController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bishopController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notValidCommand() throws Exception {
        Command command = new Command("тестовая команда 1", PriorityType.COMMON, "",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(command, "command");
        bindingResult.addError(new FieldError("command", "author", "Автор не может быть пустым"));
        doThrow(new ValidationException(bindingResult))
                .when(commandQueueManager).submitCommand(any(Command.class));

        String commandJson = jacksonMapper.writeValueAsString(command);

        mockMvc.perform(post("/command/create")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(commandJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.author").value("Автор не может быть пустым"))
                .andDo(print());
    }

    @Test
    void invalidCommandFields() throws Exception {
        Command command = new Command("", PriorityType.COMMON, "", "2025-07-20 17:30:00");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(command, "command");
        bindingResult.addError(new FieldError("command", "description", "Описание команды должно быть до 1000 символов"));
        bindingResult.addError(new FieldError("command", "author", "Имя автора должно быть до 100 символов"));
        bindingResult.addError(new FieldError("command", "time", "Время должно быть в формате ISO8601(например, 1970-01-01T12:30:00Z)"));
        doThrow(new ValidationException(bindingResult))
                .when(commandQueueManager).submitCommand(any(Command.class));

        String commandJson = jacksonMapper.writeValueAsString(command);

        mockMvc.perform(post("/command/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(commandJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.description").value("Описание команды должно быть до 1000 символов"))
                .andExpect(jsonPath("$.details.author").value("Имя автора должно быть до 100 символов"))
                .andExpect(jsonPath("$.details.time").value("Время должно быть в формате ISO8601(например, 1970-01-01T12:30:00Z)"))
                .andDo(print());
    }
}