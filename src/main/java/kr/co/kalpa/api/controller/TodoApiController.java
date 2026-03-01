package kr.co.kalpa.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.kalpa.api.dto.ApiResponse;
import kr.co.kalpa.api.dto.request.TodoCreateRequest;
import kr.co.kalpa.api.entity.Todo;
import kr.co.kalpa.api.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Todo", description = "Todo API")
public class TodoApiController {

    private final TodoService todoService;

    @PostMapping
    @Operation(summary = "Todo 생성", description = "여러 개의 Todo를 한 번에 생성합니다.")
    public ResponseEntity<ApiResponse<List<Todo>>> createTodos(@RequestBody TodoCreateRequest request) {
        List<Todo> createdTodos = todoService.createTodos(request);
        return ResponseEntity.ok(ApiResponse.success(createdTodos));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Todo 삭제", description = "ID로 Todo를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
