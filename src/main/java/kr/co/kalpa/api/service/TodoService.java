package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.TodoCreateRequest;
import kr.co.kalpa.api.entity.Todo;
import kr.co.kalpa.api.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    @Transactional
    public List<Todo> createTodos(TodoCreateRequest request) {
        if (request.getContents() == null || request.getContents().isEmpty()) {
            throw new IllegalArgumentException("Contents cannot be empty");
        }
        
        List<Todo> todos = request.getContents().stream()
                .map(content -> Todo.builder()
                        .content(content)
                        .build())
                .collect(Collectors.toList());
        
        return todoRepository.saveAll(todos);
    }

    @Transactional
    public void deleteTodo(Long id) {
        if (!todoRepository.existsById(id)) {
             throw new IllegalArgumentException("Todo not found with id: " + id);
        }
        todoRepository.deleteById(id);
    }
}
